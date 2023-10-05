package com.kwasheniak.database;

import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Log4j2
public class DatabaseHandler {


    public static Boolean isUserInUsersTable(String username, String password) throws SQLException {
        /*try(Statement statement = DatabaseService.getConnection().createStatement()){
            String query = "select * from users where username like '" + username + "' and "
                    + "password like '" + password + "';";
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet.next();
        }catch (SQLException e){
            log.info(e);
        }
        return false;*/

        Statement statement = DatabaseService.getConnection().createStatement();
        String query = "select * from users where username like '" + username + "' and "
                + "password like '" + password + "';";
        ResultSet resultSet = statement.executeQuery(query);
        boolean exists = resultSet.next();
        statement.close();
        return exists;
    }

    public static Boolean isUserInUsersTable(String username) throws SQLException {
        Statement statement = DatabaseService.getConnection().createStatement();
        String query = "select * from users where username like '" + username + "';";
        ResultSet resultSet = statement.executeQuery(query);
        boolean exists = resultSet.next();
        statement.close();
        return exists;
    }

    public static Boolean addUserToUsersTable(String username, String password) throws SQLException{
        Statement statement = DatabaseService.getConnection().createStatement();
        String query = "insert into users(username,password) values ('" + username + "','" + password + "');";
        //String query = "INSERT INTO users(username,password) VALUES ('mati','1234');";
        int count = statement.executeUpdate(query);
        statement.close();
        return count > 0;
    }
}
