package com.alpay.remittance.model.organization;

import com.alpay.remittance.model.role.Role;
import com.alpay.remittance.model.user.Person;
import com.alpay.remittance.model.user.UserAccount;
import com.alpay.remittance.model.workqueue.WorkQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * A department inside an enterprise, with its own staff and work queue.
 */
public abstract class Organization {

    private int organizationId;
    private String name;
    private WorkQueue workQueue = new WorkQueue();
    private List<UserAccount> userAccounts = new ArrayList<>();

    public Organization(String name) {
        this.name = name;
    }

    public UserAccount createUserAccount(String username, String password, Person person, Role role) {
        UserAccount account = new UserAccount(0, username, password, role, person);
        userAccounts.add(account);
        return account;
    }

    public void deleteUserAccount(UserAccount account) {
        userAccounts.remove(account);
    }

    /** The roles that staff in this organization can have. */
    public abstract List<Role> getSupportedRoles();

    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WorkQueue getWorkQueue() { return workQueue; }

    public List<UserAccount> getUserAccounts() { return userAccounts; }

    @Override
    public String toString() {
        return name;
    }
}