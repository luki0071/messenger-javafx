package com.kwasheniak.database;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.sql.*;

@Log4j2
public class DatabaseService {
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    private static final String DATABASE = "jdbc:mysql://localhost:3306/messengerjfx";

    @Getter
    private static Connection connection;
    public static void establishConnection(){
        try {
            connection = DriverManager.getConnection(DATABASE, USER, PASSWORD);
            log.info("connected with database");
        } catch (SQLException e) {
            log.info("couldn't connect to database " + e);
        }
    }

    public static Boolean isConnectionAvailable(){
        return connection != null;
    }

    public static Boolean isUserInUsersTable(String username, String password){
        if(isConnectionAvailable()){
            try {
                Statement statement = connection.createStatement();
                String query = "select * from users where username like '" + username + "' and "
                        + "password like '" + password + "';";
                ResultSet resultSet = statement.executeQuery(query);
                return resultSet.next();
            } catch (SQLException e) {
                log.info(e);
            }
        }
        return false;
    }

    public static void closeConnection(){
        if(isConnectionAvailable()){
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
