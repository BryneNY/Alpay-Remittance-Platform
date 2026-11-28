package com.alpay.remittance.dao;

import com.alpay.remittance.model.ecosystem.EcoSystem;
import com.alpay.remittance.model.role.BankOfficerRole;
import com.alpay.remittance.model.role.ComplianceOfficerRole;
import com.alpay.remittance.model.role.CustomerAgentRole;
import com.alpay.remittance.model.role.EnterpriseAdminRole;
import com.alpay.remittance.model.role.MTOOfficerRole;
import com.alpay.remittance.model.role.Role;
import com.alpay.remittance.model.role.SenderRole;
import com.alpay.remittance.model.role.SystemAdminRole;
import com.alpay.remittance.model.user.Person;
import com.alpay.remittance.model.user.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads user accounts. Each account is stored across two tables:
 * persons (personal details) and user_accounts (login details and role).
 * The password field always holds a SHA-256 hash, never plain text.
 */
public class UserAccountDAO implements CrudDAO<UserAccount> {

    private static final String SELECT_JOIN =
            "SELECT u.*, p.full_name, p.email, p.phone, p.country "
          + "FROM user_accounts u JOIN persons p ON u.person_id = p.person_id ";

    @Override
    public void create(UserAccount item) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        conn.setAutoCommit(false);   // start a transaction: both inserts succeed, or neither does
        try {
            Person person = item.getPerson();
            String personSql = "INSERT INTO persons (full_name, email, phone, country) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, person.getFullName());
                ps.setString(2, person.getEmail());
                ps.setString(3, person.getPhone());
                ps.setString(4, person.getCountry());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    person.setPersonId(keys.getInt(1));
                }
            }

            String accountSql = "INSERT INTO user_accounts (username, password_hash, role, person_id, "
                              + "organization_id, enterprise_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(accountSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, item.getUsername());
                ps.setString(2, item.getPassword());
                ps.setString(3, codeFromRole(item.getRole()));
                ps.setInt(4, person.getPersonId());
                setNullableInt(ps, 5, item.getOrganizationId());
                setNullableInt(ps, 6, item.getEnterpriseId());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    item.setAccountId(keys.getInt(1));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();          // undo the person insert if the account insert failed
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public UserAccount read(int id) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE u.account_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    @Override
    public List<UserAccount> readAll() throws SQLException {
        List<UserAccount> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "ORDER BY u.username");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(fromRow(rs));
            }
        }
        return list;
    }

    @Override
    public void update(UserAccount item) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        conn.setAutoCommit(false);
        try {
            Person person = item.getPerson();
            String personSql = "UPDATE persons SET full_name = ?, email = ?, phone = ?, country = ? WHERE person_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(personSql)) {
                ps.setString(1, person.getFullName());
                ps.setString(2, person.getEmail());
                ps.setString(3, person.getPhone());
                ps.setString(4, person.getCountry());
                ps.setInt(5, person.getPersonId());
                ps.executeUpdate();
            }

            String accountSql = "UPDATE user_accounts SET username = ?, password_hash = ?, role = ?, "
                              + "organization_id = ?, enterprise_id = ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(accountSql)) {
                ps.setString(1, item.getUsername());
                ps.setString(2, item.getPassword());
                ps.setString(3, codeFromRole(item.getRole()));
                setNullableInt(ps, 4, item.getOrganizationId());
                setNullableInt(ps, 5, item.getEnterpriseId());
                ps.setInt(6, item.getAccountId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Deleting the person also deletes their account (ON DELETE CASCADE in the schema). */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE p FROM persons p JOIN user_accounts u ON u.person_id = p.person_id "
                   + "WHERE u.account_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Login check: returns the account if the username and password match, otherwise null.
     * The typed password is hashed first, then compared with the stored hash.
     */
    public UserAccount authenticate(String username, String plainPassword) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT_JOIN + "WHERE u.username = ? AND u.password_hash = ?")) {
            ps.setString(1, username);
            ps.setString(2, EcoSystem.hashPassword(plainPassword));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        }
    }

    private UserAccount fromRow(ResultSet rs) throws SQLException {
        Person person = new Person(rs.getInt("person_id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("phone"), rs.getString("country"));
        UserAccount account = new UserAccount(rs.getInt("account_id"), rs.getString("username"),
                rs.getString("password_hash"), roleFromCode(rs.getString("role")), person);
        account.setOrganizationId(rs.getObject("organization_id", Integer.class));
        account.setEnterpriseId(rs.getObject("enterprise_id", Integer.class));
        return account;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /** Turns the role code stored in the database into the matching Role object. */
    static Role roleFromCode(String code) {
        return switch (code) {
            case "SENDER" -> new SenderRole();
            case "MTO_OFFICER" -> new MTOOfficerRole();
            case "COMPLIANCE_OFFICER" -> new ComplianceOfficerRole();
            case "CUSTOMER_AGENT" -> new CustomerAgentRole();
            case "BANK_OFFICER" -> new BankOfficerRole();
            case "ENTERPRISE_ADMIN" -> new EnterpriseAdminRole();
            case "SYSTEM_ADMIN" -> new SystemAdminRole();
            default -> throw new IllegalArgumentException("Unknown role: " + code);
        };
    }

    /** Turns a Role object into the code stored in the database. */
    static String codeFromRole(Role role) {
        if (role instanceof SenderRole) return "SENDER";
        if (role instanceof MTOOfficerRole) return "MTO_OFFICER";
        if (role instanceof ComplianceOfficerRole) return "COMPLIANCE_OFFICER";
        if (role instanceof CustomerAgentRole) return "CUSTOMER_AGENT";
        if (role instanceof BankOfficerRole) return "BANK_OFFICER";
        if (role instanceof EnterpriseAdminRole) return "ENTERPRISE_ADMIN";
        if (role instanceof SystemAdminRole) return "SYSTEM_ADMIN";
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}