package com.alpay.remittance.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * The four CRUD operations every DAO must provide.
 * T is the type of object the DAO handles, such as Beneficiary or Transfer.
 */
public interface CrudDAO<T> {

    void create(T item) throws SQLException;

    T read(int id) throws SQLException;

    List<T> readAll() throws SQLException;

    void update(T item) throws SQLException;

    void delete(int id) throws SQLException;
}