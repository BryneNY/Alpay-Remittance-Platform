package com.alpay.remittance.dao;

import com.alpay.remittance.model.transfer.Beneficiary;
import com.alpay.remittance.model.transfer.PayoutMethod;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads one sender's beneficiaries in the beneficiaries table.
 */
public class BeneficiaryDAO implements CrudDAO<Beneficiary> {

    private final int senderAccountId;

    /** Each sender only ever sees and changes their own beneficiaries. */
    public BeneficiaryDAO(int senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    @Override
    public void create(Beneficiary item) throws SQLException {
        String sql = "INSERT INTO beneficiaries (sender_account_id, full_name, phone, payout_method, "
                   + "wallet_number, bank_name, account_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, senderAccountId);
            fillDetails(ps, item, 2);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setBeneficiaryId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public Beneficiary read(int id) throws SQLException {
        String sql = "SELECT * FROM beneficiaries WHERE beneficiary_id = ? AND sender_account_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, senderAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    @Override
    public List<Beneficiary> readAll() throws SQLException {
        String sql = "SELECT * FROM beneficiaries WHERE sender_account_id = ? ORDER BY full_name";
        List<Beneficiary> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void update(Beneficiary item) throws SQLException {
        String sql = "UPDATE beneficiaries SET full_name = ?, phone = ?, payout_method = ?, "
                   + "wallet_number = ?, bank_name = ?, account_number = ? "
                   + "WHERE beneficiary_id = ? AND sender_account_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            fillDetails(ps, item, 1);
            ps.setInt(7, item.getBeneficiaryId());
            ps.setInt(8, senderAccountId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM beneficiaries WHERE beneficiary_id = ? AND sender_account_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, senderAccountId);
            ps.executeUpdate();
        }
    }

    /** Puts the six detail fields into the statement, starting at the given position. */
    private void fillDetails(PreparedStatement ps, Beneficiary item, int start) throws SQLException {
        ps.setString(start, item.getFullName());
        ps.setString(start + 1, item.getPhone());
        ps.setString(start + 2, item.getPayoutMethod().name());
        ps.setString(start + 3, item.getWalletNumber());
        ps.setString(start + 4, item.getBankName());
        ps.setString(start + 5, item.getAccountNumber());
    }

    /** Turns one database row into a Beneficiary object. */
    private Beneficiary fromRow(ResultSet rs) throws SQLException {
        return new Beneficiary(
                rs.getInt("beneficiary_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                PayoutMethod.valueOf(rs.getString("payout_method")),
                rs.getString("wallet_number"),
                rs.getString("bank_name"),
                rs.getString("account_number"));
    }
}