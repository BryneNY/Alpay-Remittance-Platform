package com.alpay.remittance.model.enterprise;

import com.alpay.remittance.model.organization.ComplianceRiskOrganization;
import com.alpay.remittance.model.organization.CustomerOperationsOrganization;
import com.alpay.remittance.model.organization.CustomerServiceOrganization;
import com.alpay.remittance.model.organization.Organization;
import com.alpay.remittance.model.organization.SettlementPayoutsOrganization;
import com.alpay.remittance.model.user.UserAccount;
import java.util.ArrayList;
import java.util.List;

/**
 * A company in the ecosystem: the MTO, Alpay or the Partner Bank.
 * Organization types match the organizations table: CUSTOMER_OPERATIONS,
 * COMPLIANCE_RISK, CUSTOMER_SERVICE, SETTLEMENT_PAYOUTS.
 */
public abstract class Enterprise {

    private int enterpriseId;
    private String name;
    private List<Organization> organizations = new ArrayList<>();
    private List<UserAccount> enterpriseAdmins = new ArrayList<>();

    public Enterprise(String name) {
        this.name = name;
    }

    public Organization createOrganization(String type) {
        if (!getSupportedOrganizations().contains(type)) {
            throw new IllegalArgumentException(name + " does not support organization type " + type);
        }
        Organization organization = switch (type) {
            case "CUSTOMER_OPERATIONS" -> new CustomerOperationsOrganization();
            case "COMPLIANCE_RISK" -> new ComplianceRiskOrganization();
            case "CUSTOMER_SERVICE" -> new CustomerServiceOrganization();
            case "SETTLEMENT_PAYOUTS" -> new SettlementPayoutsOrganization();
            default -> throw new IllegalArgumentException("Unknown organization type: " + type);
        };
        organizations.add(organization);
        return organization;
    }

    public void deleteOrganization(Organization org) {
        organizations.remove(org);
    }

    /** The organization types this kind of enterprise can contain. */
    public abstract List<String> getSupportedOrganizations();

    public int getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(int enterpriseId) { this.enterpriseId = enterpriseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Organization> getOrganizations() { return organizations; }

    public List<UserAccount> getEnterpriseAdmins() { return enterpriseAdmins; }

    @Override
    public String toString() {
        return name;
    }
}