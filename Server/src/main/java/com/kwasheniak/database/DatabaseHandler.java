package com.kwasheniak.database;

import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Log4j2
public class DatabaseHandler {



    public static Boolean isUserInUsersTable(String username, String password) throws SQLException {
        if(DatabaseService.isConnectionAvailable()){
            try(Statement statement = DatabaseService.getConnection().createStatement()){
                String query = "select * from users where username like '" + username + "' and "
                        + "password like '" + password + "';";
                ResultSet resultSet = statement.executeQuery(query);
                return resultSet.next();
            }catch (SQLException e){
                log.info(e);
            }
        }
        return false;
    }
}
