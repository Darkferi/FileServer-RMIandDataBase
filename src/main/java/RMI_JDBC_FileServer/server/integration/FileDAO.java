/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author darkferi
 */
public class FileDAO {
    private static final String File_TABLE_NAME = "filedata";
    private static final String SPACE = "          ";
    private PreparedStatement createFileStmt;
    private final Connection connection;
    private final Statement statement;
    
    //private long uid;
    
    public FileDAO() throws ClassNotFoundException, SQLException{
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        connection = DriverManager.getConnection(
           "jdbc:derby://localhost:1527/FileCatalogue", "farhad","farhad");
        createTable(connection);
        statement = connection.createStatement();
    }
    
    
    public List<String> getListOfFiles(long uid){
        List<String> listOfFiles = new ArrayList<String>();
        try {
            String fileAttributes;
            String id = Long.toString(uid);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE owner ='"+id+"' OR public_access = 'TRUE'");
            while (resultSet.next()){
                fileAttributes = "Filename: " + resultSet.getString("name")+SPACE+ "Filesize: " + resultSet.getDouble("size") 
                        +SPACE+ "Owner ID: " +resultSet.getString("owner") +SPACE+  "Write Permission: " + resultSet.getBoolean("rw_permission"); 
                listOfFiles.add(fileAttributes);
                //listOfFiles.add(resultSet.getString("name"));
            }
            
            
        }  catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listOfFiles;
        
    }
    
    public boolean addUploadedFileToDatabase(String name,double size, String userId,boolean pubAccess, boolean rwPermission){
        try {
            prepareStatements(connection);
            boolean notify = false;
            createFileStmt.setString(1, name);
            createFileStmt.setDouble(2, size);
            createFileStmt.setString(3, userId);
            createFileStmt.setBoolean(4, pubAccess);
            createFileStmt.setBoolean(5, rwPermission);
            createFileStmt.setBoolean(6, notify);
            createFileStmt.executeUpdate();
            return true;
            
        }  catch (SQLException ex) {
            System.out.println("Someone tried to overwrite one File or Problem with SQL (look at columns and data types carefully)!!!!!\n");
        }
        return false;
    
    }
    
    
   public boolean getFileFromDatabase(String fileName, String userId){
       try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE (name ='"+fileName+"') AND (owner ='"+userId+"' OR public_access = 'TRUE')");
            if(resultSet.next()){
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
   }
    
   
   public boolean deleteFileFromDatabase(String fileName, String userId){
       try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE (name ='"+fileName+"') AND (owner ='"+userId+"' "
                    + "OR (public_access = 'TRUE' AND RW_PERMISSION = 'TRUE'))");
            if(resultSet.next()){
                statement.executeUpdate("DELETE FROM " + File_TABLE_NAME + " WHERE name ='"+fileName+"' AND owner ='"+userId+"'");
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
   }
    
    
   public boolean writeToFileInDatabase(String fileName,String  userId) {
       try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE (name ='"+fileName+"') AND"
                    + " ((owner ='"+userId+"') OR (public_access = 'TRUE' AND RW_PERMISSION = 'TRUE'))" );
            
            if(resultSet.next()){
                return true;
            }
        }catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
   }
    
   public boolean setNotification(String fileName, String userId){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE name ='"+fileName+"' AND"
                    + " owner ='"+userId+"' AND public_access = 'TRUE'" );
            
            if(resultSet.next()){
                statement.executeUpdate("UPDATE " + File_TABLE_NAME + 
                        " SET notification = 'TRUE' WHERE name ='"+fileName+"' AND owner ='"+userId+"'");
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
   } 
   
   
   public String notifyOwner(String fileName, String userId){
       try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + File_TABLE_NAME + " WHERE (name ='"+fileName+"') AND"
                    + " (NOT owner ='"+userId+"') AND (notification = 'TRUE')" );
            
            if(resultSet.next()){
                return resultSet.getString("owner");
            }
        } catch (SQLException ex) {
            Logger.getLogger(FileDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "no";
   }
    
    
     
    private void createTable(Connection connection) throws SQLException {
        if (!tableExists(connection)) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                    "create table " + File_TABLE_NAME + " (name varchar(25) primary key,size double, owner varchar(25), PUBLIC_ACCESS boolean, "
                            + "RW_PERMISSION boolean, NOTIFICATION boolean)");
        }
    }

    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableMetaData = metaData.getTables(null, null, null, null);
        while (tableMetaData.next()) {
            String tableName = tableMetaData.getString(3);
            if (tableName.equalsIgnoreCase(File_TABLE_NAME)) {
                return true;
            }
        }
        return false;
    }

    private void prepareStatements(Connection connection) throws SQLException {
        createFileStmt = connection.prepareStatement("INSERT INTO "
                                                       + File_TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)");
        
        
    }

}
