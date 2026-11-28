package com.alpay.remittance.model.user;

import com.alpay.remittance.model.role.Role;
import com.alpay.remittance.model.transfer.Beneficiary;
import java.util.ArrayList;
import java.util.List;

/**
 * Login account for any user. The password field holds a SHA-256 hash, never plain text.
 */
public class UserAccount {

    private int accountId;
    private String username;
    private String password;
    private Role role;
    private Person person;
    private List<Beneficiary> beneficiaries = new ArrayList<>();

    public UserAccount() {
    }

    public UserAccount(int accountId, String username, String password, Role role, Person person) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.person = person;
    }

    public void addBeneficiary(Beneficiary beneficiary) {
        beneficiaries.add(beneficiary);
    }

    public void removeBeneficiary(Beneficiary beneficiary) {
        beneficiaries.remove(beneficiary);
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }

    public List<Beneficiary> getBeneficiaries() { return beneficiaries; }

    @Override
    public String toString() {
        return username;
    }
}