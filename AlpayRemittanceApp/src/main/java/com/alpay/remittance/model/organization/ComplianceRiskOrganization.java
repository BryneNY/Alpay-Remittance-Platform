package com.alpay.remittance.model.organization;

import com.alpay.remittance.model.role.ComplianceOfficerRole;
import com.alpay.remittance.model.role.Role;
import java.util.List;

/** Alpay department that screens transfers and requests settlement. */
public class ComplianceRiskOrganization extends Organization {

    public ComplianceRiskOrganization() {
        super("Compliance and Risk");
    }

    @Override
    public List<Role> getSupportedRoles() {
        return List.of(new ComplianceOfficerRole());
    }
}