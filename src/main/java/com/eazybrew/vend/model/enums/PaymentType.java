package com.eazybrew.vend.model.enums;

/**
 * Enum representing different payment types.
 * 
 * 1 = Pay with transfer
 * 2 = Pay with QR code
 * 3 = Pay with NFC
 * 4 = Pay with Bank Card
 */
public enum PaymentType {
    TRANSFER(1),
    QR_CODE(2),
    NFC(3),
    BANK_CARD(4);
    
    private final int value;
    
    PaymentType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Get PaymentType by its numeric value
     * 
     * @param value the numeric value
     * @return the corresponding PaymentType or null if not found
     */
    public static PaymentType fromValue(int value) {
        for (PaymentType type : PaymentType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}