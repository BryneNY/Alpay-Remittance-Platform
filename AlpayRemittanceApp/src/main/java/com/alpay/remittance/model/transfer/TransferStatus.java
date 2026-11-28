package com.alpay.remittance.model.transfer;

/**
 * The stages a transfer moves through, from submission to reconciliation.
 * The names match the status values stored in the transfers table.
 */
public enum TransferStatus {
    SUBMITTED,
    MTO_APPROVED,
    ALPAY_APPROVED,
    SETTLED,
    PAID,
    RECONCILED,
    REJECTED;

    /**
     * Readable label for screens, e.g. MTO_APPROVED becomes "Mto Approved".
     */
    @Override
    public String toString() {
        String[] words = name().split("_");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            label.append(word.charAt(0)).append(word.substring(1).toLowerCase()).append(' ');
        }
                return label.toString().trim().replace("Mto", "MTO");
    }
}