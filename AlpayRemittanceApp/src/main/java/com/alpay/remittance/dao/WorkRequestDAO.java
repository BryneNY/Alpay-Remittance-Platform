package com.alpay.remittance.dao;

import com.alpay.remittance.model.user.UserAccount;
import com.alpay.remittance.model.workqueue.CustomerQuery;
import com.alpay.remittance.model.workqueue.PaymentInstruction;
import com.alpay.remittance.model.workqueue.SettlementRequest;
import com.alpay.remittance.model.workqueue.TransferRequest;
import com.alpay.remittance.model.workqueue.WorkRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads all four kinds of work request in the work_requests table.
 * The request_type column says which subclass each row belongs to.
 */
public class WorkRequestDAO implements CrudDAO<WorkRequest> {

    private static final String SELECT_JOIN =
            "SELECT w.*, s.username AS sender_name, r.username AS receiver_name "
          + "FROM work_requests w "
          + "JOIN user_accounts s ON w.sender_account_id = s.account_id "
          + "LEFT JOIN user_accounts r ON w.receiver_account_id = r.account_id ";

    private final TransferDAO transferDAO = new TransferDAO();

    @Override
    public void create(WorkRequest item) throws SQLException {
        String sql = "INSERT INTO work_requests (request_type, transfer_id, organization_id, "
                   + "sender_account_id, receiver_account_id, status, message, notes, "
                   + "settled_amount_ghs, question, response, request_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, typeCodeOf(item));
            ps.setInt(2, item.getTransfer().getTransferId());
            ps.setInt(3, item.getOrganizationId());
            ps.setInt(4, item.getSender().getAccountId());
            setReceiver(ps, 5, item.getReceiver());
            ps.setString(6, item.getStatus());
            ps.setString(7, item.getMessage());
            ps.setString(8, notesOf(item));
            setSettledAmount(ps, 9, item);
            ps.setString(10, item instanceof CustomerQuery q ? q.getQuestion() : null);
            ps.setString(11, item instanceof CustomerQuery q ? q.getResponse() : null);
            ps.setTimestamp(12, Timestamp.valueOf(item.getRequestDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setRequestId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public WorkRequest read(int id) throws SQLException {
        List<WorkRequest> found = query(SELECT_JOIN + "WHERE w.request_id = ?", id);
        return found.isEmpty() ? null : found.get(0);
    }

    @Override
    public List<WorkRequest> readAll() throws SQLException {
        return query(SELECT_JOIN + "ORDER BY w.request_date DESC", null);
    }

    /** The work queue of one organization, newest first. */
    public List<WorkRequest> readByOrganization(int organizationId) throws SQLException {
        return query(SELECT_JOIN + "WHERE w.organization_id = ? ORDER BY w.request_date DESC", organizationId);
    }

    /** Everything one user has sent, such as a sender's customer queries. */
    public List<WorkRequest> readBySender(int senderAccountId) throws SQLException {
        return query(SELECT_JOIN + "WHERE w.sender_account_id = ? ORDER BY w.request_date DESC", senderAccountId);
    }

    /** The full history of one transfer: every request it has passed through. */
    public List<WorkRequest> readByTransfer(int transferId) throws SQLException {
        return query(SELECT_JOIN + "WHERE w.transfer_id = ? ORDER BY w.request_date", transferId);
    }

    /** Saves who handled the request, its new status, notes and any response. */
    @Override
    public void update(WorkRequest item) throws SQLException {
        String sql = "UPDATE work_requests SET receiver_account_id = ?, status = ?, message = ?, "
                   + "notes = ?, settled_amount_ghs = ?, response = ?, resolve_date = ? "
                   + "WHERE request_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setReceiver(ps, 1, item.getReceiver());
            ps.setString(2, item.getStatus());
            ps.setString(3, item.getMessage());
            ps.setString(4, notesOf(item));
            setSettledAmount(ps, 5, item);
            ps.setString(6, item instanceof CustomerQuery q ? q.getResponse() : null);
            ps.setTimestamp(7, item.getResolveDate() == null ? null : Timestamp.valueOf(item.getResolveDate()));
            ps.setInt(8, item.getRequestId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM work_requests WHERE request_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<WorkRequest> query(String sql, Integer param) throws SQLException {
        List<WorkRequest> list = new ArrayList<>();
        List<Integer> transferIds = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                    transferIds.add(rs.getInt("transfer_id"));
                }
            }
        }
        // Load each request's transfer after the first query has finished
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setTransfer(transferDAO.read(transferIds.get(i)));
        }
        return list;
    }

    /** Builds the right subclass from the request_type column. */
    private WorkRequest fromRow(ResultSet rs) throws SQLException {
        String type = rs.getString("request_type");
        WorkRequest request;
        switch (type) {
            case "TRANSFER_REQUEST" -> {
                TransferRequest r = new TransferRequest();
                r.setVerificationNotes(rs.getString("notes"));
                request = r;
            }
            case "PAYMENT_INSTRUCTION" -> {
                PaymentInstruction r = new PaymentInstruction();
                r.setScreeningNotes(rs.getString("notes"));
                request = r;
            }
            case "SETTLEMENT_REQUEST" -> {
                SettlementRequest r = new SettlementRequest();
                r.setSettledAmountGhs(rs.getDouble("settled_amount_ghs"));
                request = r;
            }
            case "CUSTOMER_QUERY" -> {
                CustomerQuery r = new CustomerQuery();
                r.setQuestion(rs.getString("question"));
                r.setResponse(rs.getString("response"));
                request = r;
            }
            default -> throw new SQLException("Unknown request type: " + type);
        }

        request.setRequestId(rs.getInt("request_id"));
        request.setOrganizationId(rs.getInt("organization_id"));
        request.setStatus(rs.getString("status"));
        request.setMessage(rs.getString("message"));
        request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
        Timestamp resolved = rs.getTimestamp("resolve_date");
        request.setResolveDate(resolved == null ? null : resolved.toLocalDateTime());

        UserAccount sender = new UserAccount();
        sender.setAccountId(rs.getInt("sender_account_id"));
        sender.setUsername(rs.getString("sender_name"));
        request.setSender(sender);

        Integer receiverId = rs.getObject("receiver_account_id", Integer.class);
        if (receiverId != null) {
            UserAccount receiver = new UserAccount();
            receiver.setAccountId(receiverId);
            receiver.setUsername(rs.getString("receiver_name"));
            request.setReceiver(receiver);
        }
        return request;
    }

    private String typeCodeOf(WorkRequest item) {
        if (item instanceof TransferRequest) return "TRANSFER_REQUEST";
        if (item instanceof PaymentInstruction) return "PAYMENT_INSTRUCTION";
        if (item instanceof SettlementRequest) return "SETTLEMENT_REQUEST";
        if (item instanceof CustomerQuery) return "CUSTOMER_QUERY";
        throw new IllegalArgumentException("Unknown request type: " + item);
    }

    private String notesOf(WorkRequest item) {
        if (item instanceof TransferRequest r) return r.getVerificationNotes();
        if (item instanceof PaymentInstruction r) return r.getScreeningNotes();
        return null;
    }

    private void setSettledAmount(PreparedStatement ps, int index, WorkRequest item) throws SQLException {
        if (item instanceof SettlementRequest r && r.getSettledAmountGhs() > 0) {
            ps.setDouble(index, r.getSettledAmountGhs());
        } else {
            ps.setNull(index, Types.DECIMAL);
        }
    }

    private void setReceiver(PreparedStatement ps, int index, UserAccount receiver) throws SQLException {
        if (receiver == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, receiver.getAccountId());
        }
    }
}