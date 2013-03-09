/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi 
 * @author DeOpenMail@Gmail.com
 */

public class Find_Friend extends JFrame {

    JTextField searchField;
    JList           resultList;
    DefaultListModel resultItems;
    Client client;
    JLabel lblInfo;
    Thread autoChecker;
    String lastTxt;
    String lastSearchElement;
    
    public Find_Friend(Client c) throws HeadlessException {
        this.client=c;
        
        setTitle("[: LanChat Friend Finder :]");
        JPanel centerPanel=new JPanel(new BorderLayout());        
        lblInfo=new JLabel(" Double click to add",
                SwingConstants.CENTER);        
        lblInfo.setFont(new Font("imapct",1,14));
        
        resultItems=new DefaultListModel();
        resultList=new JList(resultItems);
        
        searchField=new JTextField(14);
        
        resultList.setFont(new Font("times roman",1,12));
        
        centerPanel.add(lblInfo,BorderLayout.NORTH);
        centerPanel.add( new JScrollPane(resultList) 
                ,BorderLayout.CENTER);

        add(searchField,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        
        autoChecker=new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    
                synchronized(Find_Friend.this) {
                    try {
                        Find_Friend.this.wait(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Find_Friend.class.getName()).log(Level.SEVERE, null, ex);
                    }//end catch
                }//end synch
                
                if (searchField.getText().length()==0)
                    continue;
                
                if (searchField.getText().contains("\\") ||
                        searchField.getText().contains("*") ||
                                searchField.getText().contains(".") ||
                        searchField.getText().contains("[") ||
                        searchField.getText().contains("]")){
                    searchField.setText("");
                    continue;
                }
                
                if (!searchField.getText().equals(lastTxt)){
                    setLastTxt(searchField.getText());
                    continue;
                }
                
                if (searchField.getText()
                        .equals(lastSearchElement))
                    continue;
                
                
                setLastSE(searchField.getText());              
                setLastTxt(searchField.getText());
                
                String server_respond=null;
                
                client.sendMsg("find:"+
                        client.getProfile().
                        getUserName()+":to:"
                        +searchField.getText());
                
                
                while (
                        server_respond==null && 
                        searchField.getText().equals(lastTxt)){
                    
                    server_respond=client.otherMsgTypes.get("find");
                    
                }//end while
                
                try{
                    
                    client.otherMsgTypes.remove("find");
                    
                    resultItems.removeAllElements();
                    
//                    System.out.println("server res : "+server_respond);
                
                if (server_respond!=null 
                        && server_respond.length()!=0){                    
                    
                for (String e:server_respond.split(",")){
                    
                    if (e!=null && e.length()!=0)
                        resultItems.addElement(e);
                }//end for
                    
                }//end if
                }catch(Exception ex) {
                    System.out.println("exeption in find_friend skipped .");
                    continue;
                }//end catch
                }//end while
            }//end run
        });//end thread
        
        resultList.addMouseListener(new MouseAdapter() 
        {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList l=(JList)e.getSource();
                if (e.getClickCount()>=2) {
                    int i=l.locationToIndex(e.getPoint());
                    String user=(String) resultItems.get(i);
                    int bracetOpen=user.indexOf("(")+1;                    
                    int bracetClosed=user.indexOf(")");
                    client.sendMsg
                            ("add:"+client.getProfile().
                            getUserName()+":to:"+
                            user.substring(bracetOpen,bracetClosed)
                            );
                    client.getProfile().
                            addToFriendList(user.substring(bracetOpen,
                            bracetClosed));
                    dispose();
                }//end if click count >=2
            }//end mouse clicked
            
        });//end add mouse listener for result list
        
        setMeVisiblePlz();
        

    }//end generator
    
    private void setLastTxt(String e) {
        
        lastTxt=e;
        
    }//end 
    
    private void setLastSE(String e) {
        
        lastSearchElement=e;
        
    }//end 
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(250,300);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                autoChecker=null;
                dispose();
            }//end win closing
            
        });//end add win listener
        
        
        setVisible(true);
        autoChecker.start();
    }// set Me ... end 
    
}
