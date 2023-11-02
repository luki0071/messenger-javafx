package com.kwasheniak.database;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Log4j2
public class DatabaseService {

    public static final String DATABASE = "jdbc:mysql://localhost:3306/messengerjfx";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    @Getter
    private static Connection connection;

    public static void establishConnection() throws SQLException {
        if (isConnectionAvailable()) {
            log.info("database is already connected");
            return;
        }
        connection = DriverManager.getConnection(DATABASE, USER, PASSWORD);
        log.info("connected to database: " + DATABASE);
    }

    public static Boolean isConnectionAvailable() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public static void closeConnection() throws SQLException {
        if (!isConnectionAvailable()) {
            log.info("cannot close connection with database because connection is null");
            return;
        }
        connection.close();
        connection = null;
        log.info("disconnected from database");

    }
}
