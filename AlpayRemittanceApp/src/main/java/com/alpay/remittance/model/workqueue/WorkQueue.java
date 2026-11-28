package com.alpay.remittance.model.workqueue;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of work requests waiting in one organization.
 */
public class WorkQueue {

    private List<WorkRequest> workRequests = new ArrayList<>();

    public void addRequest(WorkRequest request) {
        workRequests.add(request);
    }

    public void removeRequest(WorkRequest request) {
        workRequests.remove(request);
    }

    public List<WorkRequest> getRequestsByStatus(String status) {
        List<WorkRequest> matches = new ArrayList<>();
        for (WorkRequest request : workRequests) {
            if (request.getStatus().equals(status)) {
                matches.add(request);
            }
        }
        return matches;
    }

    public List<WorkRequest> getWorkRequests() {
        return workRequests;
    }
}