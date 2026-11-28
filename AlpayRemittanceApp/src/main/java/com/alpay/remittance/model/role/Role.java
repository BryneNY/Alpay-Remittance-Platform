package com.alpay.remittance.model.role;

import com.alpay.remittance.model.ecosystem.EcoSystem;
import com.alpay.remittance.model.enterprise.Enterprise;
import com.alpay.remittance.model.organization.Organization;
import com.alpay.remittance.model.user.UserAccount;
import javax.swing.JPanel;

/**
 * Every role builds its own work area screen. This is polymorphism: the login
 * code calls createWorkArea() without needing to know which role the user has.
 */
public abstract class Role {

    public abstract JPanel createWorkArea(UserAccount account, Organization org,
                                          Enterprise enterprise, EcoSystem system);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}