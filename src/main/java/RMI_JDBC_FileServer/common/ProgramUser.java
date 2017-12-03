/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author darkferi
 */
public interface ProgramUser extends Remote{
           
        void messageOnScreen(String msg) throws RemoteException;

}
