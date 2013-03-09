
package lanchat;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.HashSet;
import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 *///http://dryicons.com/free-icons/page/3/
////http://docs.oracle.com/javase/1.4.2/docs/api/java/security/MessageDigest.html


public class LanChat {

    public final static String license= "====================================================="
                    + "===============================\n"+
                    " \t\t\t  **** Welcome to the LanChat **** \n\n\n"
                    
                    + "Copyright (c) 2013 Omid Yaghoubi <Deopenmail@gmail.com> All Rights Reserved.LanChat \n"                    
                    + "is Open Source and available on the internet.This application has been developed by\n"                    
                    + "Omid Yaghoubi as the final year project in Shamsipour Technical&Vocational College.\n\n"                     
                    + "Permission to use, copy, modify, and distribute this software and its documentation\n"                    
                    +  "for any purpose and without fee is hereby granted,"                    
                    +  "provided that the above copyright \nnotice appear in all copies.\n\n"
                    +  "Further improvements are possible if the software proves useful. These improvements\n"
                    +  "can include removing the requirement for a database and enabling attachments in the \n"
                    +  "messages.  Please let me know via the Email above if you find this software useful.\n"
                    + "This will be a great motivation for me to develop LanChat further.\n"+                    
                    "\n====================================================="
                    + "===============================\n\n"
                        +"This software contains Base64 Java-class written by Robert Harder <rob@iharder.net>  \n"
                        + "and also Modified Version of  \"TicTacToeClient.java\" Written by Deitel&Deitel.\n\n"+
                        "=========================="
                        + "==========================="
                        + "===============================";
    
    
    
    public static void main(String[] args) 
            throws InterruptedException, ClassNotFoundException, 
            SQLException, InstantiationException, 
            IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        
        UIManager.setLookAndFeel
                   ( UIManager.getSystemLookAndFeelClassName());

        
        final JFrame frm=new JFrame("Lan Chat");
        final JFrame frmAbout=new JFrame("About LanChat");
        
        ImageIcon logo_ico=new ImageIcon
                           (Login_Window.icon_resource_path+"generic_chat.png");
        
        ImageIcon idCard_ico=new ImageIcon
                           (Login_Window.icon_resource_path+"id_card.png");
        ImageIcon users_ico=new ImageIcon
                           (Login_Window.icon_resource_path+"users.png");
        
        ImageIcon monitor_ico=new ImageIcon
                           (Login_Window.icon_resource_path+"monitor.png");
        
        
        JLabel lbl_logo=new JLabel("  ",logo_ico,JLabel.RIGHT);
        frm.add(lbl_logo,BorderLayout.NORTH);
        
        JPanel centerPanel=new JPanel(new GridLayout(3,1));
        JButton serverBtn=new JButton("[: Server :]");
        Font myFavoriteLucidaConsoleColonX=new Font("lucida console",1, 12);        
        serverBtn.setIcon(monitor_ico);
        serverBtn.setFont(myFavoriteLucidaConsoleColonX);
 
        JButton clBtn=new JButton("[: Client :]");
        clBtn.setFont(myFavoriteLucidaConsoleColonX);
        clBtn.setIcon(users_ico);
        
        JButton aboutBtn=new JButton("[: About  :]");
        aboutBtn.setFont(myFavoriteLucidaConsoleColonX);
        aboutBtn.setIcon(idCard_ico);
        
        centerPanel.add(serverBtn);
        centerPanel.add(clBtn);
        centerPanel.add(aboutBtn);
        frm.add(centerPanel,BorderLayout.CENTER);
        
        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frm.setVisible(true);
        
        
        
        serverBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {            
     
                frmAbout.dispose();
                
                Server srv=new Server();
                srv.startServer();
                frm.dispose();

            }
        });
        
        clBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                frmAbout.dispose();
                new ServerFinder();
                frm.dispose();
            }
        });
        
        aboutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                
                frmAbout.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                JTextArea aboutField=new JTextArea();// {     
                aboutField.setEditable(false);
                aboutField.setBackground(Color.BLACK);
                aboutField.setForeground(Color.GREEN);
                aboutField.setSelectionColor(Color.WHITE);
                aboutField.setFont(new Font("lucida console",0,13));                                
                frmAbout.add(new JScrollPane(aboutField));                              
                aboutField.append(license);
                frmAbout.pack();
                frmAbout.setVisible(true);          
                
            }
        });
        
        
//                ==========for debug=======================
//        final PipedOutputStream pOut = new PipedOutputStream();
//            System.setErr(new PrintStream(pOut));
//            try {
//                final PipedInputStream pIn=new PipedInputStream(pOut);
//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {                    
//                        logger l=new logger(new InputStreamReader(pIn));
//                        
//                    }
//                }).start();
//
//
//            } catch (IOException ex) {   }
//        ==========================================
        
    }//end main
    
    
     public static String decrypt(String encryptedMsg,String enKey) {
        String decryptedText=null;
        try {
        
        KeySpec ks;
        SecretKeyFactory skf;
        Cipher cipher;
        SecretKey key;
        
        byte[] enKeyBytes = enKey.getBytes("UTF8");
        ks = new DESedeKeySpec(enKeyBytes);
        skf = SecretKeyFactory.getInstance("DESede");
        cipher = Cipher.getInstance("DESede");
        key = skf.generateSecret(ks);
        
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedMsgBytes = Base64.decode(encryptedMsg);
            byte[] plainMsgBytes = cipher.doFinal(encryptedMsgBytes);
            decryptedText= new String(plainMsgBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | 
                NoSuchPaddingException | InvalidKeySpecException | 
                IOException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog
                    (null, "Error in decrypting msg","Error",JOptionPane.ERROR);
        }
        return decryptedText;
    }
        
    public static String encrypt(String msg,String enKey) {
        String encryptedString = null;
        
        
        KeySpec ks;
        SecretKeyFactory skf;
        Cipher cipher;
        byte[] enKeyBytes;
        SecretKey key;
        
        try {
        
        enKeyBytes = enKey.getBytes("UTF8");
        ks = new DESedeKeySpec(enKeyBytes);
        skf = SecretKeyFactory.getInstance("DESede");
        cipher = Cipher.getInstance("DESede");
        key = skf.generateSecret(ks);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] plainMsgBytes = msg.getBytes("UTF8");
        byte[] encryptedMsgBytes = cipher.doFinal(plainMsgBytes);
        encryptedString = Base64.encodeBytes(encryptedMsgBytes);
        } catch (UnsupportedEncodingException |
                InvalidKeyException | 
                NoSuchAlgorithmException |
                NoSuchPaddingException | 
                InvalidKeySpecException |
                IllegalBlockSizeException | 
                BadPaddingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog
                    (null, "Error in encrypting msg","Error",JOptionPane.ERROR);
        }
        
        return encryptedString;
    }//end encrypt
    
    public static void showLoginWin(Client c) {
        Login_Window loginWin=new Login_Window(c);
    }//end show login win
    
    public static String hashSetToString
            (HashSet<String> hashSet,
            String seperator) {
        
        if(hashSet.isEmpty())
            return "";
        
        String res="";
        
        for (String e:hashSet)   {     
            if (e.length()==0)
                continue;
            res+=e+seperator;
            
        }//end for e
        
        if (res.length()!=0)
            res=res.substring(0,res.length()-1);//skip last seperator 
        
        return res;
    }//end hasSetToStr
    
    
    public static void playSound(String name) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        
        
        String fs=System.getProperty("file.separator");
        AudioInputStream audioIn =
                AudioSystem.getAudioInputStream
                (new URL(
                "file:"+fs+fs+fs+
                System.getProperty("user.dir")
                   +fs+"snd"+fs+name+".wav"));
        
        Clip clip = (Clip) AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
        
    }//end play sound
    
    
        
}
