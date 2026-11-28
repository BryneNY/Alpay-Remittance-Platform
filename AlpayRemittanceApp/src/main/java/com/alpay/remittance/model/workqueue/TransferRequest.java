package com.alpay.remittance.model.workqueue;

/** Sender → MTO Officer: verify the sender and the payment. */
public class TransferRequest extends WorkRequest {

    private String verificationNotes;

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    @Override
    public String getRequestType() {
        return "Transfer Request";
    }
}