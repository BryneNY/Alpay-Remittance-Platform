package com.alpay.remittance.model.transfer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks transfers for problems: amounts that don't match the exchange rate,
 * and transfers stuck at one stage for too long.
 */
public class ReconciliationReport {

    private final List<Transfer> allTransfers;
    private List<Transfer> transfers = new ArrayList<>();

    public ReconciliationReport(List<Transfer> allTransfers) {
        this.allTransfers = allTransfers;
    }

    /** Selects the transfers created between two dates, inclusive. */
    public void generate(LocalDate from, LocalDate to) {
        transfers = new ArrayList<>();
        for (Transfer transfer : allTransfers) {
            LocalDate created = transfer.getCreatedAt().toLocalDate();
            if (!created.isBefore(from) && !created.isAfter(to)) {
                transfers.add(transfer);
            }
        }
    }

    /** Paid transfers whose cedi amount doesn't equal CAD amount × exchange rate. */
    public List<Transfer> findMismatches() {
        List<Transfer> mismatches = new ArrayList<>();
        for (Transfer transfer : transfers) {
            boolean paid = transfer.getStatus() == TransferStatus.PAID
                    || transfer.getStatus() == TransferStatus.RECONCILED;
            double expected = Math.round(transfer.getAmountCad() * transfer.getExchangeRate() * 100.0) / 100.0;
            if (paid && Math.abs(expected - transfer.getAmountGhs()) > 0.009) {
                mismatches.add(transfer);
            }
        }
        return mismatches;
    }

    /** Unfinished transfers older than the given number of days. */
    public List<Transfer> findStuckTransfers(int days) {
        List<Transfer> stuck = new ArrayList<>();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        for (Transfer transfer : transfers) {
            TransferStatus status = transfer.getStatus();
            boolean finished = status == TransferStatus.PAID
                    || status == TransferStatus.RECONCILED
                    || status == TransferStatus.REJECTED;
            if (!finished && transfer.getCreatedAt().isBefore(cutoff)) {
                stuck.add(transfer);
            }
        }
        return stuck;
    }

    /** Only a paid transfer can be marked as reconciled. */
    public void markReconciled(Transfer transfer) {
        if (transfer.getStatus() != TransferStatus.PAID) {
            throw new IllegalStateException("Only paid transfers can be reconciled.");
        }
        transfer.updateStatus(TransferStatus.RECONCILED);
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }
}