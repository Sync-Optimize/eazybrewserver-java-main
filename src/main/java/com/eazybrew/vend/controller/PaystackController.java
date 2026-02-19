package com.eazybrew.vend.controller;

import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.Transaction;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.paystack.model.PaystackTransaction;
import com.eazybrew.vend.paystack.repository.PaystackTransactionRepository;
import com.eazybrew.vend.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Optional;

@Tag(name = "Paystack Management", description = "Paystack")
@RestController
@RequestMapping("/api/paystack")
@RequiredArgsConstructor
public class PaystackController {

    @Value("${paystack.secret.key}")
    private String secretKey;
    @Value("${paystack.api.key}")
    private String apiKey;
    @Value("${paystack.baseurl}")
    private String baseURL;

    private final PaystackTransactionRepository paystackTransactionRepository;
    private final TransactionRepository transactionRepository;


    @Operation(summary = "payment webhook for paystack new", description = "webhook for paystack")
    @PostMapping(value = "/8768907")
    public void webhookPaystack(HttpServletRequest request, HttpServletResponse response) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        String xpaystackSignature = request.getHeader("x-paystack-signature");
        String requestBody = "";
        System.out.println(">>>>>>>>>>>>>>>>>>>>> requestbody");
        System.out.println(requestBody);
        response.setStatus(200);
        try {

            requestBody = IOUtils.toString(request.getInputStream(), "UTF-8");
            System.out.println(">>>>>>>>>>>>>>>>>>>>> jsonbody" + requestBody);
            JSONObject body = new JSONObject(requestBody);

            System.out.println(body);
            String key = secretKey;

            String result = "";
            String HMAC_SHA512 = "HmacSHA512";
            byte[] byteKey = key.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            sha512_HMAC.init(keySpec);
            byte[] mac_data = sha512_HMAC.
                    doFinal(requestBody.getBytes("UTF-8"));
            result = DatatypeConverter.printHexBinary(mac_data);

            System.out.println("result >>>>>"+ result);
            System.out.println("xpaystackSignature >>>>>"+ xpaystackSignature);

            result.toLowerCase().equals(xpaystackSignature);
            if (result.toLowerCase().equals(xpaystackSignature)) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>> signature verified");
                String event = body.getString("event");
                if ("charge.success".equalsIgnoreCase(event)) {
                    BigDecimal amount = body.getJSONObject("data").getBigDecimal("amount");
                    String reference = body.getJSONObject("data").getString("reference");
                    System.out.println(">>>>>>>>>>>>>>>>>>>>> reference");
                    Optional<Transaction> transactionOptional = transactionRepository.findByPaystackReference(reference);
                    Optional<PaystackTransaction> paystackTransactionOptional = paystackTransactionRepository
                            .findByReference(reference);
                    PaystackTransaction paystackTransaction = null;
                    Transaction transaction = null;
                    if (transactionOptional.isPresent()) {
                        transaction = transactionOptional.get();
                        if (transaction.getAmount().multiply(BigDecimal.valueOf(100)).compareTo(amount) == 0) {
                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>> transaction found with equal amount");
                            transaction.setStatus(TransactionStatus.COMPLETED);
                        } else {
                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>> transaction not found with equal amount");
                            return;
                        }
                        transactionRepository.save(transaction);
                    }
                    if (paystackTransactionOptional.isPresent()) {
                        paystackTransaction = paystackTransactionOptional.get();
                        if (paystackTransaction.getAmount().compareTo(amount) == 0) {
                            paystackTransaction.setStatus(body.getJSONObject("data").getString("status"));
                        } else {
                            return;
                        }

                        paystackTransactionRepository.save(paystackTransaction);
                    } else {
                        return;
                    }
                    JSONObject authorization = body.getJSONObject("data").getJSONObject("authorization");


                }


            }
        } catch (IOException e) {

            System.out.println(e);
        }
    }
}
