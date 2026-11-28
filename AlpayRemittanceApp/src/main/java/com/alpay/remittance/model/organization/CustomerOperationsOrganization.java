package com.alpay.remittance.model.organization;

import com.alpay.remittance.model.role.MTOOfficerRole;
import com.alpay.remittance.model.role.Role;
import java.util.List;

/** MTO department that verifies senders and sends payment instructions. */
public class CustomerOperationsOrganization extends Organization {

    public CustomerOperationsOrganization() {
        super("Customer Operations");
    }

    @Override
    public List<Role> getSupportedRoles() {
        return List.of(new MTOOfficerRole());
    }
}