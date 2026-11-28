package com.alpay.remittance.model.role;

import com.alpay.remittance.model.ecosystem.EcoSystem;
import com.alpay.remittance.model.enterprise.Enterprise;
import com.alpay.remittance.model.organization.Organization;
import com.alpay.remittance.model.user.UserAccount;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ComplianceOfficerRole extends Role {

    @Override
    public JPanel createWorkArea(UserAccount account, Organization org,
                                 Enterprise enterprise, EcoSystem system) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Compliance Officer work area (coming soon)"));
        return panel;
    }

    @Override
    public String toString() {
        return "Compliance Officer";
    }
}