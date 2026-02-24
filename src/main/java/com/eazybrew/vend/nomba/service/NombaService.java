package com.eazybrew.vend.nomba.service;

import java.math.BigDecimal;

public interface NombaService {
    /**
     * Push a payment request to a Nomba POS terminal.
     *
     * @param terminalId    Nomba terminal ID assigned to the device
     * @param merchantTxRef unique reference for this transaction
     * @param amountNaira   the transaction amount in Naira (not kobo)
     */
    /**
     * Returns the Nomba-assigned paymentId from the push response,
     * which must be stored on the transaction for webhook lookup.
     */
    String pushPaymentToTerminal(String terminalId, String merchantTxRef, BigDecimal amountNaira);
}
