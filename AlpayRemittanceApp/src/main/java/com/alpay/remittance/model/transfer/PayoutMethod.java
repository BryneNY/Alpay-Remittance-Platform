package com.alpay.remittance.model.transfer;

/**
 * How a beneficiary in Ghana receives the money.
 */
public enum PayoutMethod {
    MOMO_WALLET("MoMo Wallet"),
    BANK_ACCOUNT("Bank Account");

    private final String label;

    PayoutMethod(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}