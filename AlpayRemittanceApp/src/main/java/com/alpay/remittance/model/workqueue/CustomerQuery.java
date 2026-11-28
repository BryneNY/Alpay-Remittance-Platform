package com.alpay.remittance.model.workqueue;

/** Sender → Alpay Customer Agent: a question about a transfer. */
public class CustomerQuery extends WorkRequest {

    private String question;
    private String response;

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    @Override
    public String getRequestType() {
        return "Customer Query";
    }
}