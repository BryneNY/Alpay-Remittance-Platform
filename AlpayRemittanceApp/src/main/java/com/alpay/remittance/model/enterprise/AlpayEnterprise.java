package com.alpay.remittance.model.enterprise;

import java.util.List;

/**
 * Alpay in Ghana: runs the platform, screens transactions and serves customers.
 */
public class AlpayEnterprise extends Enterprise {

    public AlpayEnterprise(String name) {
        super(name);
    }

    @Override
    public List<String> getSupportedOrganizations() {
        return List.of("COMPLIANCE_RISK", "CUSTOMER_SERVICE");
    }
}