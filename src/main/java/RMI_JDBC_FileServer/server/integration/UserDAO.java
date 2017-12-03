/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.integration;

import RMI_JDBC_FileServer.common.ProgramUser;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 *
 * @author darkferi
 */
public class UserDAO {
    
    ///////////////////////////////////////////////////FIELDS/////////////////////////////////////////////////////////////
    
    private static final String TABLE_NAME = "userdata";
    private static final int MAX_ID = 10000000;
    private PreparedStatement createPersonStmt;
    private PreparedStatement findAllPersonsStmt;
    private PreparedStatement deletePersonStmt;
    private PreparedStatement findUniqueUser;
    private PreparedStatement findAuthenticatedUser;
    private PreparedStatement setNotificationStmt;
    private final Random idGenerator = new Random();
    private final Connection connection;
    private final Statement statement;
    
    
    public UserDAO() throws ClassNotFoundException, SQLException{
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        connection = DriverManager.getConnection(
           "jdbc:derby://localhost:1527/FileCatalogue", "farhad","farhad");
        createTable(connection);
        statement = connection.createStatement();
    }

    ///////////////////////////////////////////////////METHODS////////////////////////////////////////////////////////////
    
    public boolean registerInDatabase(String username, String password){
        try{
            prepareStatements(connection);
            findUniqueUser.setString(1, username);
            ResultSet resultSet = findUniqueUser.executeQuery();
                    
            while(resultSet.next()){
                if(username.equals(resultSet.getString("username"))){
                    return false;
                }
            }
            long uid = 0;
            boolean goodID = false;
            while(uid < 10 && !goodID){
                uid = idGenerator.nextInt(MAX_ID);
                resultSet = findAllPersonsStmt.executeQuery();
                goodID = true;
                while(resultSet.next()){
                    if(uid == resultSet.getLong(1)){
                        goodID = false;
                        break;
                    }
                }
            }
            createPersonStmt.setLong(1, uid);
            createPersonStmt.setString(2, username);
            createPersonStmt.setString(3, password);
            createPersonStmt.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Server > UserDAO > Register > SQLException occured!!!!");
            return false;
        }
    }
    
    
    public boolean unregisterFromDatabase(String username, String password) {
        try {
            prepareStatements(connection);
            findAuthenticatedUser.setString(1, username);
            findAuthenticatedUser.setString(2, password);
            ResultSet resultSet = findAuthenticatedUser.executeQuery();
            while(resultSet.next()){
                deletePersonStmt.setString(1, username);
                deletePersonStmt.setString(2, password);
                deletePersonStmt.executeUpdate();
                return true;
            }
        }
        catch (SQLException ex) {
            System.out.println("Server > UserDAO > Unregister > SQLException occured!!!!");
        }
        return false;
    }
    
    
    public long loginToDatabase(ProgramUser user, String username, String password) {
        try {
            prepareStatements(connection);
            findAuthenticatedUser.setString(1, username);
            findAuthenticatedUser.setString(2, password);
            ResultSet resultSet = findAuthenticatedUser.executeQuery();
            while(resultSet.next()){
                 return resultSet.getLong("userid");
            }
        }
        catch (SQLException ex) {
            System.out.println("Server > UserDAO > Login > SQLException occured!!!!");
        }
        return -1;
    }
    
    private void createTable(Connection connection) throws SQLException {
        if (!tableExists(connection)) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                    "create table " + TABLE_NAME + " (userid bigint primary key, username varchar(25), password varchar(25))");
        }
    }
    
    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableMetaData = metaData.getTables(null, null, null, null);
        while (tableMetaData.next()) {
            String tableName = tableMetaData.getString(3);
            if (tableName.equalsIgnoreCase(TABLE_NAME)) {
                return true;
            }
        }
        return false;
    }
    
    private void prepareStatements(Connection connection) throws SQLException {
        createPersonStmt = connection.prepareStatement("INSERT INTO "
                                                        + TABLE_NAME + " VALUES (?, ?, ?)");
        
        findUniqueUser = connection.prepareStatement("SELECT * from "
                                                        + TABLE_NAME
                                                        + " WHERE username = ?");
        
        findAuthenticatedUser = connection.prepareStatement("SELECT * from "
                                                        + TABLE_NAME
                                                        + " WHERE username = ? AND password = ?");
        
        deletePersonStmt = connection.prepareStatement("DELETE FROM "
                                                        + TABLE_NAME
                                                        + " WHERE username = ? AND password = ?");
        
        findAllPersonsStmt = connection.prepareStatement("SELECT * from "
                                                         + TABLE_NAME);
    }

}
