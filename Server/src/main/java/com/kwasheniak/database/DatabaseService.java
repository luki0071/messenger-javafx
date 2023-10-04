package com.kwasheniak.database;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.sql.*;

@Log4j2
public class DatabaseService {
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    public static final String DATABASE = "jdbc:mysql://localhost:3306/messengerjfx";

    @Getter
    private static Connection connection;

    /**
     * initiate connection with database
     * @throws SQLException
     */
    public static void establishConnection() throws SQLException{
        if(!isConnectionAvailable()){
            connection = DriverManager.getConnection(DATABASE, USER, PASSWORD);
            log.info("connected to database: " + DATABASE);
        }else{
            log.info("database is already connected");
        }

    }

    /**
     * check if connection with database is available
     * @return true if connection is available and false if no
     * @throws SQLException
     */
    public static Boolean isConnectionAvailable() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    /**
     * closes connection with database
     * @throws SQLException
     */
    public static void closeConnection() throws SQLException{
        if(isConnectionAvailable()){
            connection.close();
            connection = null;
            log.info("disconnected from database");
        }else{
            log.info("cannot close connection with database because connection is null");
        }

    }
}
