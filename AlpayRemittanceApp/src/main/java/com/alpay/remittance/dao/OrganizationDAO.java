package com.alpay.remittance.dao;

import com.alpay.remittance.model.organization.ComplianceRiskOrganization;
import com.alpay.remittance.model.organization.CustomerOperationsOrganization;
import com.alpay.remittance.model.organization.CustomerServiceOrganization;
import com.alpay.remittance.model.organization.Organization;
import com.alpay.remittance.model.organization.SettlementPayoutsOrganization;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads the organizations of one enterprise.
 * Used by Enterprise Admins to manage their organizations.
 */
public class OrganizationDAO implements CrudDAO<Organization> {

    private final int enterpriseId;

    public OrganizationDAO(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    @Override
    public void create(Organization item) throws SQLException {
        String sql = "INSERT INTO organizations (enterprise_id, name, type) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enterpriseId);
            ps.setString(2, item.getName());
            ps.setString(3, typeOf(item));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setOrganizationId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public Organization read(int id) throws SQLException {
        String sql = "SELECT * FROM organizations WHERE organization_id = ? AND enterprise_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, enterpriseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    @Override
    public List<Organization> readAll() throws SQLException {
        String sql = "SELECT * FROM organizations WHERE enterprise_id = ? ORDER BY name";
        List<Organization> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enterpriseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                }
            }
        }
        return list;
    }

    /** Only the name can change; an organization's type is fixed when it is created. */
    @Override
    public void update(Organization item) throws SQLException {
        String sql = "UPDATE organizations SET name = ? WHERE organization_id = ? AND enterprise_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getOrganizationId());
            ps.setInt(3, enterpriseId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM organizations WHERE organization_id = ? AND enterprise_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, enterpriseId);
            ps.executeUpdate();
        }
    }

    private Organization fromRow(ResultSet rs) throws SQLException {
        Organization org = switch (rs.getString("type")) {
            case "CUSTOMER_OPERATIONS" -> new CustomerOperationsOrganization();
            case "COMPLIANCE_RISK" -> new ComplianceRiskOrganization();
            case "CUSTOMER_SERVICE" -> new CustomerServiceOrganization();
            case "SETTLEMENT_PAYOUTS" -> new SettlementPayoutsOrganization();
            default -> throw new SQLException("Unknown organization type: " + rs.getString("type"));
        };
        org.setOrganizationId(rs.getInt("organization_id"));
        org.setName(rs.getString("name"));
        return org;
    }

    private String typeOf(Organization org) {
        if (org instanceof CustomerOperationsOrganization) return "CUSTOMER_OPERATIONS";
        if (org instanceof ComplianceRiskOrganization) return "COMPLIANCE_RISK";
        if (org instanceof CustomerServiceOrganization) return "CUSTOMER_SERVICE";
        if (org instanceof SettlementPayoutsOrganization) return "SETTLEMENT_PAYOUTS";
        throw new IllegalArgumentException("Unknown organization: " + org);
    }
}