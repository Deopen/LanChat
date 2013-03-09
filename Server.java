/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.PreparedStatement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import static lanchat.ImpConsts.chat_port_number;
import static lanchat.LanChat.hashSetToString;
//http://www.roseindia.net/answers/viewqa/JDBC/13110-How-to-check-whether-database-exists.html
//http://docs.oracle.com/javase/1.5.0/docs/api/javax/crypto/spec/package-summary.html   
//havaaset baashe message digest ham baayad source daashte baashe , age nist peydaash kon

public class Server {
    ServerSocket serverSocket;
    private ExecutorService serverThreadPool;
    private HashMap<String,Formatter> outStreams;
//    private static final String SERVER_PATH="jdbc:mysql://localhost/lanchat_beta";
    
    public  static final String DATABASE_NAME="DeopenLanChat";
    private static final String SERVER_PATH="jdbc:mysql://localhost/"+DATABASE_NAME;
    private static final String JDBC_DRIVER="com.mysql.jdbc.Driver";
    
    private Connection sqlConnection;
    
    private HashMap<String,String> cNameToID;
    private HashMap<String,String> IDTocName;
    private HashSet<String> onlineList;    
    private HashMap<String,Date> lastLogin;
    private HashMap<Integer,String> confMap;    
    private HashMap<String,ArrayList<String>> offMsgs;
    
    private String sqlUsr="root";
    private String sqlPass="";    
    public static boolean isServerRun=false;
    private boolean LOg=true;
    private int updatingKeyForConfMap=-1;
    
    public Server() {
        
        
        isServerRun=true;
        outStreams=new HashMap<>();
        cNameToID=new HashMap<>();
        IDTocName=new HashMap<>();
        lastLogin=new HashMap<>();
        onlineList=new HashSet<>();
        offMsgs=new HashMap<>();
        confMap=new HashMap<>();
        try {
            serverThreadPool=Executors.newCachedThreadPool();
           serverSocket=new ServerSocket(chat_port_number,100);
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
    }// end Server generator
    
    public void startServer() {
        
        listen();  
        try {
            
            final PipedOutputStream pOut = new PipedOutputStream();   
            System.setOut(new PrintStream(pOut));
            final PipedInputStream pIn=new PipedInputStream(pOut);
        
            
            new Thread(new Runnable() {

                @Override
                public void run() {                    
                    new log(new InputStreamReader(pIn));
                }
            }).start();
            
        
        
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            sqlConnection=
                        DriverManager.getConnection(SERVER_PATH,sqlUsr,sqlPass);
        } catch (SQLException ex) {
            
            
            makeDbIfWeeNeed();
            
            
        }
            
        
        
        
    }// end start Server
    
    
    private void makeDbIfWeeNeed() {
        try {

            
            initializeDB();   
            
        
        } // end start Server
        catch (ClassNotFoundException | SQLException ex) {
//            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            
            sqlUsr=JOptionPane.showInputDialog("Please enter your MySqlConnection user name", "root");
            sqlPass=JOptionPane.showInputDialog("Please enter your "+sqlUsr+" password");
            try {
                initializeDB();
            } catch (    ClassNotFoundException | SQLException ex1) {
                
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                JOptionPane.showMessageDialog
                    (null, "SQL Error","SQL Error",
                    JOptionPane.ERROR_MESSAGE);

                System.exit(1);

            }
            
        }//end Error message (end catch)
    }//end make Db if wee need
    
    private void listen() {
        
        Runnable listener=new Runnable() {

            @Override
            public void run() {
                try {
               
                    while (true){
                        
                        synchronized(Server.this) {
                            Server.this.wait(200);
                        }
              
                        try{
                            Socket clinetSocket = serverSocket.accept();
                            runATheadForThisClient(clinetSocket);
                            System.out.println
                                    ("new connection recived from: "+
                                    clinetSocket.getInetAddress());
                        }catch(SocketException ex) {
                            continue;
                        } //end catch
                        
                    }// end endless while
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        };
        
        Thread listenerThread= new Thread(listener);        
        listenerThread.start();
    }// end listen
    
    
     private void runATheadForThisClient(final Socket connection) {
        
        Runnable client=new Runnable() {

            @Override
            public void run() {
                BufferedReader bufReader = null;
                String connectionName=
                        connection.getInetAddress().getHostName();
                
                
                try {
                    bufReader = new BufferedReader
                    (new InputStreamReader(connection.getInputStream()));
                    
                    outStreams.put
                            (connectionName,
                            new Formatter(connection.getOutputStream()));
                    outStreams.get(connectionName).flush();
                    
                    cNameToID.put(connectionName, "Unknown");

                    
                    while (connection.isConnected()) {
                        
                        synchronized(Server.this){
                            Server.this.wait(200);
                        }
                        String msg=null;
                        try{
                            msg=bufReader.readLine();
                        }catch(SocketException ex){}
                        if (msg==null)
                            continue;
                        
                        
                        if (msg.equals("dc"))
                            break;
                        
                        String[] splitedMsg=msg.split(":");
                        
                        if (splitedMsg.length>4){
                            String[] tmpSplitedMsg=new String[4];
                            tmpSplitedMsg[0]=splitedMsg[0];
                            tmpSplitedMsg[1]=splitedMsg[1];
                            tmpSplitedMsg[2]=splitedMsg[2];
                            tmpSplitedMsg[3]=splitedMsg[3];
                            for (int i=4;i<splitedMsg.length;i++)
                                tmpSplitedMsg[3]+=splitedMsg[i];
                            splitedMsg=tmpSplitedMsg;
                        }//end if
                       
                        if (splitedMsg.length!=4) {
                            continue;
                        }// end catching exception
                        
                        if (!splitedMsg[0].equals("servermsg"))
                            processMsg
                                    (splitedMsg[0], splitedMsg[1],
                                    splitedMsg[2],splitedMsg[3],
                                    connectionName
                                    );

                        if (
                                LOg &&
                                !splitedMsg[0].equals("updateOnList") &&
                                !splitedMsg[0].equals("servermsg"))
                            System.out.println(msg);
                        else if (splitedMsg[0].equals("servermsg")){
                            String eKey="SerrrrrrrrDeOpeNOMidddd99**64##@#$ ???"+splitedMsg[1];
                            System.out.println(splitedMsg[1]+" >>>SERVER>>> "+
                                    LanChat.decrypt(splitedMsg[3], eKey));
                        }
                        
                        
                    }// end for ever while
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    
                    try {
                        outStreams.remove(connectionName);
                        bufReader.close();
                        
                        if (cNameToID.containsKey(connectionName)){
                            IDTocName.remove(cNameToID.get(connectionName));
                            onlineList.remove(cNameToID.get(connectionName));
                            cNameToID.remove(connectionName);
                        }
                        
                        System.out.println
                                ("connection "+connection.getInetAddress()+" closed ");                        
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }// end catch
                }//end finaly
            }//end run
        };// end runnable
               
        serverThreadPool.execute(client);
        
    }// end run a thread
    
    private void processMsg
            (String type,String from,String to,
            String Msg,String connectionName) {
        switch (type) {
            case "register":
                try {
                    String[] cmd=Msg.split(",");
                    if (cmd.length!=5){
                        JOptionPane.showMessageDialog(null, 
                            "error registeration, Server class",
                            "Error",JOptionPane.ERROR_MESSAGE);
                        System.exit(1);                
                    }//end catching exception
                    addRow(cmd[0], cmd[1],cmd[2],cmd[3],cmd[4]);
                } //end if equal req
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, 
                            "error registeration, SQL exception",
                            "Error",JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    
                }// end catch                
                break;
            case "update":
                try {
                    String[] cmd=Msg.split(",");
                    if (cmd.length!=5){
                        JOptionPane.showMessageDialog(null, 
                            "error registeration, Server class",
                            "Error",JOptionPane.ERROR_MESSAGE);
                        System.exit(1);                
                    }//end catching exception
                    editRow(cmd[0], cmd[1],cmd[2],cmd[3],cmd[4]);
                } //end if equal req
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, 
                            "error registeration, SQL exception",
                            "Error",JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    
                }// end catch                
                break;
            case "msg":
                sendMsg(from,to, Msg);
                break;
            case "isUserValid":
                
        try {
            if (isUserValid(Msg))
                sendMsg(connectionName, "1");
            else
                sendMsg(connectionName, "0");
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch
                break;
            case "areYouLanchat":
                sendMsg(connectionName,"Yes-My-Son !");
                break;
            case "auth":
                String[] splitedMsg=Msg.split(",");
                if (authentication(splitedMsg[0],splitedMsg[1])){
                    sendMsg(connectionName,"come in!");
                    cNameToID.put(connectionName,splitedMsg[0]);
                    lastLogin.put(splitedMsg[0],new Date());
                    IDTocName.put(splitedMsg[0], connectionName);
                    onlineList.add(splitedMsg[0]);
                    Profile p=loadProfile(splitedMsg[0]);
                    
                    sendMsg
                            (connectionName,p.getFullName());
                    
                    sendMsg
                            (connectionName, hashSetToString
                            (p.getFriendList(),","));
                    
                    sendMsg
                            (connectionName, hashSetToString
                            (p.getBanList(),","));
                    
                    HashSet<String> onSet=new HashSet<>();
                    for (String e:p.getFriendList())
                        if (onlineList.contains(e))
                            onSet.add(e);
                    String onListStr=hashSetToString
                            (onSet,",");
                    
                    sendMsg
                            (connectionName, onListStr);
                    
                }
                else
                    sendMsg(connectionName,"faild!");
                break;
            case "giveMeMyOff":
                
                sendAllOffMsg(from,
                            offMsgs.get(from));                    
                break;
            case "updateOnList":
                String flist=Msg;
                String onList="";
                
                for (String f:flist.split(","))
                    if (onlineList.contains(f))
                        onList+=f+",";

                if (onList.length()>0)
                    onList=onList.substring(0,onList.length()-1);//ignore last seperator ","                
                
                sendMsg(connectionName,"updateOnList:server:"+onList);              
                break;                
            case "find":                          
                String searchRes=searchByNameFamilyUser(Msg,from);
                sendMsg(connectionName,"find:server:"+searchRes);                              
                break;                
            case "add":                          
                
                System.out.println
                        ("request to add "+Msg+" from "+from+" : "+
                        addOrRemoveFriendsOrBans
                        (from, Msg, "f","add"));      
                break;
            case "remove":                                          
                System.out.println
                        ("request to remove "+Msg+" from "+from+" : "+
                        addOrRemoveFriendsOrBans
                        (from, Msg, "f","remove"));   
                break;
            case "ban":                                          
                System.out.println
                        ("request to ban "+Msg+" from "+from+" : "+
                        addOrRemoveFriendsOrBans
                        (from, Msg, "b","add"));   
                
                break;
            case "removeBan":                                          
                System.out.println
                        ("request to rmove from banList "
                        +Msg+" from "+from+" : "+
                        addOrRemoveFriendsOrBans
                        (from, Msg, "b","remove"));   
                break;
            case "reqConf":
                
                String mainMsg="";
                for (String id:Msg.split(",")){
                    if (onlineList.contains(id) && 
                            !Arrays.asList(mainMsg.split(",")).contains(id))
                        mainMsg+=id+",";
                }                
                confMap.put(confMap.size(),mainMsg);
                if (mainMsg.length()>1){
                    mainMsg=mainMsg.substring(0,mainMsg.length()-1);
                    for (String id:mainMsg.split(",")){                   
                            sendMsg(IDTocName.get(id),
                                    "runConf:"+cNameToID.get(connectionName)+
                                    ":"+(confMap.size()-1)+","+mainMsg);
                    }//end for
                }//end if
                break;
            case "addConf":
                String confID=Msg.split(",")[1];
                while(updatingKeyForConfMap==Integer.valueOf(confID))
                {
                    synchronized(this){
                        try {
                            this.wait(50);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }//end synch
                }//end while                
                String idToAdd=Msg.split(",")[0];
                String users=confMap.get(Integer.valueOf(confID));
                users+=","+idToAdd;
                
                String mainUsers="";
                
                for (String id:users.split(",")){
                    if (!Arrays.asList(mainUsers.split(",")).contains(id))
                        mainUsers+=id+",";
                }
                if (mainUsers.length()>1)
                    mainUsers=mainUsers.substring(0,mainUsers.length()-1);
                updatingKeyForConfMap=Integer.valueOf(confID);
                confMap.put(Integer.valueOf(confID),mainUsers);
                updatingKeyForConfMap=-1;
                
                sendMsg(IDTocName.get(idToAdd),
                                    "runConf:"+cNameToID.get(connectionName)+
                                    ":"+(confID)+","+mainUsers);
                
                for (String id:users.split(","))
                        sendMsg(IDTocName.get(id),
                                "newConfUsers:"+
                                confID+":"+mainUsers);
                
                break;
            case "rmConf":
                while(updatingKeyForConfMap==Integer.valueOf(Msg))
                {
                    synchronized(this){
                        try {
                            this.wait(50);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }//end synch
                }//end while
                confID=Msg;
                users=confMap.get(Integer.valueOf(confID));                
                String newUsers="";
                for (String id:users.split(","))
                    if (!id.equals(from))
                        newUsers+=id+",";
                if (newUsers.length()>1){
                    newUsers=newUsers.substring(0,newUsers.length()-1);
                    updatingKeyForConfMap=Integer.valueOf(confID);
                    confMap.put(Integer.valueOf(confID),newUsers);
                    updatingKeyForConfMap=-1;
                    for (String id:newUsers.split(","))
                        sendMsg(IDTocName.get(id),"newConfUsers:"+confID+":"+newUsers);
                }//end if
                else
                    confMap.remove(Integer.valueOf(Msg));
                break;
            case"msgConf":
                
                while(updatingKeyForConfMap==Integer.valueOf(to))
                {
                    synchronized(this){
                        try {
                            this.wait(50);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }//end synch
                }//end while
                
                users=confMap.get(Integer.valueOf(to));
                
                for (String id:users.split(",")){
                    
                    sendMsg(IDTocName.get(id),
                            "confMsg:"+cNameToID.get(connectionName)+":"+to+","+Msg);                    
                }//end for id
                
                break;                
            default :
                //forward message
                sendMsg(IDTocName.get(to), type+":"+from+":"+Msg);
                break;
        }//end switch case
        
    }// end process msg
    
    public String searchByNameFamilyUser(String e,String uName) {
        
        ArrayList<Profile> results=new ArrayList<>();
        Profile userProfile=loadProfile(uName);
        String sqlCmd=
                "select user_name "
                + "from person where "
                + "user_name rlike \""+e+"\" "
                + "or name rlike \""+e+"\" "
                + "or family rlike \""+e+"\";";
        
        System.out.println(sqlCmd);
        
        try {
            ResultSet rs=executeSqlCmdAndReadResult(sqlCmd);
            
            while (rs.next()) 
                results.add(loadProfile(rs.getString("user_name")));            
                
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String answ="";
        
        
        for (Profile p:results){
            if (!p.isItBan(uName) && 
                    !userProfile.getFriendList().
                    contains(p.getUserName()) && 
                    !userProfile.isItBan(p.getUserName()))
                answ+=p.getFullNamePlusUserName()+",";
        }
        if (answ.length()>0)
            answ=answ.substring(0,answ.length()-1);//skip last seperator
        
        return answ;
    }//end search
    
    private void saveThisOffMsg
            (String to,String fromColonMsg) {
        
        String encryptedMsg=
                LanChat.encrypt
                (fromColonMsg,
                to+
                "OmidDeOpen_"
                + "insertintooffline_"
                + "msgvalues");
        
        addRowToOffMsg(to,encryptedMsg);
        
    }//end save this off msg
    private void sendAllOffMsg
            (String to,ArrayList<String> msgs) {
        
        
        for (String msg:msgs)
            sendOfflineMsg(to,msg);
            
        
        deleteOffMsgs(to);
    }//end send all off msg
    
    private void deleteOffMsgs(String to) {
        
        executeSqlCmd
                ("delete from of"
                + "fline_msg where "
                + "toThisUser=\'"+to+"\'");
        
    }//end delete offMsg
    
    private void sendOfflineMsg(final String to,
            String encryptedFromColonMsg) {
        
        String fromColonMsg=
                LanChat.decrypt
                (encryptedFromColonMsg,
                to+
                "OmidDeOpen_"
                + "insertintooffline_"
                + "msgvalues");
        
        String[] splitedFromColonMsg
                =fromColonMsg.split(":");
        

        final String from=splitedFromColonMsg[0];
        
        System.out.println("from : "+from);
        final String msg=splitedFromColonMsg[1];
        System.out.println("msg : "+msg);
        if (!IDTocName.containsKey(to))
            return;
        
        
        Thread t=new Thread(new Runnable() {

            @Override
            public void run() {
                
                sendMsg(from, to, msg);
                
            }
        });
        t.start();
        try {
            t.join(400);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        t.interrupt();
        
    }//end send messg offline
    
    private void addRowToOffMsg(String c1,String c2) {
        
            String sql_cmd="insert into offline_msg values "
                    + "(\'"+c1+"\',\'"+c2+"\');";
            
            executeSqlCmd(sql_cmd);
            
            System.out.println("sql : "+sql_cmd);
        
    }//end adding row
    
    private Profile loadProfile(String uName) {
        Profile p = null;
        try {
            ResultSet rs=
                    executeSqlCmdAndReadResult
                    ("select * from person "
                    + "where user_name"
                    + "=\'"+uName+"\'");
            if(!rs.next()){ 
                System.err.println("sql error server class load profile");
                System.exit(1);
            }
            String name=rs.getString("name");
            String fam=rs.getString("family");
            String fList=null;
            String banList=null;
            
//            int isBanListNull=rs.getInt("ban_list");
            
            if (!rs.wasNull())
                banList=rs.getString("ban_list");
            
//            int isFListNull=rs.getInt("ban_list");
            
            if (!rs.wasNull())
                fList=rs.getString("friend_list");
            
            HashSet<String> fSet=new HashSet<>();
            HashSet<String> banSet=new HashSet<>();
            
            for (String e:fList.split(",")){
                if (isUserValid(e))
                    fSet.add(e);
                else    
                    addOrRemoveFriendsOrBans(uName, e,"f","remove");
                
            }//end for
            
            
            for (String e:banList.split(",")){
                if (isUserValid(e))
                    banSet.add(e);
                else
                    addOrRemoveFriendsOrBans(uName, e,"b","remove");
            }//end for
            
            
            
            p=new Profile(name, fam, fSet, banSet);
            
            p.setUserName(uName);
            ArrayList<String> offlineMsg=new ArrayList<>();
            
            rs=
                    executeSqlCmdAndReadResult
                    ("select * from offline_msg "
                    + "where toThisUser"
                    + "=\'"+uName+"\'");
            
            while (rs.next()) {
                offlineMsg.add(rs.getString("msg"));
            }
            
            offMsgs.put
                    (uName, offlineMsg);
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return p;
        
    }
    

    private void sendMsg
            (String from,
            String to,String msg) {
        
        try {
            
            if (!isUserValid(from) || !isUserValid(to))
                 return;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (from.equals("server")){
            String eKey="MsgMsgDe0"
                + "PenServerSer"
                    + "<DeopenMaiL@g"
                    + "mail.com>"+to;
            msg=LanChat.encrypt(msg, eKey);
        }
        
        if (onlineList.contains(to))
            sendMsg
                    (IDTocName.get(to), "msg:"+from+":"+msg);
        else if (!to.equals("server"))
            saveThisOffMsg(to, from+":"+msg);
        
            
    }//end send msg
    
    private void initializeDB()
            throws ClassNotFoundException, SQLException {
        
        if(isDBExist()){
         
            return;
        }
        sqlConnection=
                        DriverManager.getConnection("jdbc:mysql://localhost/",sqlUsr,sqlPass);
        executeSqlCmd3306("CREATE DATABASE "+DATABASE_NAME);
        
        sqlConnection=
                        DriverManager.getConnection(SERVER_PATH,sqlUsr,sqlPass);
        ArrayList<String> ct=new ArrayList<>();
        
        ct.add(
                "user_name varchar(50) NOT NULL PRIMARY KEY");
        
        ct.add(
                "name varchar(50) NOT NULL");
        
        ct.add(
                "family varchar(50) NOT NULL");
        
        ct.add(
                "birth date NOT NULL");
        ct.add(
                "pass blob NOT NULL");
        
        ct.add(
                "friend_list blob");
        
        ct.add(
                "ban_list blob");
        
        createTbls("person",ct);
        
        ct.removeAll(ct);
        
        ct.add("toThisUser varchar(50) NOT NULL");
        ct.add("msg blob");
        

        
        createTbls("offline_msg",ct);
        
        
        
        
    }//end initializeDB
    
    
    private void createTbls(String tblName,ArrayList<String> cAndTyp) {
        
        String mainCmd="create table "+tblName+"(";
        
        for (String nt:cAndTyp) {
            
            mainCmd+=nt;
            
            if(!nt.equals(cAndTyp.get(cAndTyp.size()-1)))
                mainCmd+=",";
            else
                mainCmd+=");";
            
            
        }//end for nt
        
            System.out.println(mainCmd);
            executeSqlCmd(mainCmd);
    }//end create Tbl
    
    private boolean isDBExist() throws 
            ClassNotFoundException, SQLException {
        
        Connection connection;
        java.sql.Statement statement;
        Class.forName(JDBC_DRIVER);//load db driver class;
        
        connection=DriverManager.
                getConnection("jdbc:mysql://127.0.0.1:3306",sqlUsr,sqlPass);
        
        statement=(java.sql.Statement)connection.createStatement();
        DatabaseMetaData meta = (DatabaseMetaData) connection.getMetaData();
        ResultSet rs = meta.getCatalogs();
        
        while(rs.next())
            if (rs.getString("TABLE_CAT").equals(DATABASE_NAME))
                return true;

        
        return false;
    }
    
    private boolean isUserValid(String user_name) throws ClassNotFoundException, SQLException {
        
        if (user_name.equals("Unknown") 
                || 
                user_name.equals("server"))
            return true;
        
                String sql_cmd=
                "select * from person "
                + "where user_name=\'"+user_name+"\';";
        
        ResultSet resultSet=
                executeSqlCmdAndReadResult(sql_cmd);

        boolean result=resultSet.next();
        
        return result;
    }// end 
    

    private void executeSqlCmd3306(String cmd) {
        
        executeSqlCmd(cmd,"jdbc:mysql://127.0.0.1:3306");
        
    }//end execute as 3306
    
    private ResultSet executeSqlCmdAndReadResult(String cmd) 
            throws ClassNotFoundException, SQLException {
        return executeSqlCmdAndReadResult(cmd,SERVER_PATH);
    }//end execute and reading result on db
    
    private ResultSet executeSqlCmdAndReadResult(String cmd,String url) throws ClassNotFoundException, SQLException {
        
                
        
        java.sql.Statement statement;
        
        Class.forName(JDBC_DRIVER);//load db driver class;
            
        statement=(java.sql.Statement)sqlConnection.createStatement();
        

        ResultSet resultSet=
                statement.executeQuery(cmd);
        
        return resultSet;
        
    }//end execute sql command and reading result set
    
    
    private void executeSqlCmd(String cmd) {
        
        executeSqlCmd(cmd,SERVER_PATH);
        
    }//end execute as Server path
    
    private void executeSqlCmd(String cmd,String url) {
        
        java.sql.Statement statement;
        

        try 
        {
            
            Class.forName(JDBC_DRIVER);//load db driver class;
            
            statement=(java.sql.Statement)sqlConnection.createStatement();
            
            //query :
            
            PreparedStatement preparedStatement =
                    (PreparedStatement) sqlConnection.prepareStatement
                    (cmd);
            preparedStatement.execute(cmd);
            
        }catch(ClassNotFoundException | SQLException e)
        {
            
            System.err.println(e.getLocalizedMessage());
        }// end catch
        
    }
    
    private boolean authentication
            (String userName,String encryptedPass) {
        String pass=LanChat.decrypt
                (encryptedPass,
                "00Fr0gBugBugBugBugFr0g00"+
                userName);
        try {
            if (!isUserValid(userName))
                return false;
            
            ResultSet rs=
                    executeSqlCmdAndReadResult
                    ("select * from person "
                    + "where user_name="
                    + "\'"+userName+"\'");
            
            rs.next();

            String storedHashedPass=rs.getString("pass");
            String name=rs.getString("name");
            String family=rs.getString("family");
            String birth=rs.getString("birth");
            
            String[] splitedBirth=birth.split("-");
            
            
            if (splitedBirth[1].charAt(0)=='0')
                splitedBirth[1]=
                        splitedBirth[1].substring(1);
            
            
            if (splitedBirth[2].charAt(0)=='0')//even for year :v
                splitedBirth[2]=
                        splitedBirth[2].substring(1);
            
            birth=
                    splitedBirth[0]+"-"+
                    splitedBirth[1]+"-"+
                    splitedBirth[2];
            
            
            
            String salt=userName+"passg0rd";
            String thisPassHashed=
                    Sign_Up_Window.hash_this(pass, salt);
            salt=
                    userName.hashCode()+
                    name+family+
                    family.hashCode()+
                    birth+
                    thisPassHashed.hashCode();
            
            thisPassHashed=
                    Sign_Up_Window.hash_this
                    (thisPassHashed, salt);
            
            
            if (storedHashedPass.equals
                    (thisPassHashed))
                return true;
            
            
        } catch (NoSuchAlgorithmException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }//end authontication
    
    public int getAge(String userName) {
        try {
            if (!isUserValid(userName)){
                System.out.println("get age error , userName "+userName+" doesn't exist" );
                return -1;
            }//end if
            
            ResultSet res=executeSqlCmdAndReadResult
                    ("select birth from person "
                    + "where "
                    + "user_name='"+userName+"'");
            res.next();
            int bornYear=Integer.valueOf(res.getString("birth").split("-")[0]);
            DateFormat dateFormat = new SimpleDateFormat("yyyy");
            int currentYear=Integer.valueOf(dateFormat.format(new Date()));
            return currentYear-bornYear;
        } catch (NumberFormatException | 
                ClassNotFoundException | SQLException ex ) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        
    }//end getAge
        
    public String 
            addOrRemoveFriendsOrBans
            (String id,String item,String fOrB,String addOrRemove) {
        
        if (
                fOrB.toLowerCase().equals("f") && 
                fOrB.toLowerCase().equals("b"))
            return "";
        
        String column=fOrB.equals("f")?"friend_list":"ban_list";
        try {
            
            if (!isUserValid(id) || !isUserValid(item))
                return "notExist";
            
            ResultSet friendList=
                    executeSqlCmdAndReadResult
                    ("select "+column+" from person "
                    + "where user_name=\'"+id+"\'");
            friendList.next();            
            if (!friendList.wasNull()){
                HashSet<String> fSet=
                        new HashSet<>
                        (Arrays.asList
                        (friendList.getString
                        (column).split(",")));
                
                if (fSet.contains(item) && 
                        !addOrRemove.toLowerCase().equals("remove")) 
                    return "ItIsInYourList";
                else if (!fSet.contains(item) &&
                        addOrRemove.toLowerCase().equals("remove"))
                    return "ItIsntInYourList";
                
                if(addOrRemove.toLowerCase().equals("remove"))
                    fSet.remove(item);
                else
                    fSet.add(item);
                
                String fListForStore=hashSetToString(fSet, ",");
                System.out.println(fListForStore);
                executeSqlCmd
                        ("update person set "
                        + column+"=\'"+fListForStore+"\'"
                        + " where user_name=\'"+id+"\'");
                
                if (fOrB.toLowerCase().equals("b")){
                    addOrRemoveFriendsOrBans(id, item, "f", "remove");
                    addOrRemoveFriendsOrBans(item,id, "f", "remove");
                }
                System.out.println
                        (addOrRemove +" "+item+
                        (addOrRemove.equals("remove")?" from ":" to ")
                        +(fOrB.equals("f")?"friend ":"ban " )
                        +" >>> ok" ) ;
                return "ok";
            }//end if
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "";
    }//end add friend
    
    
    private void addRow
            (String name , String family ,
            String user,String birth,String pass) throws SQLException {
        
            String sql_cmd="insert into person values "
                    + "(\'"+user+"\',\'"+name+"\',\'"
                    +family+"\',\'"+birth+"\',\'"+pass+"\',\'\',\'\');";
            executeSqlCmd(sql_cmd);
            
            System.out.println("sql : "+sql_cmd);
        
        
    }// end adding row to database
    
    private void editRow
            (String name , String family ,
            String user,String birth,String pass) throws SQLException {
        
            String sql_cmd=
                    "update person set "
                    + "name=\'"+name+"\',"
                    + "family=\'"+family+"\',"
                    + "birth=\'"+birth+"\',"
                    + "pass=\'"+pass+"\'"
                    + "where "
                    + "user_name=\'"+user+"\';";
            executeSqlCmd(sql_cmd);
            
            System.out.println("sql : "+sql_cmd);
        
        
    }// end adding row to database
 
    public void sendMsg(String connectionName,String msg) {
        try{
            if (msg.equals("dc")) 
                System.out.println("request to disconnect for "+connectionName);
                    
        outStreams.get(connectionName).format("%s\n", msg);
        outStreams.get(connectionName).flush();
        }catch(Exception ex) {
            
        }
    }//end sending msg

    private String remveID(String id) {
        try {
            if (!isUserValid(id))
                return "id doesn\'t exist";
            
            executeSqlCmd
                    ("delete from person where"
                    + " user_name=\'"+id+"\'");
            
            executeSqlCmd
                    ("delete from offline_msg where"
                    + " toThisUser=\'"+id+"\'");
            
        } catch (ClassNotFoundException ex) {
            return 
                    "Class Not Found Error : "
                    +ex.getMessage();
        } catch (SQLException ex) {
            return "SQL Error : "+ex.getMessage();
        }
        
        return id+" deleted";
    }//end removeID
    
    private class log extends JFrame {

        private BufferedReader bufReader;
        private JTextArea logField;
        private JTextField cmdField;
        private ArrayList<String> cmdHistory;
        private int cmdToken;
        
        public log(InputStreamReader in) 
                throws HeadlessException {
            setTitle("[ ** Deopen LanChat Server Terminal ** ]");
            
            cmdHistory=new ArrayList<>();
            cmdHistory.add("license");
            cmdToken=0;
            
            bufReader=new BufferedReader(in);
            logField=new JTextArea();
            logField.setEditable(false);
            logField.setBackground(Color.BLACK);
            logField.setForeground(Color.GREEN);
            logField.setSelectionColor(Color.WHITE);
            logField.setFont(new Font("lucida console",0,13));
            cmdField=new JTextField();
            
            
            add(new JScrollPane(logField));
            add(cmdField,BorderLayout.SOUTH);
            
            
            cmdField.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cmdHistory.size()>100000)//are you kidding with me :-| ? no because Omid Is Sleepy ...
                        cmdHistory=new ArrayList<>();
                    append(e.getActionCommand());                    
                    cmdHistory.add(e.getActionCommand());
                    final ActionEvent fe=e;
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            processCmd(fe.getActionCommand().toLowerCase());                    
                        }
                    }).start();
                    
                    cmdField.setText("");
                    cmdToken=cmdHistory.size()-1;
                }
            });
            
            cmdField.addKeyListener(new KeyAdapter() 
            {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode()==38){                        
                        final String cmd=cmdHistory.get(cmdToken);
                        cmdToken--;
                        if (cmdToken<0)
                            cmdToken=cmdHistory.size()-1;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                cmdField.setText(cmd);
                            }
                        });
                    }//end if
                }
                
            });
            
            logField.setAutoscrolls(true);
            
            setMeVisiblePlz();
            cmdField.requestFocusInWindow();
            showLicense();
            
            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (isVisible()) {

                        try {
                            
                            String line=bufReader.readLine();
                            
                            if (line!=null && line.length()!=0)
                                append(line);
                            
                        } catch (IOException ex) {
                        }
                        
                    }
                }
            }).start();
            
        
        }//end 
        
        private void processCmd(String cmd) {
            
            switch (cmd.split(" ").length) {
                case 1:
                    switch (cmd) 
                    {
                        case "clear":
                            clearScrean();
                            break;
                        case "help":
                            showHelp();
                            break;
                        case "?":
                            showHelp();
                            break;        
                        case "\\?":
                            showHelp();
                            break; 
                        case "\\h":
                            showHelp();
                            break;  
                        case "cls":
                            clearScrean();
                            break;
                        case "license":
                            showLicense();
                            break;
                        case "about":
                            showLicense();
                            break;
                        case "new":
                            addNewMember();
                            break;
                        case "serverfinder":
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    new ServerFinder();
                                }
                            }).start();
                            break;
                        case "sf":
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    new ServerFinder();
                                }
                            }).start();
                            
                            break;
                        case "finder":
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    new ServerFinder();
                                }
                            }).start();
                            
                            break;
                        case "exit":
                            System.exit(0);
                            break;                            
                        case "quit":
                            System.exit(0);
                            break;
                        case "onlist":
                            showOnlineList();
                            break;
                        case "onlines":
                            showOnlineList();
                            break;                            
                        case "ons":
                            showOnlineList();
                            break;
                        case "add":
                            addNewMember();
                            break;
                        case "login":
                            addNewLogin();
                            break;
                        default:
                            showInfo(cmd);
                            break;
                        
                    }//end switch case cm
                    break;
                case 2:
                    String mainCmd=cmd.split(" ")[0].toLowerCase();
                    String arg=cmd.split(" ")[1];
                    switch (mainCmd){
                        
                        case "kick":
                            
                            if(arg.contains(".")){
                                try {
                                    String cn=InetAddress.getByName(arg).getHostName();
                                    if (cNameToID.containsKey(cn))
                                        sendMsg(cn,"dc");
                                    } catch (UnknownHostException ex) {
                                     Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                    }//end catch
                            }//end if 
                            else
                            {
                                String cn=IDTocName.get(arg);
                                if (cn!=null)
                                   sendMsg(cn,"dc");
                            }//end else
                        
                        case "add":
                            
                            if (arg.contains("member") || arg.contains("new")) 
                                addNewMember();
                            break;
                            
                        case "new":                            
                            if (arg.contains
                                    ("member") || 
                                    arg.contains("id")) 
                                addNewMember();
                            break;
                        case "remove":
                            append(remveID(arg));
                            break;
                        case "info":
                            showInfo(arg);
                            break;
                        case "show":
                            showInfo(arg);
                            break;
                        case "log":
                            LOg=arg.equals("0")?false:true;
                            append("Message Log Flag >>>>> "+LOg);
                            break;
                        
                            
                            
                    }//end switch case mainCmd
                    break;
                default:
                    mainCmd=cmd.split(" ")[0].toLowerCase();
                    String arg1=cmd.split(" ")[1];
                    String arg2=cmd.split(" ")[2];
                    
                    for (int i=3;i<cmd.split(" ").length;i++)
                        arg2+=" "+cmd.split(" ")[i];
                    
                    switch(mainCmd) {
                        
                        case "msg":
                            if (IDTocName.containsKey(arg1))
                                sendMsg("server", arg1, arg2);
                            break;
                        case "sendMsg":                            
                            if (IDTocName.containsKey(arg1))
                                sendMsg("server", arg1, arg2);
                            break;
                        case "sendto":                            
                            if (IDTocName.containsKey(arg1))
                                sendMsg("server", arg1, arg2);
                            break;
                        case "send":                            
                            if (IDTocName.containsKey(arg1))
                                sendMsg("server", arg1, arg2);
                            break;
                            
                        case "add":                            
                            if (arg1.contains("new") || arg2.contains("member")) 
                                addNewMember();
                            break;
                            
                        case "show":
                            if (arg1.contains("info"))
                                showInfo(arg2);
                            break;
                        
                    }//end switch case for main cmd
                    
                    
            }//end if switch case cmd length
            
        }//end process CMD
        
        private void addNewMember(){
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (cNameToID.containsKey(InetAddress.getByName("127.0.0.1").getHostName())){
                            System.out.println("Sorry , Your hostname is online !");
                            return;
                        }
                    } catch (UnknownHostException ex) {
                        return;
                    }
                    append
                            ("request for adding new member from server");
                    Client c=new Client("127.0.0.1");        
                    c.runClinet();
                    Sign_Up_Window sg=new Sign_Up_Window(c);

                }
            }).start();
            
            
        }//end addNewMember
        
        private void showHelp() {
            
            String help="List of all commands:\n\n";
            help+="msg [id] [msg]\t\tsend message to id\n";
            help+="info [id]\t\tshow information about id\n";
            help+="kick [ip]\t\tdisconnect ip if online\n";
            help+="kick [id]\t\tdisconnect id if online\n";
            help+="remove [id]\t\tremove id and it's information from database\n";
            help+="new \t\t\topen sign up window (with local host : 127.0.0.1)\n";
            help+="login \t\t\topen sign in window (with local host : 127.0.0.1)\n";
            help+="sf \t\t\topen server finder window\n";
            help+="ons \t\t\tshow online users\n";
            help+="clear \t\t\tclear screen\n";
            help+="lisesce \t\tshow license\n";
            help+="exit \t\t\tquit program\n";
            help+="log 0 \t\t\tDisable Message logger\n";
            help+="log 1 \t\t\tEnable Message logger\n";
            help+="help \t\t\tdisplay list of all commands\n\n";
            
            append(help);
            
        }//end show help
        
        private void addNewLogin(){
            
            new Thread(new Runnable() {

                        @Override
                        public void run() {
                    try {
                        if (cNameToID.containsKey(InetAddress.getByName("127.0.0.1").getHostName()))
                            return;
                    } catch (UnknownHostException ex) {
                        return;
                    }
                    append
                            ("request to show login window from server");
                    Client c=new Client("127.0.0.1");        
                    c.runClinet();
                    Login_Window sg=new Login_Window(c);

                }
            }).start();
            
        }//end addNewMember
        private void showOnlineList() {
            
            append
                    ("================ "
                    + "Online list "
                    +"==================\n");
                            
            for (String id:onlineList)
                append
                        ("[ id => "
                        +id
                        +" , hostName => "+
                        IDTocName.get(id)+" ]");
                                                        
            append
                ("================="
                + "==============="
                + "===============\n");
            
        }//end show online list
        
        private void showInfo(String user_name) {
            try {
                append("cheking is user valid ...");
                if (!isUserValid(user_name)){                    
                    append("user name doesn't exist !");
                    return;
                }
                append("user "+user_name+" is valid .");
                String spLineEnd=
                        "======================"
                        + "======================="
                        + "======================="
                        + "================";
                String spLineStart="================================  "
                        +user_name+" info  "
                        + "=====================================";
                
                while (spLineStart.length()<spLineEnd.length())
                    spLineStart+="=";
                
                append(spLineStart);
                String status=onlineList.contains(user_name)?"Online":"Offline";
                append ("Status : "+status);
                if (lastLogin.containsKey(user_name)){
                    Date now=new Date();
                    Date lastL=lastLogin.get(user_name);
                    long secAgo=
                            TimeUnit.MILLISECONDS.
                            toSeconds(now.getTime()-lastL.getTime());
                    long minuteAgo=
                            TimeUnit.MILLISECONDS.
                            toMinutes(now.getTime()-lastL.getTime());
                    long hoursAgo=
                            TimeUnit.MILLISECONDS.
                            toHours(now.getTime()-lastL.getTime());
                    long daysAgo=
                            TimeUnit.MILLISECONDS.
                            toDays(now.getTime()-lastL.getTime());
                    String lastLoginAgo="Last login at ";
                    lastLoginAgo+=daysAgo==0?"":", "+daysAgo%60+" days ";
                    lastLoginAgo+=hoursAgo==0?"":", "+hoursAgo%60+" hourse ";
                    lastLoginAgo+=minuteAgo==0?"":", "+minuteAgo%60+" minutes ";                    
                    lastLoginAgo+=secAgo==0?"":", "+secAgo%60+" seconds ";
                    lastLoginAgo+=" ago ";
                    if ((minuteAgo+hoursAgo+daysAgo+secAgo)>0)
                        append(lastLoginAgo);
                }//end if 
                if (onlineList.contains(user_name))
                    append("Host name : "+IDTocName.get(user_name));
                
                Profile p=loadProfile(user_name);
                
                append("Full Name : "+p.getFullName());
                append("Friend List : "+p.getFriendList());
                append("Ban List : "+p.getBanList());
                append ("Age : "+getAge(user_name));
                
                append(spLineEnd);
            } //end show info
            catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }//end show info
        
        private void showLicense() {
            append(LanChat.license+"\n");            
        }//end show help
        private void clearScrean() {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    logField.setText("");
                }
            });
        }//end clear
         
        private void append(final String line) {
            
            
            
            SwingUtilities.invokeLater(new Runnable() {

                
                @Override
                public void run() {
                    
                    logField.append(line+"\n");
                    
                }
            });
            
        }//end append
        private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        
        setSize(700,410);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
    
        }// set Me ... end 
        
    }//end inner class log
    
}
