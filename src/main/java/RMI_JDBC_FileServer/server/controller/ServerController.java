/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.controller;

import RMI_JDBC_FileServer.server.model.UserInfo;
import RMI_JDBC_FileServer.common.FileServer;
import RMI_JDBC_FileServer.common.ProgramUser;
import RMI_JDBC_FileServer.server.integration.FileDAO;
import RMI_JDBC_FileServer.server.integration.UserDAO;
import RMI_JDBC_FileServer.server.model.FileManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *
 * @author darkferi
 */
public class ServerController extends UnicastRemoteObject implements FileServer{
    
    private long userId = 0;
    private boolean success = false;
    private final List <UserInfo> listOfConnectedUsers;
    private final UserDAO userDatabase;
    private final FileDAO fileCatalogue;
    
    
    public ServerController() throws RemoteException, ClassNotFoundException, SQLException {
        listOfConnectedUsers = new CopyOnWriteArrayList<UserInfo>();
        userDatabase = new UserDAO();
        fileCatalogue = new FileDAO();
    }
    
    ////////////////////////////////////////////REGISTER///////////////////////////////////////////////////
    
    @Override
    public synchronized boolean register(String username, String password) throws RemoteException{
        success = userDatabase.registerInDatabase(username, password);
        return success;
    }
    
    ////////////////////////////////////////////UNREGISTER/////////////////////////////////////////////////

    @Override
    public synchronized boolean unregister(String username, String password) throws RemoteException{
        success = userDatabase.unregisterFromDatabase(username, password);
        return success;
    }
    
    ///////////////////////////////////////////////LOGIN///////////////////////////////////////////////////
    
    @Override
    public synchronized long login(ProgramUser user, String username, String password) throws RemoteException{
        
        UserInfo userInfo = new UserInfo(user, username, password);
        userId = userDatabase.loginToDatabase(user, username, password);
        for (UserInfo element : listOfConnectedUsers) {
            if(element.userId == userId && element.connected){
                return 0;
            }
        }
        //the first possible user ID is 10 which belongs to the administrator. 
        //User ID less than 10 are used for controlling oprations...
        if (userId > 9){
            userInfo.connected = true;
            userInfo.userId = userId;
            listOfConnectedUsers.add(userInfo);
        }
        return userId;
    }
    
    ///////////////////////////////////////////////LOGOUT//////////////////////////////////////////////////
    
    @Override
    public synchronized boolean logout(long id) throws RemoteException{
        for (UserInfo element : listOfConnectedUsers) {
            if(element.userId == id && element.connected){
                element.connected = false;
                listOfConnectedUsers.remove(element);
                success = true;
                if (listOfConnectedUsers.isEmpty()){
                    break;
                }
            }
        }
        if (success){
            success = false;
            return true;
        }
        return success;
    }
    
    ///////////////////////////////////////////////FILE_LIST/////////////////////////////////////////////////
    
    @Override
    public synchronized List<String> fileList(long uid){
        List <String> listOfFiles = new ArrayList<String>();
        listOfFiles = fileCatalogue.getListOfFiles(uid);
        
        return listOfFiles;
    }
    
    ////////////////////////////////////////////UPLOAD_FILE/////////////////////////////////////////////////
    
    @Override
    public synchronized boolean uploadFile(String fileName, String[] content, long userId, boolean pubAccess, boolean rwPermission){
        FileManager fileManager = new FileManager();
        double fileSize = (double) fileManager.uploadFile(fileName, content);
        String uid = Long.toString(userId);
        success = fileCatalogue.addUploadedFileToDatabase(fileName, fileSize, uid,pubAccess, rwPermission);
        return success;
    }
    
    /////////////////////////////////////////DOWNLOAD_FILE//////////////////////////////////////////////////
    
    @Override
    public synchronized String[] downloadFile(String fileName, long userId){
        FileManager fileManager = new FileManager();
        String uid = Long.toString(userId);
        boolean permission = fileCatalogue.getFileFromDatabase(fileName, uid);
        if (permission){
            String[] content = fileManager.downloadFile(fileName);
            return content;
        }
        return null;
    }
    
    /////////////////////////////////////////DELETE_FILE////////////////////////////////////////////////////
    
    @Override
    public synchronized boolean deleteFile(String fileName, long userId){
        FileManager fileManager = new FileManager();
        String uid = Long.toString(userId);
        boolean permission = fileCatalogue.deleteFileFromDatabase(fileName, uid);
        if (permission){
            fileManager.deleteFile(fileName);
            return true;
        }
        return false;
    }
    
    ///////////////////////////////////////WRITE_INTO_FILE//////////////////////////////////////////////////
    
    @Override
    public synchronized boolean writeToFile(String fileName, String dataToWrite, long userId, String method) throws RemoteException{
        FileManager fileManager = new FileManager();
        String uid = Long.toString(userId);
        boolean permission = fileCatalogue.writeToFileInDatabase(fileName, uid);
        if(permission){
            success = fileManager.writeIntoFile(fileName, dataToWrite, method);
            if(success){
                String ownerId = fileCatalogue.notifyOwner(fileName, uid);
                if(!ownerId.equalsIgnoreCase("no")){
                    for (UserInfo element : listOfConnectedUsers) {
                        if(element.userId == Long.parseLong(ownerId) && element.connected){
                            element.remoteUser.messageOnScreen(
                                    "\n----------------------------NOTIFICATION----------------------------------------\n" + 
                                    "A user with ID = "+ uid +" WROTE into your file named "+ fileName +"." +       
                                    "\n----------------------------NOTIFICATION----------------------------------------\n\n");
                            break;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /////////////////////////////////////////READ_FROM_FILE/////////////////////////////////////////////////
    
    @Override
    public synchronized String[] readFromFile(String fileName, long userId) throws RemoteException{
        FileManager fileManager = new FileManager();
        String uid = Long.toString(userId);
        boolean permission = fileCatalogue.getFileFromDatabase(fileName, uid);
        if (permission){
            String[] content = fileManager.downloadFile(fileName);
            String ownerId = fileCatalogue.notifyOwner(fileName, uid);
            if(!ownerId.equalsIgnoreCase("no")){
                for (UserInfo element : listOfConnectedUsers) {
                    if(element.userId == Long.parseLong(ownerId) && element.connected){
                        element.remoteUser.messageOnScreen(
                                "\n----------------------------NOTIFICATION----------------------------------------\n" + 
                                "A user with ID = "+ uid +" READ your file named "+ fileName +"." +       
                                "\n----------------------------NOTIFICATION----------------------------------------\n\n");
                        break;
                    }
                }
            }
            
            return content;
        }
        return null;
    }
    
    ///////////////////////////////////////////NOTIFY_USER//////////////////////////////////////////////////
    
    @Override
    public synchronized boolean notifyWhenCHange(String fileName, long userId){
        String uid = Long.toString(userId);
        boolean done = fileCatalogue.setNotification(fileName, uid);
        if(done){
            return true;
        }
        return false;
    }
    
}
