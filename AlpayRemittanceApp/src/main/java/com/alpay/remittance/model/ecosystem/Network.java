package com.alpay.remittance.model.ecosystem;

import com.alpay.remittance.model.enterprise.AlpayEnterprise;
import com.alpay.remittance.model.enterprise.Enterprise;
import com.alpay.remittance.model.enterprise.MTOEnterprise;
import com.alpay.remittance.model.enterprise.PartnerBankEnterprise;
import java.util.ArrayList;
import java.util.List;

/**
 * A country network (Canada or Ghana) containing enterprises.
 * Enterprise types match the enterprises table: MTO, ALPAY, PARTNER_BANK.
 */
public class Network {

    private int networkId;
    private String name;
    private List<Enterprise> enterprises = new ArrayList<>();

    public Network(String name) {
        this.name = name;
    }

    public Enterprise createEnterprise(String name, String type) {
        Enterprise enterprise = switch (type) {
            case "MTO" -> new MTOEnterprise(name);
            case "ALPAY" -> new AlpayEnterprise(name);
            case "PARTNER_BANK" -> new PartnerBankEnterprise(name);
            default -> throw new IllegalArgumentException("Unknown enterprise type: " + type);
        };
        enterprises.add(enterprise);
        return enterprise;
    }

    public void deleteEnterprise(Enterprise enterprise) {
        enterprises.remove(enterprise);
    }

    public Enterprise findEnterprise(String name) {
        for (Enterprise enterprise : enterprises) {
            if (enterprise.getName().equalsIgnoreCase(name)) {
                return enterprise;
            }
        }
        return null;
    }

    public int getNetworkId() { return networkId; }
    public void setNetworkId(int networkId) { this.networkId = networkId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Enterprise> getEnterprises() { return enterprises; }

    @Override
    public String toString() {
        return name;
    }
}