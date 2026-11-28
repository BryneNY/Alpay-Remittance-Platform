package com.alpay.remittance.model.transfer;

/**
 * Someone in Ghana who receives money from a sender.
 * A MoMo beneficiary uses walletNumber; a bank beneficiary uses bankName and accountNumber.
 */
public class Beneficiary {

    private int beneficiaryId;
    private String fullName;
    private String phone;
    private PayoutMethod payoutMethod;
    private String walletNumber;
    private String bankName;
    private String accountNumber;

    public Beneficiary() {
    }

    public Beneficiary(int beneficiaryId, String fullName, String phone, PayoutMethod payoutMethod,
                       String walletNumber, String bankName, String accountNumber) {
        this.beneficiaryId = beneficiaryId;
        this.fullName = fullName;
        this.phone = phone;
        this.payoutMethod = payoutMethod;
        this.walletNumber = walletNumber;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    public int getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(int beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public PayoutMethod getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(PayoutMethod payoutMethod) { this.payoutMethod = payoutMethod; }

    public String getWalletNumber() { return walletNumber; }
    public void setWalletNumber(String walletNumber) { this.walletNumber = walletNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    @Override
    public String toString() {
        return fullName + " (" + payoutMethod + ")";
    }
}