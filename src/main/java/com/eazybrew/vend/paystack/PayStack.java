package com.eazybrew.vend.paystack;



import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.paystack.dto.request.*;
import com.eazybrew.vend.paystack.dto.response.*;

import java.util.List;

public interface PayStack {

  CreateCustomerPayStackResponse createCustomerOnPayStack(CreatePayStackCustomerRequest request);
  NubanResponse createBankAccountForCustomer(CreateVirtualAccountRequest request);
  TransferRecipientResponse transferFundsGetRecipient(BankAccountTransferRequest request);
  TransferTransactionResponse transferFunds(TransferTransactionRequest request);
  List<Bank> getAllBanks();
  AccountData doNameEnquiry(NameEnquiry enquiryRequest);
  InitializeTransactionResponse initializeTransaction(InitializeTransactionRequest request);
}
