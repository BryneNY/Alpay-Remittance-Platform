package com.alpay.remittance.dao;

import com.alpay.remittance.model.transfer.Beneficiary;
import com.alpay.remittance.model.transfer.PayoutMethod;
import com.alpay.remittance.model.transfer.Transfer;
import com.alpay.remittance.model.transfer.TransferStatus;
import com.alpay.remittance.model.user.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads transfers. Each transfer is loaded together with its
 * beneficiary and the sender's username, using a single JOIN query.
 */
public class TransferDAO implements CrudDAO<Transfer> {

    private static final String SELECT_JOIN =
            "SELECT t.*, u.username, b.full_name AS b_name, b.phone AS b_phone, "
          + "b.payout_method, b.wallet_number, b.bank_name, b.account_number "
          + "FROM transfers t "
          + "JOIN user_accounts u ON t.sender_account_id = u.account_id "
          + "JOIN beneficiaries b ON t.beneficiary_id = b.beneficiary_id ";

    @Override
    public void create(Transfer item) throws SQLException {
        String sql = "INSERT INTO transfers (reference, sender_account_id, beneficiary_id, amount_cad, "
                   + "fee, exchange_rate, amount_ghs, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getReference());
            ps.setInt(2, item.getSender().getAccountId());
            ps.setInt(3, item.getBeneficiary().getBeneficiaryId());
            ps.setDouble(4, item.getAmountCad());
            ps.setDouble(5, item.getFee());
            ps.setDouble(6, item.getExchangeRate());
            ps.setDouble(7, item.getAmountGhs());
            ps.setString(8, item.getStatus().name());
            ps.setTimestamp(9, Timestamp.valueOf(item.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setTransferId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public Transfer read(int id) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE t.transfer_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    /** Every transfer, newest first. Used by the reconciliation report. */
    @Override
    public List<Transfer> readAll() throws SQLException {
        return query(SELECT_JOIN + "ORDER BY t.created_at DESC", null);
    }

    /** One sender's transfer history, newest first. */
    public List<Transfer> readBySender(int senderAccountId) throws SQLException {
        return query(SELECT_JOIN + "WHERE t.sender_account_id = ? ORDER BY t.created_at DESC", senderAccountId);
    }

    @Override
    public void update(Transfer item) throws SQLException {
        String sql = "UPDATE transfers SET beneficiary_id = ?, amount_cad = ?, fee = ?, exchange_rate = ?, "
                   + "amount_ghs = ?, status = ? WHERE transfer_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getBeneficiary().getBeneficiaryId());
            ps.setDouble(2, item.getAmountCad());
            ps.setDouble(3, item.getFee());
            ps.setDouble(4, item.getExchangeRate());
            ps.setDouble(5, item.getAmountGhs());
            ps.setString(6, item.getStatus().name());
            ps.setInt(7, item.getTransferId());
            ps.executeUpdate();
        }
    }

    /** Saves a new status, used at each approval step. */
    public void updateStatus(int transferId, TransferStatus status) throws SQLException {
        String sql = "UPDATE transfers SET status = ? WHERE transfer_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, transferId);
            ps.executeUpdate();
        }
    }

    /** Records who reconciled a paid transfer and when. */
    public void markReconciled(int transferId, int reconciledByAccountId) throws SQLException {
        String sql = "UPDATE transfers SET status = 'RECONCILED', reconciled_by = ?, reconciled_at = NOW() "
                   + "WHERE transfer_id = ? AND status = 'PAID'";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reconciledByAccountId);
            ps.setInt(2, transferId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Only paid transfers can be reconciled.");
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transfers WHERE transfer_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Transfer> query(String sql, Integer param) throws SQLException {
        List<Transfer> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                }
            }
        }
        return list;
    }

    private Transfer fromRow(ResultSet rs) throws SQLException {
        UserAccount sender = new UserAccount();
        sender.setAccountId(rs.getInt("sender_account_id"));
        sender.setUsername(rs.getString("username"));

        Beneficiary beneficiary = new Beneficiary(rs.getInt("beneficiary_id"), rs.getString("b_name"),
                rs.getString("b_phone"), PayoutMethod.valueOf(rs.getString("payout_method")),
                rs.getString("wallet_number"), rs.getString("bank_name"), rs.getString("account_number"));

        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setReference(rs.getString("reference"));
        transfer.setSender(sender);
        transfer.setBeneficiary(beneficiary);
        transfer.setAmountCad(rs.getDouble("amount_cad"));
        transfer.setFee(rs.getDouble("fee"));
        transfer.setExchangeRate(rs.getDouble("exchange_rate"));
        transfer.setAmountGhs(rs.getDouble("amount_ghs"));
        transfer.setStatus(TransferStatus.valueOf(rs.getString("status")));
        transfer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return transfer;
    }
}