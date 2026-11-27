package com.alpay.remittance;

import com.alpay.remittance.dao.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AlpayRemittanceApp {

    public static void main(String[] args) {
        try (Statement statement = DBConnection.getInstance().getConnection().createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM user_accounts")) {
            result.next();
            System.out.println("Connected to Docker MySQL. User accounts found: " + result.getInt(1));
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}