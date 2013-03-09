/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class UserMainWin extends JFrame {

    private boolean on;
    
    
    private JButton settingBtn;
    private JButton editBanListBtn;
    private JButton findFriendBtn;
    private JButton signOutBtn;
    private JList friendList;
    private DefaultListModel friendListElements;
    private Client client;
    private JPopupMenu singlePopUpMenu;
    private JPopupMenu multiplePopUpMenu;
    public UserMainWin(final Client client) throws HeadlessException {
    
        super("[: LAN-CHAT :]");
        
        JMenuItem sendMsgMnu = new JMenuItem( "Send Message" );
        JMenuItem banUserMnu = new JMenuItem( "Ban User" );
        JMenuItem removeUserMnu = new JMenuItem( "Remove User" );
        JMenuItem playXOUserMnu = new JMenuItem( "Play XO" );
        JMenuItem whiteBoardUserMnu = new JMenuItem( "Execute WhiteBoard" );
        JMenuItem sendGroupMsgUserMnu = new JMenuItem( "Send Group Message" );
        JMenuItem removeSelectedUserMnu = new JMenuItem( "Remove All Selected User" );
        JMenuItem startConfUserMnu = new JMenuItem( "Start Conferance" );
        ImageIcon block_ico=new ImageIcon(Login_Window.icon_resource_path+"block.png");
        ImageIcon msg_ico=new ImageIcon(Login_Window.icon_resource_path+"msg.png");
        ImageIcon groupMsg_ico=new ImageIcon(Login_Window.icon_resource_path+"groupMsg.png");
        ImageIcon delete_ico=new ImageIcon(Login_Window.icon_resource_path+"delete.png");
        ImageIcon paint_ico=new ImageIcon(Login_Window.icon_resource_path+"paint_brush.png");
        ImageIcon game_ico=new ImageIcon(Login_Window.icon_resource_path+"games.png");
        ImageIcon coffee_ico=new ImageIcon(Login_Window.icon_resource_path+"coffee.png");
        ImageIcon setting_ico=new ImageIcon(Login_Window.icon_resource_path+"process.png");
        ImageIcon shutdown_ico=new ImageIcon(Login_Window.icon_resource_path+"shut_down.png");
        ImageIcon search_ico=new ImageIcon(Login_Window.icon_resource_path+"search.png");
        ImageIcon bin_ico=new ImageIcon(Login_Window.icon_resource_path+"bin.png");
        
        sendMsgMnu.setIcon(msg_ico);
        sendGroupMsgUserMnu.setIcon(groupMsg_ico);
        banUserMnu.setIcon(block_ico);
        removeUserMnu.setIcon(delete_ico);
        removeSelectedUserMnu.setIcon(delete_ico);
        whiteBoardUserMnu.setIcon(paint_ico);
        playXOUserMnu.setIcon(game_ico);
        startConfUserMnu.setIcon(coffee_ico);
        
        singlePopUpMenu=new JPopupMenu("Menu");
        singlePopUpMenu.add(sendMsgMnu);
        singlePopUpMenu.add(playXOUserMnu);
        singlePopUpMenu.add(whiteBoardUserMnu);
        singlePopUpMenu.add(removeUserMnu);
        singlePopUpMenu.add(banUserMnu);
        
        multiplePopUpMenu=new JPopupMenu("Menu");
        multiplePopUpMenu.add(sendGroupMsgUserMnu);
        multiplePopUpMenu.add(startConfUserMnu);
        multiplePopUpMenu.add(removeSelectedUserMnu);
        
        this.client=client;
        final JLabel userHeader=new JLabel(client.getProfile().getFullNamePlusUserName(),SwingConstants.CENTER);
        JLabel ipStat=new JLabel("Server : "+client.getHostAddress(),SwingConstants.CENTER);
        friendListElements=new DefaultListModel();
        userHeader.setFont(new Font("tahoma", Font.BOLD, 14));
        userHeader.setText(client.getProfile().getFullNamePlusUserName());
        
        
        JPanel nPanel=new JPanel(new BorderLayout());
        
        nPanel.add
                (userHeader,BorderLayout.NORTH);
        nPanel.add
                (ipStat,BorderLayout.SOUTH);
        
        add(nPanel,BorderLayout.NORTH);
        
        for (String e:client.getProfile()
                .getFriendList())
            friendListElements.addElement(e);
        
        friendList=new JList(friendListElements);
        
        friendList.setToolTipText("Friend List");
        
        friendList.setCellRenderer(new fList(client.getProfile().getOnList()));
        
        
        
        add(new JScrollPane(friendList),BorderLayout.CENTER);
        

        JPanel southPanel=new JPanel();
        JPanel optionsPanel=new JPanel();
        
        
        optionsPanel.setLayout(new FlowLayout());
        
        settingBtn=new JButton();
        settingBtn.setToolTipText("Edit Profile");
        settingBtn.setIcon(setting_ico);
        optionsPanel.add(settingBtn);
        
        editBanListBtn=new JButton();
        editBanListBtn.setToolTipText("Edit BanList");
        editBanListBtn.setIcon(bin_ico);
        optionsPanel.add(editBanListBtn);
        
        editBanListBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {                        
                        String idForRemoveFromBan=
                        (new ListChooser(new ArrayList<>(client.getProfile().getBanList())
                                , " Remove From Ban List","Edit Ban List")).getSelectedValue();
                        client.sendMsg("removeBan:"+
                        client.getProfile().getUserName()+":to:"+idForRemoveFromBan);
                        client.getProfile().removeFromBan(idForRemoveFromBan);
                    }
                }).start();
                
                
            }
        });
        
        findFriendBtn=new JButton();
        findFriendBtn.setToolTipText("Find Friend");
        findFriendBtn.setIcon(search_ico);
        optionsPanel.add(findFriendBtn);
        
        signOutBtn=new JButton();
        signOutBtn.setToolTipText("Sign out");
        signOutBtn.setIcon(shutdown_ico);
        optionsPanel.add(signOutBtn);
        
        signOutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                signOut();
            }
        });
        
        southPanel.setLayout(new BorderLayout());        
        southPanel.add(optionsPanel,BorderLayout.CENTER);
        
        add(southPanel,BorderLayout.SOUTH);
        
        friendList.setSelectionMode(
                    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        friendList.addMouseListener(new MouseAdapter() 
        {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList l=(JList)e.getSource();            
                
            if (l.getSelectedIndices().length<=1){                    
                    if (!SwingUtilities.isRightMouseButton(e)){
                        if (e.getClickCount()>=2 ) {
                            int i=l.locationToIndex(e.getPoint());
                            try{
                            String user=(String) friendListElements.get(i);
                            client.addChatWin(user);
                            }catch(ArrayIndexOutOfBoundsException ex) {
                                //i'm Empty And I Know IT!
                            }
                        }//end if count >=2
                    }//end if it isn't pop up
                    else {
                        l.setSelectedIndex(l.locationToIndex(e.getPoint()));
                        singlePopUpMenu.show(l, e.getX(), e.getY());
                    }//end if is pop up                    
                }//end if single
            else {
                //if count more than 1
                    if (SwingUtilities.isRightMouseButton(e)) {
                        multiplePopUpMenu.show(e.getComponent(),e.getX()+50,e.getY());
                        
                    }//end if right click
            }//end if not single
            }//end mousCLicked

            @Override
            public void mouseReleased(MouseEvent e) {
                JList l=(JList)e.getSource();
                if (l.getSelectedIndices().length<=1){  
                    if (SwingUtilities.isRightMouseButton(e)){                           
                        l.setSelectedIndex(l.locationToIndex(e.getPoint()));
                        singlePopUpMenu.show(l, e.getX(), e.getY());
                    }//end if is metaa down ...
                }//end if selection is 1
                else{
                //if count more than 1
                    if (SwingUtilities.isRightMouseButton(e)) {
                        multiplePopUpMenu.show(e.getComponent(),e.getX()+50,e.getY());
                    }//end if right click
                }
            }//end mouse released0

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                
                JList l=(JList)e.getSource();
                
                
            }//end mouse wheel moved
          
            
            
        });
        
        sendMsgMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!friendList.isSelectionEmpty()) {
                    int i=friendList.getSelectedIndex();
                    String user=(String)friendListElements.get(i);
                    client.addChatWin(user);
                }
            }
        });
        
        sendGroupMsgUserMnu
                .addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!friendList.isSelectionEmpty()) {
                    String users="";             
                    
                    for (int index:friendList.getSelectedIndices()) {
                        
                        users+=friendListElements.get(index)+",";
                        
                    }//end for 
                    
                    if (users.length()>=1)
                        users.substring(0,users.length()-1);
                    
                    client.addChatWin(users);
                }
            }
        });
        
        removeUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i=friendList.getSelectedIndex();
                String user=(String)friendListElements.get(i);
                reqForRemove(user);
            }//end action perfomed -->
            /*یادگاری از امید یعقوبی*/
            //yaadegaari 5:43 Am 18 bahman :P ---Omid DeOPen---
        });
        
        
        removeSelectedUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                    for (int index:friendList.getSelectedIndices())
                        reqForRemove((String)friendListElements.get(index));
                   
            }//end action perfomed -->
            /*یادگاری از امید یعقوبی*/
            //yaadegaari 5:43 Am 18 bahman :P ---Omid DeOPen---
        });
        
        
        banUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i=friendList.getSelectedIndex();
                String user=(String)friendListElements.get(i);
                reqForBan(user);
            }//end action perfomed -->
            /*یادگاری از امید یعقوبی*/
            //yaadegaari 5:43 Am 18 bahman :P ---Omid DeOPen---
        });
        
        findFriendBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try{
                            new Find_Friend(client);
                        }catch(ArrayIndexOutOfBoundsException ex) {
                            new Find_Friend(client);
                        }//end catch
                    }
                }).start();
            }
        });
        
        playXOUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i=friendList.getSelectedIndex();
                String user=(String)friendListElements.get(i);
                client.xoMap.put(user,new TicTacToeClient(user, "X", client));
            }
        });
        whiteBoardUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i=friendList.getSelectedIndex();
                String user=(String)friendListElements.get(i);
                if (client.getProfile().getOnList().contains(user)){
                    client.sendMsg("runBoard:"+client.getProfile().getUserName()+":"+user+":msg");
                    client.boardsMap.put(user,new White_board(client, user));
                }
                else
                    JOptionPane.showMessageDialog(null,"Sorry, "+user+" is offline !");
            }
        });
        
        startConfUserMnu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String users="";
                
                for (int index:friendList.getSelectedIndices())
                    users+=(String)friendListElements.get(index)+",";
                
                users+=client.getProfile().getUserName();
                
                client.sendMsg("reqConf:from:to:"+users);
                
            }
        });
        
        settingBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Sign_Up_Window(client,"Edit:"+
                        client.getProfile().getUserName());
            }//end action performed
        });//end AddActionListener
        
        
        
        setMeVisiblePlz();
        client.startListening();
        
                new Thread(new Runnable() {

            @Override
            public void run() {
                while(client.isConnected()){
                    
                    synchronized (UserMainWin.this) {
                        try {
                            UserMainWin.this.wait(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientChatWin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        }//end synchs
                        
                            if (!client.getProfile().
                                getFullNamePlusUserName().
                                equals(userHeader.getText()))
                            userHeader.setText(client.getProfile().getFullNamePlusUserName());
                        
                        if (client.getProfile().getUpdateFList() ){
                        friendListElements.removeAllElements();
                        for (String e:client.getProfile()
                            .getFriendList())                            
                                friendListElements.addElement(e);
                        friendList.setVisible(true);//for update on list
                            System.out.println("*********** up friendList *************");
                        
                        }//end if
                        if (client.getProfile().getUpdateOnList()) {
                            System.out.println("************ up onlist ****************");
                        friendList.setCellRenderer(new fList(client.getProfile().getOnList()));
                        friendList.setVisible(true);//for update on list
                        }
                        
                }//end while
                signOut();
            }
        }).start();
                
                
    }// edn Generator
    
    
    private void signOut() {
        
        setVisible(false);
        client.closeConnection();
        if (Server.isServerRun)
            dispose();
        else
            System.exit(0);
    }
    
    private void reqForRemove(String user) {
        
        client.sendMsg
                        ("remove:"
                        +client.getProfile().
                        getUserName()+":to:"+user);
        client.getProfile().removeFromFriendList(user);
        
    }//end req for remove
    
    private void reqForBan(String user) {
        
        client.sendMsg
                        ("ban:"
                        +client.getProfile().
                        getUserName()+":to:"+user);
        client.getProfile().addToBanList(user);
        
    }//end req for ban
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        pack();
        setSize(getWidth(),400);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                client.closeConnection();
            }
            
        });
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
    }// set Me ... end 
    
    private class onlineIcon implements Icon {

        public onlineIcon(boolean online) {
            
            on=online;
            
        }// end generator for inner class on icon
        
        @Override
        public void paintIcon
                (Component c, Graphics g, int x, int y) {
            
            if (on) {
            g.setColor(Color.ORANGE);
            g.fillOval(0, 0, 15, 15);
            g.setColor(Color.BLACK);
            g.fillOval(4, 2, 3, 4);
            g.fillOval(9, 2, 3, 4);
            g.drawArc(3, 7, 10, 3, 0,-180);
            }else{
            g.setColor(Color.GRAY);
            g.fillOval(0, 0, 15, 15);
            g.setColor(Color.WHITE);
            g.drawLine(5, 5,6, 5);
            g.drawLine(9, 5, 10, 5);
            g.drawLine(4, 10,11, 10);
            }
   
        }

        @Override
        public int getIconWidth() {
            return 15;
        }

        @Override
        public int getIconHeight() {
            return 15;
        }
        
    }// end inner class onlineICon
    
    private class fList extends DefaultListCellRenderer {

        private HashSet<String> onlineList;
        
        public fList(HashSet<String> onList) {
            onlineList=onList;
        }// end generator inner class
        @Override
        public Component getListCellRendererComponent
                (JList<?> list, Object value, int index, 
                boolean isSelected, boolean cellHasFocus) {
           JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
           
           if (value.equals(""))
               return label;
           
           label.setIcon(new onlineIcon
                   (onlineList.contains((String)value)?true:false));
           return label;
        }

    }// end private inner class fLIst

    
}
