/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.model;

import RMI_JDBC_FileServer.common.ProgramUser;

/**
 *
 * @author darkferi
 */
public class UserInfo {
    public long userId;
    public String username;
    public String password;
    public boolean connected;
    public ProgramUser remoteUser;
    
    public UserInfo(){
        userId = 0;
        username = null;
        password = null;
        remoteUser = null;
        connected = false;
    }
    public UserInfo(ProgramUser remoteUser, String username, String password){
        userId = 0;
        this.username = username;
        this.password = password;
        this.remoteUser = remoteUser;
        connected = false;
    }
    
}
