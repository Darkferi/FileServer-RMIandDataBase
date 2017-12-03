
//no change

package RMI_JDBC_FileServer.client.view;

import RMI_JDBC_FileServer.common.ProgramUser;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class ConsoleOutput extends UnicastRemoteObject implements ProgramUser {                     
    
    private ClientConsoleThreadSafety consoleManager = new ClientConsoleThreadSafety();
    public String uid;
    
    public ConsoleOutput() throws RemoteException {
    }
    
    public void messageOnScreen(String message) {
        consoleManager.println(message);
    }
    
}
