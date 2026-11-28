package com.alpay.remittance.model.organization;

import com.alpay.remittance.model.role.BankOfficerRole;
import com.alpay.remittance.model.role.Role;
import java.util.List;

/** Partner Bank department that converts funds and pays bank accounts. */
public class SettlementPayoutsOrganization extends Organization {

    public SettlementPayoutsOrganization() {
        super("Settlement and Payouts");
    }

    @Override
    public List<Role> getSupportedRoles() {
        return List.of(new BankOfficerRole());
    }
}