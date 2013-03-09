/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class Login_Window extends JFrame{

    private GridBagConstraints gbc;
    private GridBagLayout gbl;
    private Client client;
    public static String icon_resource_path=
            System.getProperty("user.dir")
            +System.getProperty("file.separator")+
            "icons"+System.getProperty("file.separator");
    
    private JTextField txt_usr;
    private JPasswordField pass;
    
    public Login_Window(final Client client) throws HeadlessException {//constructor begin
    
        
        this.client=client;
        gbc = new GridBagConstraints();
        gbl =  new GridBagLayout();
        setLayout(gbl);
        gbc.fill=GridBagConstraints.BOTH;
        
        gbc.insets=new Insets(3, 3, 3, 3);
        
        setTitle("(: Lan Chat :)");
        
        ImageIcon buff_ico=new ImageIcon
                           (icon_resource_path+"generic_chat.png");
        JLabel lbl_logo=new JLabel("  ",buff_ico,JLabel.RIGHT);
        plusCmp(lbl_logo, 0, 0);
        
        buff_ico=new ImageIcon(icon_resource_path+"user.png");
        JLabel lbl_usr=new JLabel(" Username : ",buff_ico,JLabel.LEFT);
        plusCmp(lbl_usr, 0, 1);
        
        txt_usr=new JTextField(20);
        plusCmp(txt_usr, 0, 2);
      
        
        JLabel gap_lbl1=new JLabel();
        plusCmp(gap_lbl1, 0, 3);
        
        buff_ico=new ImageIcon(icon_resource_path+"password.png");
        JLabel lbl_pass=new JLabel(" Password : ",buff_ico,JLabel.LEFT);
        
        
        plusCmp(lbl_pass, 0, 4);
        
        pass=new JPasswordField(20);
        plusCmp(pass, 0, 5);
        
        
        JLabel gap_lbl2=new JLabel();
        plusCmp(gap_lbl2, 0, 6);
        JCheckBox chk_remember_me=new JCheckBox("Remember my password");
        plusCmp(chk_remember_me, 0, 7);
        
        
        JLabel gap_lbl3=new JLabel();
        plusCmp(gap_lbl3, 0, 8);
//     
        gbc.weighty=2;
        JButton btn_sign_in=new JButton("Sign in");
        plusCmp(btn_sign_in, 0, 9);
        
        gbc.weighty=0;
        JLabel gap_lbl4=new JLabel();
        plusCmp(gap_lbl4, 0, 10);
//        
        JButton btn_sign_up=new JButton("Create LanChat Account");
        gbc.weighty=2;
        plusCmp(btn_sign_up, 0, 11);
        
        
        btn_sign_up.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                Sign_Up_Window sg=new Sign_Up_Window(client);
                dispose();
                
            }
        });
        
        btn_sign_in.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                signIN();
            }
        });
        
        
        AbstractAction signInAct=new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                signIN();
            }
        };
        
        AbstractAction signUpAct=new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Sign_Up_Window sg=new Sign_Up_Window(client);
                dispose();
            }
        };
        
        getRootPane().getInputMap().put
                (KeyStroke.getKeyStroke
                (KeyEvent.VK_ENTER,0),"s");
        getRootPane().getActionMap().put("s",signInAct);
        
        pass.getInputMap().put
                (KeyStroke.getKeyStroke
                (KeyEvent.VK_ENTER,0),"s");
        pass.getActionMap().put("s", signInAct);
        
        btn_sign_in.getInputMap().put
                (KeyStroke.getKeyStroke
                (KeyEvent.VK_ENTER,0),"s");
        btn_sign_in.getActionMap().put("s", signInAct);

        btn_sign_up.getInputMap().put
                (KeyStroke.getKeyStroke
                (KeyEvent.VK_ENTER,0),"s");
        btn_sign_up.getActionMap().put("s", signUpAct);
        
        
        
        gbc.weighty=0;
        JLabel gap_lbl5=new JLabel();
        plusCmp(gap_lbl5, 0, 12);
//     
        
        setFrmVisible();
        txt_usr.requestFocusInWindow();
    }//constructor end
    
    private void signIN() {
        auth(txt_usr.getText(), pass.getPassword());
    }//end sign in
    private void auth(String uName,char[] chrPass) {
        Profile profile;
        try {
            String p=new String(chrPass);
            String localLock=uName+chrPass[0];
            client.lockGetMsg(localLock);
            client.sendMsg("auth:from:to:"+uName+","+
                    LanChat.encrypt(p, "00Fr0gBugBugBugBugFr0g00"+uName));
            String answ=client.getMsg(localLock);
            
            
            if (answ.equals("come in!")){
                
                String fullName=client.getMsg(localLock);
                String fList=client.getMsg(localLock);
                String banList=client.getMsg(localLock);
                String onList=client.getMsg(localLock);
                client.unlockGetMsg(localLock);
                HashSet<String> bSet=new HashSet<>();
                HashSet<String> fSet=new HashSet<>();
                HashSet<String> onSet=new HashSet<>();
                if (fList!=null && fList.length()!=0)
                    fSet.addAll(Arrays.asList(fList.split(",")));
                if (banList!=null && banList.length()!=0)
                    bSet.addAll(Arrays.asList(banList.split(",")));
                
                
                if (onList!=null && onList.length()!=0)
                    onSet.addAll(Arrays.asList(onList.split(",")));
                
                profile=new Profile
                        (fullName.split(" ")[0],
                        fullName.split(" ")[1],
                        fSet, bSet);
                
                
                profile.setUserName(uName);
                
                setVisible(false);
                profile.setOnList(onSet);
                client.setProfile(profile);
                
                UserMainWin uMainWin=
                        new UserMainWin(client);
                
                dispose();
                
            }else{
                    String errMsg;
                    if (Sign_Up_Window.
                            isUserValid
                            (uName, localLock, client)) 
                        errMsg="Your password is incorrect "
                                + "or your user name doesn't exist. "
                                + " (User name is case sensitive) ";
                    else
                        errMsg="Your user name does not exist !";
                    
                    client.unlockGetMsg(localLock);
                    
                    JOptionPane.showMessageDialog(null, errMsg);
            }
        } //end auth
        catch (IOException ex) {
            Logger.getLogger(Login_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }//end auth
    
    
    
    private void plusCmp
            (Component cmp,int gridX,int gridY) {//plus component begin
        
        gbc.gridx=gridX;
        gbc.gridy=gridY;
        gbl.setConstraints(cmp, gbc);
        add(cmp);
        
    }//plus component end
    
    private void setFrmVisible() {//set visible ... begin

        setSize(200, 450);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                if (Server.isServerRun){
                    client.closeConnection();
                    dispose();
                
                }else
                    System.exit(0);
            }
            
            
            
        });
        
        setVisible(true);
        
    }// set visible ... end 
    
    
    
    
    
}//ok √ (alt + v )
