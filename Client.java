/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import static lanchat.ImpConsts.chat_port_number;
import static lanchat.LanChat.hashSetToString;
import static lanchat.LanChat.playSound;
/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class Client {
    
     
    private Formatter            out_stream;
    private BufferedReader    in_stream;
    private String chatServer;
    private Socket connection;
    private Profile profile;
    private boolean isProfileSet=false;
    boolean socketFaild=true;
    
    private boolean lockInStream=false;
    private String lockString;
    private HashMap<String,ClientChatWin> chatWins;
    public HashMap<String,String> otherMsgTypes;
    public HashMap<String,TicTacToeClient> xoMap;
    public HashMap<String,White_board> boardsMap;
    private HashSet<String> onConfrances;
    Thread listening;
    Thread updating;
    public Client(String host) {
        
        otherMsgTypes=new HashMap<>();
        xoMap=new HashMap<>();
        boardsMap=new HashMap<>();
        chatServer=host;
        onConfrances=new HashSet<>();
        
    }// end generator

    
    public void sendMsg(String msg) {
        try{
            out_stream.format("%s\n",msg);
            out_stream.flush();        
        }catch(Exception ex) {
            
        }
    }// end send msg

    public String getMsg() throws IOException {
        
        if (lockInStream){
            synchronized(this) {
                try {
                    this.wait(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return getMsg();
        }//end if lock
        else {
            String msg=in_stream.readLine();
//            System.out.println("client msg : "+msg+" readed ");
            return msg;
        }
    }// end process connection
    
    
    public String getMsg(String lockStr) throws IOException {
        
        if (lockInStream && lockStr.equals(lockString)) {
            String msg=in_stream.readLine();
//            System.out.println("client msg (locked) : "+msg+" readed ");
            return msg;
        }
        else 
            return null;
        
    }//end getMsg (lock)
    
    public void closeConnection() {
        try {
            
            for (String conf:onConfrances)
                removeConfWin(conf);
            
            sendMsg("dc");            
            out_stream.close();
            in_stream.close();
            connection.close();
            socketFaild=true;
        } // end closing connection
        catch (IOException ex) {
            Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }// end closing connection
    
    
    private boolean getStreams() throws IOException {
        
//        out_stream=
//                new ObjectOutputStream(connection.getOutputStream());
        out_stream=
                new Formatter(connection.getOutputStream());
        out_stream.flush();// for sending Object Header to InputStream

        in_stream=
                new BufferedReader
                (new InputStreamReader(connection.getInputStream()));
        
        return true;
        
    }//end get Streams
    
    private boolean connectToServer() 
            throws  IOException ,ConnectException {
        
        try{
        connection=
                new Socket(InetAddress.getByName(chatServer),chat_port_number );
        }catch (UnknownHostException | ConnectException ex) {            
            return false;
        }
        
        return true;
    }//end connecting to server
    
    public void runClinet() {
        
        try{
            socketFaild=!connectToServer();
            if (!socketFaild) {
            getStreams();
            System.out.println("Client Established .");
            }//end if connect
        }catch(EOFException  e) {
            System.out.println
                ("\n"+"Client terminated connection");
        }catch(IOException e) {
            e.printStackTrace();
        }// end catchs
        
    }// end run ClientChatWin
    
    
    public String getHostAddress() {
        
        if (isConnected())
            return chatServer;
        else
            return null;
    }// end getHostAddress
    public boolean isConnected() {
        
        if (!socketFaild)
            return connection.isConnected();
        else
            return false;
        
    }//end is connected
    
    public void lockGetMsg(String lockStr) {
        if(isProfileSet){
            
        }
        while (lockInStream) {
            synchronized(this) {
                try {
                    this.wait(80);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        try {
            in_stream=
                    new BufferedReader
                    (new InputStreamReader(connection.getInputStream()));            
            lockInStream=true;
            lockString=lockStr;
        } // end lockGetMsg
        catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }// end lockGetMsg
    
    
    
    public void unlockGetMsg(String lockStr) {
        
        if (lockStr.equals(lockString)) {
            try {
                in_stream=
                        new BufferedReader
                        (new InputStreamReader(connection.getInputStream()));
                
                lockString=null;
                lockInStream=false;
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else
        {
            System.err.println
                    ("you can't unlock getMsg,your lock value is wrong.");
        }
        
    }//end unlockGetMSg

    
    public void startListening() {
    
        listening=new Thread(new Runnable() {

            @Override
            public void run() {
                if (lockInStream)
                    System.out.println
                            ("InStream is lock , "
                            + "logical error will happend "
                            + "in start listening client");
                sendMsg
                        ("giveMeMyOff:"+profile.getUserName()+":to:msg");
                
                while(isConnected()) {
                    //start wait

                    try {
                        
                        if (lockInStream)
                            continue;
                        
                        String msg=getMsg();
                        
                        if (msg==null)
                            continue;
                        
                        if (msg.equals("dc"))
                            closeConnection();
                        
                        processMsg(msg);

                
                    } catch (IOException ex) {
//                        Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch
                    
                    
            
        }//end while
            }//end run
        });
        listening.start();
        updating=
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(isConnected()) {
                    synchronized (Client.this) {
                        try {
                            Client.this.wait(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    String myFList=
                            hashSetToString
                            (getProfile().getFriendList(), ",");                                       
                    sendMsg("updateOnList:from:to:"+myFList);
                    
                }//end for while
            }//end run
        });
        updating.start();
        
    }// end listening to client
    
    
    
    public Profile getProfile() {
        
        return profile;
        
    }//end initialize
    
    
    private void processMsg(String msg) {
        
        if (msg.split(":").length<2)
            return;
        

        String[] splitedMsg=msg.split(":");
        String mainMsg="";
        for (int i=2;i<splitedMsg.length;i++)
            mainMsg+=splitedMsg[i];
        
        String msgType=splitedMsg[0];
        String from=splitedMsg[1];        
        
        
        
        if (getProfile().getBanList().contains(from))
            return;
        
        switch (msgType) {
            case "msg":
                
                
                String eKey="MsgMsgDe0"
                + "PenDeOPenOmid";
                
                if (from.equals("server"))
                    eKey="MsgMsgDe0"
                        + "PenServerSer"
                        + "<DeopenMaiL@g"
                        + "mail.com>";
                
                eKey+=getProfile().getUserName()+from;        
                String decryptedMsg=
                LanChat.decrypt
                    (mainMsg, eKey);
                
                
                if (!chatWins.containsKey(from)){
                    addChatWin(from);
                    
                    try {
                       LanChat.playSound("msg");
                    } catch (UnsupportedAudioFileException
                            | IOException | LineUnavailableException ex) {
                    Logger.getLogger(ClientChatWin
                            .class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch
                
                
                }//end if
                else if (!chatWins.get(from).isFocused()) {
                    
                    try {
                       LanChat.playSound("msg");
                    } catch (UnsupportedAudioFileException 
                            | IOException | LineUnavailableException ex) {
                    Logger.getLogger(ClientChatWin
                            .class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch
                    
                }//end else if
                chatWins.get(from).
                        showMsg(from+": "+decryptedMsg+"\n");
                break;
            case "confMsg":
                String confID=mainMsg.split(",")[0];
                eKey="MsgMsgDe0"
                + "PenDeOPenOmid"
                + "ConfranceYes"
                + "ConfranceMySon:D"+
                confID+from;
                decryptedMsg=
                LanChat.decrypt
                    (mainMsg.split(",")[1], eKey);
                
                if (!chatWins.get(confID).isFocused()) {
                    
                    try {
                       LanChat.playSound("msg");
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                    Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch
                    
                }//end else if
                chatWins.get(confID).
                        showMsg(from+": "+decryptedMsg+"\n");
                break;
            case "updateOnList":
                
                String onList=mainMsg;     
                
                HashSet<String> newOn=
                        new HashSet<>( Arrays.asList(onList.split(",")) );
                HashSet<String> 
                        exOn=new HashSet<>(getProfile().getOnList());
                    
                for (String e:newOn) {
                    
                    if (!exOn.contains(e) && e.length()>0){
                        try {
                            playSound("on");
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        break;
                    }//end if
                    
                }//end for e
                
                for (String e:exOn) {
                    
                    if (!newOn.contains(e) 
                            && e.length()>0){
                        
                        try {
                            playSound("off");
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        break;
                    }
                    
                }//end for e
                
                if (!newOn.isEmpty())
                    getProfile().setOnList(newOn);                 
                
                break;
            case "reqXO":
                final String f=from;
                final Client fc=this;
                new Thread(new Runnable() {

                @Override
                public void run() {
                    xoMap.put(f,new TicTacToeClient(f,"O", fc)); 
                }
                }).start();
                    
                break;
            case "locXO":
                System.out.println("((Client)) --> "
                        + "my name : "+getProfile().getUserName()+" my msg :"+mainMsg+" from : "+from);
                if (xoMap.containsKey(from))
                    xoMap.get(from).setMark(Integer.valueOf(mainMsg));          
                break;
            case "closeXO":
                if (xoMap.containsKey(from)){
                    xoMap.get(from).close();
                    xoMap.remove(from);
                }
                break;
            case "runBoard":
                boardsMap.remove(from);
                boardsMap.put(from, new White_board(this, from));
                break;
            case "boardAdd":
                if (!boardsMap.containsKey(from))
                    break;
                String[] spilitedMainMsg=mainMsg.split(";");
                int x,y,size;
                for (String p:spilitedMainMsg){
                     x=Integer.valueOf(p.split(" ")[0]);
                     y=Integer.valueOf(p.split(" ")[1]);
                     size=Integer.valueOf(p.split(" ")[2]);
                     boardsMap.get(from).points.add(new Point(x, y));
                     boardsMap.get(from).pointSizes.put
                        (boardsMap.get(from).points.size()-1, size);
                     boardsMap.get(from).reqToPaint();
                }//end for
//                    boardsMap.put(from, new White_board(this, from));
                
                break;
            case "boardClose":
                if (boardsMap.containsKey(from)){                    
                    boardsMap.get(from).dispose();
                    boardsMap.remove(from);
                }
                break;
            case "boardRemove":
                if (!boardsMap.containsKey(from))                  
                        break;
                spilitedMainMsg=mainMsg.split(";");
                for (String p:spilitedMainMsg){
                    x=Integer.valueOf(p.split(" ")[0]);
                    y=Integer.valueOf(p.split(" ")[1]);                    
                    boardsMap.get(from).reqToRemove(new Point(x, y));
                    boardsMap.get(from).reqToPaint();
                }//end for
                break;
            case "runConf":
                addConfWin("confrance,"+mainMsg);     
                System.out.println("((Client)) --> "+msg);
                break;
            case "newConfUsers":
                confID=from;
                chatWins.get(confID).confranceListElements.removeAllElements();
                for (String id:mainMsg.split(","))
                    chatWins.get(confID).confranceListElements.addElement(id);
                break;
            default:
                otherMsgTypes.put(msgType, mainMsg);
                System.out.println("((Client)) --> other msg type "+msgType+" msg : "+msg+ " stored ");
                break;
                    
                
        }
        
        
        
        
    }//end process MSg
    
    public void addChatWin(String from) {
        if (!chatWins.containsKey(from))
            chatWins.put(from,
                            new ClientChatWin(this,from));
        
    }//end addChatWin
    
    public void addConfWin(String from) {
        
        String confranceID=from.split(",")[1];
        onConfrances.add(confranceID);
        if (!chatWins.containsKey(confranceID))
            chatWins.put(confranceID,
                            new ClientChatWin(this,from));
        
    }//end addChatWin
    
    public void removeChatWin(String from) {
        chatWins.get(from).dispose();
        chatWins.remove(from);
        
    }
    
    public void removeConfWin(String confID){
        sendMsg("rmConf:"+getProfile().getUserName()+":to:"+confID);
        chatWins.get(confID).dispose();
        chatWins.remove(confID);
        onConfrances.remove(confID);
    }
    
    public void setProfile(Profile p) {
        
        profile=p;
        isProfileSet=true;
        chatWins=new HashMap<>();
        
    }//end initialize
    
}
