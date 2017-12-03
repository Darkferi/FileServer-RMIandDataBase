
//no change

package RMI_JDBC_FileServer.client.view;


public class ClientConsoleThreadSafety {
    
    synchronized void print(String consoleOutput) {
        System.out.print(consoleOutput);
    }
    
    synchronized void println(String consoleOutput) {
        System.out.println(consoleOutput);
    }
    
}
