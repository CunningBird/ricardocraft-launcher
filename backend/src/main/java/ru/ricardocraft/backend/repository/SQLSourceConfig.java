package ru.ricardocraft.backend.repository;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLSourceConfig {
    Connection getConnection() throws SQLException;

    void close();
}
