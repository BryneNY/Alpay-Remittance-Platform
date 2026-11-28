package com.alpay.remittance.model.ecosystem;

import com.alpay.remittance.model.enterprise.Enterprise;
import com.alpay.remittance.model.enterprise.MTOEnterprise;
import com.alpay.remittance.model.organization.Organization;
import com.alpay.remittance.model.user.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The whole platform. Singleton: there is only ever one ecosystem.
 */
public class EcoSystem {

    private static EcoSystem instance;
    private List<Network> networks = new ArrayList<>();
    private List<UserAccount> systemAdmins = new ArrayList<>();

    private EcoSystem() {
    }

    public static synchronized EcoSystem getInstance() {
        if (instance == null) {
            instance = new EcoSystem();
        }
        return instance;
    }

    public Network createNetwork(String name) {
        Network network = new Network(name);
        networks.add(network);
        return network;
    }

    public Network findNetwork(String name) {
        for (Network network : networks) {
            if (network.getName().equalsIgnoreCase(name)) {
                return network;
            }
        }
        return null;
    }

    /**
     * Checks a username and password against every account in the ecosystem.
     * Returns the matching account, or null if the login is wrong.
     */
    public UserAccount authenticateUser(String username, String password) {
        String hash = hashPassword(password);
        for (UserAccount account : getAllUserAccounts()) {
            if (account.getUsername().equals(username) && account.getPassword().equals(hash)) {
                return account;
            }
        }
        return null;
    }

    private List<UserAccount> getAllUserAccounts() {
        List<UserAccount> all = new ArrayList<>(systemAdmins);
        for (Network network : networks) {
            for (Enterprise enterprise : network.getEnterprises()) {
                all.addAll(enterprise.getEnterpriseAdmins());
                if (enterprise instanceof MTOEnterprise mto) {
                    all.addAll(mto.getCustomers());
                }
                for (Organization org : enterprise.getOrganizations()) {
                    all.addAll(org.getUserAccounts());
                }
            }
        }
        return all;
    }

    /** SHA-256 hash in lowercase hex, matching MySQL's SHA2(password, 256). */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public List<Network> getNetworks() { return networks; }

    public List<UserAccount> getSystemAdmins() { return systemAdmins; }
}