package com.alpay.remittance.model.enterprise;

import java.util.List;

/**
 * The partner bank in Ghana: converts funds to cedis and pays bank accounts.
 */
public class PartnerBankEnterprise extends Enterprise {

    public PartnerBankEnterprise(String name) {
        super(name);
    }

    @Override
    public List<String> getSupportedOrganizations() {
        return List.of("SETTLEMENT_PAYOUTS");
    }
}