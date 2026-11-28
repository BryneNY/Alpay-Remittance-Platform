package com.alpay.remittance.model.enterprise;

import com.alpay.remittance.model.user.UserAccount;
import java.util.ArrayList;
import java.util.List;

/**
 * The money transfer operator in Canada. Its customers are the senders.
 */
public class MTOEnterprise extends Enterprise {

    private List<UserAccount> customers = new ArrayList<>();

    public MTOEnterprise(String name) {
        super(name);
    }

    public void registerCustomer(UserAccount account) {
        customers.add(account);
    }

    public List<UserAccount> getCustomers() {
        return customers;
    }

    @Override
    public List<String> getSupportedOrganizations() {
        return List.of("CUSTOMER_OPERATIONS");
    }
}