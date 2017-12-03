/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RMI_JDBC_FileServer.server.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author darkferi
 */

public class FileManager {
    
    public long uploadFile(String name, String content[]){
        name = "files\\" + name;
        try{
            File file = new File(name);
            PrintWriter toFile = new PrintWriter(new FileWriter(file));
            for (int i=0; i < content.length; i++){
                toFile.println(content[i]);
           }
           toFile.close();
           return file.length();
        }catch (IOException e){
            System.out.println("Server(FileRead): IOException happened!!!");
            return 0;
        }
    }
    
    public String[] downloadFile(String name){
        try{
            name = "files\\" + name;
            boolean readOK = true;
            BufferedReader fromFile = new BufferedReader(new FileReader(name));
            String paragraph = fromFile.readLine();
            String temp;
            while(readOK){
                if((temp = fromFile.readLine()) == null)
                    readOK = false;
                else{
                    paragraph = paragraph + "\n" + temp; 
                }
            }
            String[] content = paragraph.split("\n");
            fromFile.close();
            return content;
        }catch (IOException e){
            System.out.println("FileRead: IOException happened!!!");
            return null;
        }
    }
    
    public void deleteFile(String name){
        name = "files\\" + name;
        File file = new File(name);
        file.delete();
        
    }
    
    public boolean writeIntoFile(String name, String data, String method){
       name = "files\\" + name;
        try{
            File file = new File(name);
            PrintWriter toFile;
            if(method.equalsIgnoreCase("overwritten")){
                toFile = new PrintWriter(new FileWriter(file));
                toFile.println(data);
                toFile.close();
            }
            else if(method.equalsIgnoreCase("append")){
                toFile = new PrintWriter(new FileWriter(file, true));
                toFile.println(data);
                toFile.close();
            }
            else{
                return false;
            }
        }catch (IOException e){
            System.out.println("Server(FileRead): IOException happened!!!");
            return false;
        }
        return true;
    }
    
}
