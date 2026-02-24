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
    void pushPaymentToTerminal(String terminalId, String merchantTxRef, BigDecimal amountNaira);
}
