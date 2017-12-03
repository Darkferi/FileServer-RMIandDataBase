
package RMI_JDBC_FileServer.client.view;

import RMI_JDBC_FileServer.common.FileServer;
import RMI_JDBC_FileServer.common.ProgramUser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author darkferi
 */
public class ClientInterpreter implements Runnable{
    
    private static final String PUB_ACCESS = "public_access";
    private static final String RW_PERMISSION = "write_permission";
    private static final String PROMPT = "> ";
    private BufferedReader console;
    private final ClientConsoleThreadSafety consoleManager = new ClientConsoleThreadSafety();
    private static boolean ThreadStarted = false;
    private final ProgramUser remoteUserObj;
    private FileServer fileServer;
    private long userId;
    private boolean success = false;
    private boolean flagConnected = false;
    
    
    public ClientInterpreter() throws RemoteException{
        remoteUserObj = new ConsoleOutput();
        userId = -1;
    }
    
    
    @Override
    public void run(){
        ThreadStarted = true;
        
        try {
            fileServer = (FileServer) Naming.lookup("//" + FileServer.HOST + "/" + FileServer.SERVER_NAME_IN_REGISTRY);
        } catch (NotBoundException e) {
            consoleManager.println("ClientVeiw > run() > NotBoundException: Name is not currently bound");
        } catch (MalformedURLException e) {
            consoleManager.println("ClientVeiw > run() > MalformedURLException: Name is not an appropriately formatted URL");
        } catch (RemoteException e) {
            consoleManager.println("ClientVeiw > run() > RemoteException: Registry could not be contacted");
        }
        
        printIntroductionMessage();
        
        console = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String [] userData;
        while(ThreadStarted){
            try {
                consoleManager.print(PROMPT);
                command = console.readLine();
                command = command.trim();
                userData = command.split(" ");
                userData[0] = userData[0].toLowerCase();
                
                ////////////////////////////////////////////LOGIN///////////////////////////////////////////////////
                
                if(userData[0].equalsIgnoreCase("login") && userData.length == 3){
                    if(!flagConnected){
                        userId = fileServer.login(remoteUserObj, userData[1],userData[2]);
                        if(userId == -1){
                            consoleManager.println("Username or Password is wrong!!!.\n");
                        }
                        else if(userId==0){
                            consoleManager.println("This account is already logged in!!!.\n");
                        }
                        else{
                            consoleManager.println("Successful Login!!!\n");
                            flagConnected = true;
                        }
                    }
                    else{
                        consoleManager.println("You are already logged in!!!.\n");        
                    }
                }
                
                ////////////////////////////////////////////LOGOUT//////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("logout") && userData.length == 1){
                    
                    success = fileServer.logout(userId);
                    if(success){
                        consoleManager.println("Successful Logout!!!\n");
                        flagConnected = false;
                    }
                                           
                    else{
                        consoleManager.println("You haven't logged in yet. First Login then Logout!!!\n");
                    }
                }
                
                ////////////////////////////////////////////REGISTER////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("register") && userData.length == 3){
                    success = fileServer.register(userData[1].trim(),userData[2].trim());
                    if (success){
                        consoleManager.println("Successful Register!!!\n");
                    }
                    else{
                        consoleManager.println("your USERNAME is already taken, give another one...\n");
                    }
                }
                
                //////////////////////////////////////////UNREGISTER////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("unregister") && userData.length == 3){
                    success = fileServer.unregister(userData[1].trim(),userData[2].trim());
                    if (success){
                        consoleManager.println("Successful Unregister!!!\n");
                    }
                    else{
                        consoleManager.println("Username or Password for Unregister is incorrect!!!\n");
                    }
                }
                
                /////////////////////////////////////////GET_LIST///////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("list") && userData.length == 1){
                    if(flagConnected){
                        List<String> fileList = new ArrayList<String>();
                        fileList = fileServer.fileList(userId);
                        consoleManager.println("");
                        for (String s: fileList){
                            consoleManager.println(s);
                        }
                        consoleManager.println("");
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                
                //////////////////////////////////////UPLOAD_FILE//////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("upload") && ( 2 < userData.length && userData.length < 6)){
                    if(flagConnected){
                        String fileAddress = userData[1];
                        String fileNameInServer = userData[2];
                        String[] content = readFile(fileAddress);
                        boolean flag = false;
                        boolean pubAccess = false;
                        boolean rwPermission = false;
                        if(userData.length > 3){
                            if(userData[3].equalsIgnoreCase(PUB_ACCESS)){
                                pubAccess = true;
                                if((userData.length == 5) && userData[4].equalsIgnoreCase(RW_PERMISSION)){
                                    rwPermission = true;
                                }
                                else{
                                    flag =true;
                                }
                            }
                            else{
                                flag = true;
                            }
                        }
                        if(content[0].equalsIgnoreCase("empty")){
                            consoleManager.println("Problem with your FILE_PATH!!!\n");
                        }
                        else if(flag){
                            consoleManager.println("Problem with your Public Access or Write Permission setting!!!\n");
                        }
                        else{
                            success = fileServer.uploadFile(fileNameInServer, content, userId, pubAccess, rwPermission);
                            if (success){
                                consoleManager.println("Successful Upload!!!\n");
                            }
                            else{
                                consoleManager.println("Your upload was Unsuccesfull\n"
                                        + "Possible reasons: 1) the name chosen for you file in the server, already exists "
                                        + "2) Problem with FileCatalogue.\n");
                            }
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                
                
                //////////////////////////////////////DOWNLOAD_FILE////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("download") && (userData.length == 2 || userData.length == 3)){
                    if(flagConnected){
                        String fileName = userData[1];
                        String filePath;
                        if(userData.length == 2){
                            filePath = "default_downloaded_path\\";
                            filePath += fileName;
                        }
                        else{
                            filePath = userData[2];
                            if(filePath.endsWith("\\")){
                                filePath += fileName;
                            }
                            else{
                                filePath = filePath + "\\" + fileName;
                            }
                        }
                        String[] content = fileServer.downloadFile(fileName, userId);
                        if(content == null){
                            consoleManager.println("ServerError --> Possible reasons: 1) Your can't access to the file 2) The file doesn't exist.\n");
                        }
                        else{
                            success = storeFile(filePath, content);
                            if(success){
                                consoleManager.println("Successful Download!!!\n");
                            }
                            else{
                                consoleManager.println("ClientError: Check your FILE_PATH)\n");
                            }
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                
                ///////////////////////////////////////DELETE_FILE//////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("delete") && userData.length == 2){
                    if(flagConnected){
                        String fileName = userData[1];
                        
                        success = fileServer.deleteFile(fileName, userId);
                        if (success){
                            consoleManager.println("Successful Delete!!!\n");
                        }
                        else{
                            consoleManager.println("Your delete was Unsuccesfull\n"
                                    + "Possible reasons: 1) The file doesn't exist 2) Your are not allowed to delete the file.\n ");
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }

                /////////////////////////////////////////READ_FILE//////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("read") && userData.length == 2){
                    if(flagConnected){
                        String fileName = userData[1];
                        
                        String[] content = fileServer.readFromFile(fileName, userId);
                        
                        if(content == null){
                            consoleManager.println("ServerError --> Possible reasons: 1) Your can't access to the file 2) The file doesn't exist.\n");
                        }
                        else{
                            consoleManager.println("\n");
                            consoleManager.println("---------------------------------------------Start of File------------------------------------------");
                            consoleManager.println("\n");
                            for (int i=0; i < content.length; i++){
                                consoleManager.println(content[i]);
                            }
                            consoleManager.println("\n");
                            consoleManager.println("----------------------------------------------End of File-------------------------------------------");
                            consoleManager.println("\n");
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                
                /////////////////////////////////////////WRITE_FILE/////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("write") && userData.length == 3){
                    if(flagConnected){
                        String fineNameInServer = userData[1];
                        String method = userData[2];                //overwritten or appended
                                               
                        consoleManager.println("Write whatever you want to put into the file:\n");
                        String dataToWrite = console.readLine();
                        
                        success = fileServer.writeToFile(fineNameInServer, dataToWrite, userId, method);
                       
                        if (success){
                            consoleManager.println("Successful Write!!!\n");
                        }
                        else{
                            consoleManager.println("ServerError --> Possible reasons: 1) Your can't access to the file 2) The file doesn't exist. 3) chosen wrong method \n");
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                

                ///////////////////////////////////////////NOTIFY///////////////////////////////////////////////////
                else if(userData[0].equalsIgnoreCase("notify") && userData.length == 2){
                    if(flagConnected){
                        String fileName = userData[1];
                                              
                        success = fileServer.notifyWhenCHange(fileName, userId);
                       
                        if (success){
                            consoleManager.println("Successful Notify!!!\n");
                        }
                        else{
                            consoleManager.println("ServerError --> Possible reasons: 1) The file doesn't exist 2) Your are not owner of this file "
                                    + "3) File is private\n");
                        }
                    }
                    else{
                        consoleManager.println("You haven't logged in yet. First Login!!!\n");
                    }
                }
                
                ///////////////////////////////////////////HELP/////////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("help") && userData.length == 1){
                    printHelpMessage();
                    
                }
                
                ///////////////////////////////////////////EXIT/////////////////////////////////////////////////////
                
                else if(userData[0].equalsIgnoreCase("exit") && userData.length == 1){
                    if(flagConnected){
                        fileServer.logout(userId);
                        flagConnected = false;
                    } 
                    ThreadStarted = false;
                    consoleManager.println("Successful EXIT!!!\n");
                }
                else{
                    consoleManager.println("Illegal Command (Press HELP)...\n");
                }
                           
            } catch (IOException e) {
                consoleManager.println("ClientVeiw > run() > IOException");
            } 
        }      
    }
    
    
    public String[] readFile(String fileAddress){
        try{
            boolean readOK = true;
            BufferedReader fromFile = new BufferedReader(new FileReader(fileAddress));
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
            String problem = "empty";
            String[] res = problem.split(" ");
            return res;
        }
    }
    
    
    public boolean storeFile(String path, String content[]){
        try{
            File file = new File(path);
            PrintWriter toFile = new PrintWriter(new FileWriter(file));
            for (int i=0; i < content.length; i++){
                toFile.println(content[i]);
            }
            toFile.close();
            return true;
        }catch (IOException e){
            System.out.println("Server(FileRead): IOException happened!!!");
            return false;
        }
    }
     
    private void printIntroductionMessage(){
         String s = "-----------------------------------------------"
                    + "---------------------------------------------------------------------"
                    + "\nHey!!!\nWelcome to File Sharing Application.\n\n"
                    + "In this application you are allowed to upload, download and delete your files to/from File Catalogue.\n" 
                    + "First you need to create an account using REGISTER command if you don't have any account in our user database.\n"
                    + "Otherwise you just need to login into our server using LOGIN command.\n"
                    + "After login, you can see list of files to which you have access using LIST command.\n"
                    + "you are able to read the files from File Catalogue which has PUBLIC access and write into files with WRITE permision.\n"
                    + "Also you can ask our server to sen you notification whenever someone reads or modifies one of you files.\n\n"
                    + "To find the application commands and their syntax print HELP...\n\n";
         
         consoleManager.println(s);
    }
     
    private void printHelpMessage(){
        String s = "\n--------------------------------------------------------------------------------------------------------------------\n" +
                    "Legal Commands in Application \n\n" +
                    "[option]: Mandatory    <option>: Optional\n\n" +
                    "01) REGISTER   --> register [username] [password]\n" +
                    "02) UNREGISTER --> unregister [username] [password]\n" + 
                    "03) LOGIN      --> login  [username] [password]\n" + 
                    "04) LOGOUT     --> logout\n" +
                    "05) UPLOAD     --> upload [file_path_in_your_system] [file_name_in_server] <public_access> <write_permission>\n" +
                    "06) DOWNLOAD   --> download [file_name] <file_path_in_your_system>\n" +
                    "07) READ       --> read [file_name]\n" +
                    "08) WRITE      --> write [file_name] [overwritten|append]\n" +
                    "09) DELETE     --> delete [file_name]\n" +
                    "10) LIST       --> list\n" +
                    "11) NOTIFY     --> notify [file_name]\n" +
                    "12) HELP       --> notify [file_name]\n" +
                    "13) EXIT       --> exit\n" +
                    "\n" +
                    "--------------------------------------------------------------------------------------------------------------------\n";

        consoleManager.println(s);
    }

}


