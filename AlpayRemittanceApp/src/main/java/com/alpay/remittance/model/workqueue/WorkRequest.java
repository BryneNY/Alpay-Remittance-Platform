package com.alpay.remittance.model.workqueue;

import com.alpay.remittance.model.transfer.Transfer;
import com.alpay.remittance.model.user.UserAccount;
import java.time.LocalDateTime;

/**
 * A piece of work passed from one user to another, always about one transfer.
 * Status values match the database: PENDING, APPROVED, REJECTED, RESOLVED.
 */
public abstract class WorkRequest {

    private int requestId;
        private int organizationId;      // the organization whose queue holds this request
    private UserAccount sender;
    private UserAccount receiver;
    private Transfer transfer;
    private String status;
    private String message;
    private LocalDateTime requestDate;
    private LocalDateTime resolveDate;

    public WorkRequest() {
        this.status = "PENDING";
        this.requestDate = LocalDateTime.now();
    }

    public void markResolved(UserAccount resolvedBy) {
        this.receiver = resolvedBy;
        this.status = "RESOLVED";
        this.resolveDate = LocalDateTime.now();
    }

    public abstract String getRequestType();

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
        public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }

    public UserAccount getSender() { return sender; }
    public void setSender(UserAccount sender) { this.sender = sender; }

    public UserAccount getReceiver() { return receiver; }
    public void setReceiver(UserAccount receiver) { this.receiver = receiver; }

    public Transfer getTransfer() { return transfer; }
    public void setTransfer(Transfer transfer) { this.transfer = transfer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getResolveDate() { return resolveDate; }
    public void setResolveDate(LocalDateTime resolveDate) { this.resolveDate = resolveDate; }

    @Override
    public String toString() {
        return getRequestType() + " #" + requestId;
    }
}