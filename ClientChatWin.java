/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Omid Yaghoubi 
 * @author DeOpenMail@Gmail.com
 */



public class ClientChatWin extends JFrame{
    
        
    private JTextField                   txtMsg;
    private JTextPane                  txtInfo;
    private Client client;
    String from;
    boolean isGroupChat=false;
    boolean isConfrance=false;
    private JList confracneList;
    public DefaultListModel confranceListElements;
    private JButton inviteFriendToConf;
    private String confID;
    public ClientChatWin(final Client client,final String from) throws HeadlessException {
        
        
        if (from.contains("confrance,")){
            isConfrance=true;
            String[] splitedFrom=from.split(",");
            confranceListElements=new DefaultListModel();
            confracneList=new JList(confranceListElements);
            
            JPanel confrancePanel=new JPanel(new BorderLayout());            
            
            confrancePanel.add(confracneList,BorderLayout.CENTER);
            confracneList.setBackground(Color.ORANGE);
            ImageIcon coffee_ico=new ImageIcon(Login_Window.icon_resource_path+"coffee.png");
            JLabel lblTtile=new JLabel("",coffee_ico,JLabel.CENTER);//("Members",SwingConstants.CENTER);
            lblTtile.setOpaque(true);
            lblTtile.setBackground(Color.ORANGE);
           
            lblTtile.setFont(new Font("lucida console",1,14));
            confrancePanel.add(lblTtile,BorderLayout.NORTH);
            inviteFriendToConf =new JButton("Invite Friend");
            confrancePanel.add
                    (inviteFriendToConf
                    ,BorderLayout.SOUTH);
            confID=splitedFrom[1];
            for (int i=2;i<splitedFrom.length;i++)
                confranceListElements.addElement(splitedFrom[i]);            
            add(confrancePanel,BorderLayout.EAST);
            confracneList.addMouseListener(new MouseAdapter() 
            {

                @Override
                public void mouseClicked(MouseEvent e) {
                    JList l=(JList)e.getSource();
                    
                    if (e.getClickCount()>=2 && !l.isSelectionEmpty()){
                        int i=l.getSelectedIndex();
                        String user=(String) confranceListElements.get(i);
                        client.addChatWin(user);
                    }
                    
                }
                
            });
            inviteFriendToConf.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(new Runnable() {

                        @Override
                            public void run() {
                                ArrayList<String> listForInvite=new ArrayList<>();
                                for (String id:client.getProfile().getOnList()){
                                    if (!confranceListElements.contains(id))
                                        listForInvite.add(id);
                                }//end for

                                String idToInvite=
                                        new ListChooser(listForInvite," invite","Invite Friend")
                                        .getSelectedValue();

                                client.sendMsg
                                        ("addConf:"+
                                        client.getProfile().getUserName()
                                        +":to:"+idToInvite+","+confID);

                        }
                    }).start();
                    
                }//end acrtion performed
            });
            
        }//end if conf
        
        
        if (from.split(",").length>1 && !isConfrance)
            isGroupChat=true;        
        
        if (!isGroupChat && !isConfrance && 
                !client.getProfile().getFriendList().contains(from) && 
                !from.equals("server")){
            
            final JPanel northPanel=new JPanel(new GridLayout(1, 3));
            ImageIcon friend_ico=new ImageIcon(Login_Window.icon_resource_path+"plus.png");
            ImageIcon block_ico=new ImageIcon(Login_Window.icon_resource_path+"block.png");
            JButton addFriend=new JButton("ADD USER",friend_ico);
            JButton banUser = new JButton("BAN USER",block_ico);    
            addFriend.setFont(new Font("Times Roman",0,15));
            banUser.setFont(new Font("lucida console",0,15));
            
            northPanel.add(addFriend);
            northPanel.add(new JLabel());            
            northPanel.add(banUser);            
            add(northPanel,BorderLayout.NORTH);
            
            addFriend.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    client.sendMsg
                            ("add:"+client.getProfile().
                            getUserName()+":to:"+
                            from
                            );
                    client.getProfile().
                            addToFriendList(from);
                    
                    remove(northPanel);
                    setVisible(true);
                }
            });
            
            banUser.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    client.sendMsg
                            ("ban:"+client.getProfile().
                            getUserName()+":to:"+
                            from
                            );
                    client.getProfile().addToBanList(from);
                    client.removeChatWin(from);
                }
            });
            
        }
        
        
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                
                while(client.isConnected()) {
                    
                    synchronized (ClientChatWin.this) {
                        try {
                            ClientChatWin.this.wait(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }//end synch
                    
                }//end while
                
                dispose();
            }//end run
        }).start();
        
        this.client=client;
        this.from=from;
        if (!isConfrance)
            setTitle(from);
        else
            setTitle("Confrance "+from.split(",")[1]);
        
        txtMsg=new JTextField();        
        txtMsg.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                
                    sendMsg(e.getActionCommand(),from);
                    txtMsg.setText("");
                
            }// end action perfomed
        });// end add action listener
        
        add(txtMsg,BorderLayout.SOUTH);
        txtInfo=new JTextPane();
        txtInfo.setEditable(false);
        
        add(new JScrollPane(txtInfo),BorderLayout.CENTER);
       
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                if (!isConfrance)
                    client.removeChatWin(from);
                else
                    client.removeConfWin(confID);
            }
            
        });
        
        setMeVisiblePlz();
        
        txtMsg.requestFocusInWindow();
        
        
    }// end generator
    
    
     
    private void sendMsg(String msg,String to) {
        
        if (to.split(",").length>1 && !isConfrance){            
            groupMsg(msg, to);
            return;
        }
        
        if (isConfrance){
            sendConfMsg(msg);
            return;
        }
        String prifix="msg:"+getUserName()+":"+to+":";
        
       
        String eKey="MsgMsgDe0"
                + "PenDeOPenOmid"+to+getUserName();
        
        if (!to.equals("server"))
            client.sendMsg(prifix+LanChat.encrypt(msg,eKey));
        else{
            eKey="SerrrrrrrrDeOpeNOMidddd99**64##@#$ ???"+getUserName();
            client.sendMsg("server"+prifix+LanChat.encrypt(msg,eKey));
        }
        if (!isGroupChat)
            showMsg(getUserName()+": "+msg+"\n");        
        
    }// end send msg
    
    
    private void sendConfMsg(String msg) {
        
        String prifix="msgConf:"+getUserName()+":"+confID+":";
                
        String eKey="MsgMsgDe0"
                + "PenDeOPenOmid"
                + "ConfranceYes"
                + "ConfranceMySon:D"
                +confID+getUserName();
        
        client.sendMsg(prifix+LanChat.encrypt(msg,eKey));
        
    }//end 
    
    private void groupMsg(String msg,String ppl) {
        
        for (String person:ppl.split(","))
            sendMsg(msg,person);
        
        
        showMsg(getUserName()+": "+msg+"\n");
        
    }//end group msg
    
    public void showMsg(final String msg) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String[] splited=msg.split(":");

                if (splited.length>2)
                    for (int i=2;i<splited.length;i++)
                        splited[1]+=":"+splited[i];
                
                SimpleAttributeSet bold = new SimpleAttributeSet();
                StyleConstants.setBold(bold, true);
                SimpleAttributeSet normal = new SimpleAttributeSet();
                
                if (splited[0].equals("server"))
                    StyleConstants.setForeground(bold, Color.red);
                
                try {
                    
                    txtInfo.getDocument().
                            insertString(txtInfo.getDocument().getLength(),
                            splited[0]+" : ", bold);
                    txtInfo.getDocument().
                            insertString(txtInfo.getDocument().getLength(),
                            splited[1], normal);
                    
                } catch (BadLocationException ex) {
                    Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
    

        
    }// end showing msg
    

    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(480,300);
        setLocationRelativeTo(null);        
        setDefaultCloseOperation
                (WindowConstants.DO_NOTHING_ON_CLOSE);
        setVisible(true);

        
    }// set Me ... end 
    
    public String getUserName (){
        
        
        return client.getProfile().getUserName();

        
    }//end get user name from current Profile
    
    
}
