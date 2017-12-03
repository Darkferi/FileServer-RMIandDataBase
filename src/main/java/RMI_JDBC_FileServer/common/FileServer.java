/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 *
 * @author darkferi
 */
public interface FileServer extends Remote{
    
    public static final String SERVER_NAME_IN_REGISTRY = "FileServer";
    public static final String HOST = "localhost:1099";
    
    boolean register(String username, String password)throws RemoteException;
    
    boolean unregister(String username, String password)throws RemoteException;
    
    long login(ProgramUser user, String username, String password) throws RemoteException;
    
    boolean logout(long id) throws RemoteException;
    
    List <String> fileList(long id)throws RemoteException;
    
    boolean uploadFile(String fileName, String[] content, long userId, boolean pubAccess, boolean rwPermission) throws RemoteException;
    
    String[] downloadFile(String fileName, long userId) throws RemoteException;
    
    boolean deleteFile(String fileName, long userId) throws RemoteException;

    boolean writeToFile(String fineNameInServer, String dataToWrite, long userId, String method) throws RemoteException;
    
    String[] readFromFile (String fileName, long userId) throws RemoteException;

    boolean notifyWhenCHange(String fileName, long userId)throws RemoteException;
    
}
