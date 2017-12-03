 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.startup;

import RMI_JDBC_FileServer.server.controller.ServerController;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

public class StartServer {
    public static void main(String[] args) {
        try {
            new StartServer().startRegistry();
            ServerController server = new ServerController();
                     
            Naming.rebind(ServerController.SERVER_NAME_IN_REGISTRY, server);
            System.out.println("Server is running.");
        } catch (MalformedURLException | RemoteException ex) {
            System.out.println("Could not start File Server.");
        } catch (ClassNotFoundException ex) {
            System.out.println("ServerError > Start > ClassNotFoundException > Class.forName(java.lang.String)");
        } catch (SQLException ex) {
            System.out.println("ServerError > Start > SQLException > Connection to File Catalogue");
        }
    }
    private void startRegistry() throws RemoteException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryIsRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
    }
}
