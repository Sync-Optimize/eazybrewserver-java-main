package com.eazybrew.vend.paystack.impl;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.paystack.PayStack;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.paystack.dto.request.*;
import com.eazybrew.vend.paystack.dto.response.*;
import com.eazybrew.vend.paystack.model.PaystackTransaction;
import com.eazybrew.vend.paystack.repository.PaystackTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PayStackImpl implements PayStack {

    @Value("${paystack.secret.key}")
    private String secretKey;
    @Value("${paystack.api.key}")
    private String apiKey;
    @Value("${paystack.baseurl}")
    private String baseURL;

    @Value("${paystack.prefered.bank}")
    private String preferredBank;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PaystackTransactionRepository paystackTransactionRepository;

    @Override
    public CreateCustomerPayStackResponse createCustomerOnPayStack(CreatePayStackCustomerRequest request) {

        try {
            CreateCustomerPayStackResponse createCustomerPayStackResponse = null;
            String finalUri = baseURL + "/customer";
            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreatePayStackCustomerRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<CreateCustomerPayStackResponse> result = restTemplate.postForEntity(uri, httpRequest,
                    CreateCustomerPayStackResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                createCustomerPayStackResponse = result.getBody();

            }
            return createCustomerPayStackResponse;
        } catch (Exception e) {
            log.info("Error creating customer on paystack for email>> " + request.getEmail() + " " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public NubanResponse createBankAccountForCustomer(CreateVirtualAccountRequest request) {
        try {
            request.setPreferredBank(preferredBank);
            NubanResponse nubanResponse = null;
            String finalUri = baseURL + "/dedicated_account";
            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CreateVirtualAccountRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<NubanResponse> result = restTemplate.postForEntity(uri, httpRequest,
                    NubanResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                nubanResponse = result.getBody();

            }
            return nubanResponse;
        } catch (Exception e) {
            log.info("Error creating virtual account on paystack for customerId>> " + request.getPreferredBank() + " "
                    + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public TransferRecipientResponse transferFundsGetRecipient(BankAccountTransferRequest request) {
        try {

            TransferRecipientResponse transferRecipientResponse = null;
            String finalUri = baseURL + "/transferrecipient";
            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BankAccountTransferRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<TransferRecipientResponse> result = restTemplate.postForEntity(uri, httpRequest,
                    TransferRecipientResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                transferRecipientResponse = result.getBody();

            }
            return transferRecipientResponse;
        } catch (Exception e) {
            log.info("Error Transferring " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public TransferTransactionResponse transferFunds(TransferTransactionRequest request) {
        try {

            TransferTransactionResponse transactionResponse = null;
            String finalUri = baseURL + "/transfer";
            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TransferTransactionRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<TransferTransactionResponse> result = restTemplate.postForEntity(uri, httpRequest,
                    TransferTransactionResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                transactionResponse = result.getBody();

            }
            return transactionResponse;
        } catch (Exception e) {
            log.info("Error Transferring " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<Bank> getAllBanks() {
        try {

            BankResponse bankResponse = null;
            // Build the URI with query parameters
            String finalUri = UriComponentsBuilder.fromHttpUrl(baseURL + "/bank")
                    .queryParam("currency", "NGN")
                    .queryParam("enabled_for_verification", "true")
                    .toUriString();

            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request entity (headers only since it's a GET request)
            HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

            // Use RestTemplate's exchange method for GET request
            ResponseEntity<BankResponse> result = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpRequest,
                    BankResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                bankResponse = result.getBody();

            }
            return bankResponse != null ? bankResponse.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.info("Error Getting Banklist " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public AccountData doNameEnquiry(NameEnquiry enquiryRequest) {
        try {

            AccountNumberResponse accountNumberResponse = null;
            // Build the URI with query parameters
            String finalUri = UriComponentsBuilder.fromHttpUrl(baseURL + "/bank/resolve")
                    .queryParam("account_number", enquiryRequest.getAccountNumber())
                    .queryParam("bank_code", enquiryRequest.getCode())
                    .toUriString();

            URI uri = new URI(finalUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request entity (headers only since it's a GET request)
            HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

            // Use RestTemplate's exchange method for GET request
            ResponseEntity<AccountNumberResponse> result = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpRequest,
                    AccountNumberResponse.class);
            System.out.println(result);
            if (result.getStatusCode().is2xxSuccessful()) {
                accountNumberResponse = result.getBody();

            }
            return accountNumberResponse != null ? accountNumberResponse.getData() : null;
        } catch (Exception e) {
            log.info("Error Getting Banklist " + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public InitializeTransactionResponse initializeTransaction(InitializeTransactionRequest request) {
        try {

            String url = "https://api.paystack.co/transaction/initialize";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InitializeTransactionRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<InitializeTransactionResponse> response = restTemplate.postForEntity(url, entity,
                    InitializeTransactionResponse.class);

            InitializeTransactionResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.isStatus()) {
                PaystackTransaction transaction = new PaystackTransaction();
                transaction.setEmail(request.getEmail());
                transaction.setAmount(request.getAmount());
                transaction.setAuthorizationUrl(responseBody.getData().getAuthorization_url());
                transaction.setStatus("pending");
                transaction.setAccessCode(responseBody.getData().getAccess_code());
                transaction.setReference(responseBody.getData().getReference());
                paystackTransactionRepository.save(transaction);
            }

            return responseBody;
        } catch (Exception e) {
            log.info("initializing transaction" + e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
