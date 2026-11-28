package com.alpay.remittance.model.transfer;

import com.alpay.remittance.model.user.UserAccount;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One remittance from a sender in Canada to a beneficiary in Ghana.
 */
public class Transfer {

    private int transferId;
    private String reference;
    private UserAccount sender;
    private Beneficiary beneficiary;
    private double amountCad;
    private double fee;
    private double exchangeRate;
    private double amountGhs;
    private TransferStatus status;
    private LocalDateTime createdAt;

    public Transfer() {
        this.status = TransferStatus.SUBMITTED;
        this.createdAt = LocalDateTime.now();
    }

    public Transfer(UserAccount sender, Beneficiary beneficiary, double amountCad,
                    double fee, double exchangeRate) {
        this();
        this.sender = sender;
        this.beneficiary = beneficiary;
        this.amountCad = amountCad;
        this.fee = fee;
        this.exchangeRate = exchangeRate;
        this.reference = generateReference();
        calculateAmountGhs();
    }

    /** Creates a unique reference such as ALP-20261127-4821. */
    public String generateReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int number = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "ALP-" + date + "-" + number;
    }

    /** Converts the CAD amount to cedis, rounded to 2 decimal places. */
    public double calculateAmountGhs() {
        amountGhs = Math.round(amountCad * exchangeRate * 100.0) / 100.0;
        return amountGhs;
    }

    public void updateStatus(TransferStatus newStatus) {
        this.status = newStatus;
    }

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public UserAccount getSender() { return sender; }
    public void setSender(UserAccount sender) { this.sender = sender; }

    public Beneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(Beneficiary beneficiary) { this.beneficiary = beneficiary; }

    public double getAmountCad() { return amountCad; }
    public void setAmountCad(double amountCad) { this.amountCad = amountCad; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }

    public double getAmountGhs() { return amountGhs; }
    public void setAmountGhs(double amountGhs) { this.amountGhs = amountGhs; }

    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return reference;
    }
}