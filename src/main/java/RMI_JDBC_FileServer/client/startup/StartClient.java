/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.client.startup;

import java.rmi.RemoteException;
import RMI_JDBC_FileServer.client.view.ClientInterpreter;

public class StartClient {
    
    //public static final int SERVER_PORT = 8080;
    
    public static void main(String[] args) throws RemoteException{
            try {
                Thread userInterfaceThread = new Thread(new ClientInterpreter());
                userInterfaceThread.start();
            } catch (RemoteException e) {
                System.out.println("Client > Startup > RemoteException occured");
            }   
    }

}
