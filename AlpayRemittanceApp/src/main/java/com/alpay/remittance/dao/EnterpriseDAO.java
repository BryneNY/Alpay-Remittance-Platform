package com.alpay.remittance.dao;

import com.alpay.remittance.model.enterprise.AlpayEnterprise;
import com.alpay.remittance.model.enterprise.Enterprise;
import com.alpay.remittance.model.enterprise.MTOEnterprise;
import com.alpay.remittance.model.enterprise.PartnerBankEnterprise;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads the enterprises of one network (Canada or Ghana).
 * Used by the System Admin to manage enterprises.
 */
public class EnterpriseDAO implements CrudDAO<Enterprise> {

    private final int networkId;

    public EnterpriseDAO(int networkId) {
        this.networkId = networkId;
    }

    @Override
    public void create(Enterprise item) throws SQLException {
        String sql = "INSERT INTO enterprises (network_id, name, type) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, networkId);
            ps.setString(2, item.getName());
            ps.setString(3, typeOf(item));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setEnterpriseId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public Enterprise read(int id) throws SQLException {
        String sql = "SELECT * FROM enterprises WHERE enterprise_id = ? AND network_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, networkId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    @Override
    public List<Enterprise> readAll() throws SQLException {
        String sql = "SELECT * FROM enterprises WHERE network_id = ? ORDER BY name";
        List<Enterprise> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, networkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                }
            }
        }
        return list;
    }

    /** Only the name can change; an enterprise's type is fixed when it is created. */
    @Override
    public void update(Enterprise item) throws SQLException {
        String sql = "UPDATE enterprises SET name = ? WHERE enterprise_id = ? AND network_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getEnterpriseId());
            ps.setInt(3, networkId);
            ps.executeUpdate();
        }
    }

    /** Also deletes the enterprise's organizations (ON DELETE CASCADE). */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM enterprises WHERE enterprise_id = ? AND network_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, networkId);
            ps.executeUpdate();
        }
    }

    private Enterprise fromRow(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        Enterprise enterprise = switch (rs.getString("type")) {
            case "MTO" -> new MTOEnterprise(name);
            case "ALPAY" -> new AlpayEnterprise(name);
            case "PARTNER_BANK" -> new PartnerBankEnterprise(name);
            default -> throw new SQLException("Unknown enterprise type: " + rs.getString("type"));
        };
        enterprise.setEnterpriseId(rs.getInt("enterprise_id"));
        return enterprise;
    }

    private String typeOf(Enterprise enterprise) {
        if (enterprise instanceof MTOEnterprise) return "MTO";
        if (enterprise instanceof AlpayEnterprise) return "ALPAY";
        if (enterprise instanceof PartnerBankEnterprise) return "PARTNER_BANK";
        throw new IllegalArgumentException("Unknown enterprise: " + enterprise);
    }
}