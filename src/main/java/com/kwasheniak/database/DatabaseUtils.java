package com.kwasheniak.database;

import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Log4j2
public class DatabaseUtils {

    public static boolean isLoginDataCorrect(String username, String password) throws SQLException {

        Connection connection = DatabaseService.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean exists = resultSet.next();
        preparedStatement.close();
        resultSet.close();
        return exists;
    }

    public static boolean isUserExists(String username) throws SQLException {
        Connection connection = DatabaseService.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean exists = resultSet.next();
        preparedStatement.close();
        resultSet.close();
        return exists;
    }

    public static boolean addUser(String username, String password) throws SQLException {
        Connection connection = DatabaseService.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        int count = preparedStatement.executeUpdate();
        preparedStatement.close();
        return count > 0;
    }

    public static ArrayList<String> getAllUsernames() throws SQLException {
        Connection connection = DatabaseService.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users");
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> username = new ArrayList<>();
        while (resultSet.next()) {
            username.add(resultSet.getString(1));
        }
        preparedStatement.close();
        resultSet.close();
        return username;
    }

    public static String[] getAllUsernamesExcept(String username) throws SQLException {
        Connection connection = DatabaseService.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username <> ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<String> usernames = new ArrayList<>();
        while (resultSet.next()) {
            usernames.add(resultSet.getString(1));
        }
        preparedStatement.close();
        resultSet.close();
        return usernames.toArray(new String[0]);
    }
}
