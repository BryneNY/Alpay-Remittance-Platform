package com.alpay.remittance.model.organization;

import com.alpay.remittance.model.role.CustomerAgentRole;
import com.alpay.remittance.model.role.Role;
import java.util.List;

/** Alpay department that answers sender queries. */
public class CustomerServiceOrganization extends Organization {

    public CustomerServiceOrganization() {
        super("Customer Service");
    }

    @Override
    public List<Role> getSupportedRoles() {
        return List.of(new CustomerAgentRole());
    }
}