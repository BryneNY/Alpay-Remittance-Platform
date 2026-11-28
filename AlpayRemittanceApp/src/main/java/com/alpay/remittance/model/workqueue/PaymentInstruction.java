package com.alpay.remittance.model.workqueue;

/** MTO → Alpay Compliance Officer: screen the transfer before payout. */
public class PaymentInstruction extends WorkRequest {

    private String screeningNotes;

    public String getScreeningNotes() { return screeningNotes; }
    public void setScreeningNotes(String screeningNotes) { this.screeningNotes = screeningNotes; }

    @Override
    public String getRequestType() {
        return "Payment Instruction";
    }
}