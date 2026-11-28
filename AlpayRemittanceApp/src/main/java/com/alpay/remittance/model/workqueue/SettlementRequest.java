package com.alpay.remittance.model.workqueue;

/** Alpay → Partner Bank: convert the funds to cedis and settle. */
public class SettlementRequest extends WorkRequest {

    private double settledAmountGhs;

    public double getSettledAmountGhs() { return settledAmountGhs; }
    public void setSettledAmountGhs(double settledAmountGhs) { this.settledAmountGhs = settledAmountGhs; }

    @Override
    public String getRequestType() {
        return "Settlement Request";
    }
}