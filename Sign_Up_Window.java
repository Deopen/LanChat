/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class Sign_Up_Window extends JFrame{

    private GridBagConstraints gbc;
    private GridBagLayout gbl;
    private JTextField txt_usr;
    private JTextField txt_pass;
    private JTextField txt_repeat_pass;
    private JComboBox yCombo;
    private JComboBox mCombo;
    private JComboBox dCombo;
    private JTextField txt_nme;
    private String registerOrEdit;
    private Client client;
    
    //http://www.mysql.com/downloads/connector/j/
    //for insert into
    //http://www.vogella.com/articles/MySQLJava/article.html
    //about data type :
    //http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
    //ascii table :
    //http://www.asciitable.com/
    //algorithms :
    //http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html
    //maroofaash :
    /*
     * SHA-1
     * SHA-384
     * SHA-512
     * SHA-256
     * MD5
     * MD2
     */
    
    
    public Sign_Up_Window
            (final Client client,String rOrE) 
            throws HeadlessException {//constructor begin
    
        
        registerOrEdit=rOrE.toLowerCase();
        
       
        
        this.client=client;
        
        gbc = new GridBagConstraints();
        gbl=new GridBagLayout();
        setLayout(gbl);
        gbc.fill=GridBagConstraints.BOTH;
        
        gbc.insets=new Insets(3, 3, 3, 3);
        
        
        ImageIcon buff_ico=new ImageIcon(Login_Window.icon_resource_path+"sign_up.png");
        
        gbc.anchor=GridBagConstraints.CENTER;
        setTitle("(: Lan Chat - "+registerOrEdit+" :)");
        
        JLabel sign_up_logo=new JLabel("Create  Lan Chat  Account",buff_ico,JLabel.CENTER);
        sign_up_logo.setFont(new Font("tahoma", Font.BOLD, 20));

        plusCmp(sign_up_logo, 0, 0,4);

        JLabel lbl_gap0=new JLabel();
        lbl_gap0.setFont(new Font("tahoma", Font.BOLD, 16));
        plusCmp(lbl_gap0, 0, 1);
        
        JLabel lbl_choose_usr=new JLabel("Choose Username :");
        txt_usr=new JTextField(13);
         if (!registerOrEdit.equals("register")){
             setTitle("(: Lan Chat - "+registerOrEdit.split(":")[0] +" :)");
             txt_usr.setEditable(false);
             txt_usr.setText(registerOrEdit.split(":")[1]);
             sign_up_logo.setText("Edit  Lan Chat  Account");
         }//end if edit
        plusCmp(lbl_choose_usr, 0, 2,1);
        plusCmp(txt_usr, 1, 2);
            
        JLabel lbl_choose_pass=new JLabel("Choose Password  :");
        txt_pass=new JPasswordField(13);
        plusCmp(lbl_choose_pass, 0, 3);
        plusCmp(txt_pass, 1, 3);

        JLabel lbl_repeat_pass=new JLabel("Repeat  Password  :");
        txt_repeat_pass=new JPasswordField(13);
        plusCmp(lbl_repeat_pass, 0, 4);
        plusCmp(txt_repeat_pass, 1, 4);
        
       String[] strMonth= { " -month- ",
                                     "JANUARY", "FEBRUARY", "MARCH",
                                        "APRIL",   "MAY",      "JUNE",
                                        "JULY",    "AUGUST",   "SEPTEMBER",
                                         "OCTOBER","NOVEMBER","DECEMBER" };
       ArrayList<String> strDays=new ArrayList<>();
       strDays.add(" -day- ");
       
       ArrayList<String> strYear=new ArrayList<>();
       strYear.add(" -year- ");
       int currentYear=Calendar.getInstance().get(Calendar.YEAR);
       
       for (int i=1900;i<=currentYear;i++)
           strYear.add(String.valueOf(i));
       
        yCombo=new JComboBox(strYear.toArray());
        dCombo=new JComboBox(strDays.toArray());
        mCombo=new JComboBox(strMonth);
       // Number of Days   :
        //{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};      
       // Mounth :
       // 0   1    2    3    4    5   6    7    8    9  10  11   12  

        plusCmp(new JLabel ("Date of birth :"),0,5);
        mCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                int count=((JComboBox)e.getSource()).getSelectedIndex();
                HashSet th1=new HashSet();
                th1.add(1);th1.add(3);th1.add(5);
                th1.add(7);th1.add(8);th1.add(10);
                th1.add(12);
                
                int lastDay=0;
                if (count==2) 
                    lastDay=28;
                else if (th1.contains(count))
                    lastDay=31;
                else if (count!=0)
                    lastDay=30;
                
                
                int lastIndexInDayCombo=dCombo.getSelectedIndex();
                dCombo.removeAllItems();
                dCombo.addItem(" -day-");
                for (int i=1;i<=lastDay;i++)
                    dCombo.addItem(i);
                
                if (lastIndexInDayCombo>lastDay)
                    dCombo.setSelectedIndex(lastDay);
                else
                    dCombo.setSelectedIndex
                            (lastIndexInDayCombo);
            }
        });
        
        
        plusCmp(mCombo,1,5,1);
        plusCmp(dCombo,2,5);
        plusCmp(yCombo,3,5);
       
        JButton btnSignUp=new JButton("Sign Up !");
        JButton btnEdit=new JButton("Update");
        
        btnSignUp.setFont(new Font("Arial",Font.BOLD, 16));
        btnEdit.setFont(new Font("Arial",Font.BOLD, 16));
        JLabel lb_name=new JLabel("Full Name :");
        plusCmp(lb_name, 0,gbc.gridy+1);
        
        txt_nme=new JTextField(13);
        plusCmp(txt_nme,1, gbc.gridy);
        
        
        JLabel lbl_gap2=new JLabel();
        plusCmp(lbl_gap2, 0,gbc.gridy+1);
        
        gbc.weighty=2;
        if (registerOrEdit.equals("register"))
            plusCmp(btnSignUp,0, gbc.gridy+1,4);
        else
            plusCmp(btnEdit,0, gbc.gridy+1,4);
        
        gbc.weighty=0;
        JLabel lbLastGap=new JLabel();
        plusCmp(lbLastGap,0, gbc.gridy+1);

        ActionListener registerAndAdd= 
                new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String btnTitle=((JButton)e.getSource()).getText();
                

                            Random rndGenerator=new Random();
                            final String lockValue=rndGenerator.nextLong()+"";
                            

                            try{
                            client.lockGetMsg(lockValue);
                            boolean err_flg;
                            String name = null;
                            String family = null;
                            String err_log="";
                            int err_cnt=0;


                            if (    !txt_nme.getText().contains(" ") ||
                                    txt_nme.getText().split(" ").length!=2 || 
                                    txt_nme.getText().split(" ")[0].isEmpty() ||
                                    txt_nme.getText().split(" ")[1].isEmpty()    ) {
                                err_log+=
                                        ++err_cnt+"- Please enter your full name . \n";       
                            }
                            else {
                            name = txt_nme.getText().split(" ")[0];
                            family = txt_nme.getText().split(" ")[1];
                            }



                            if (
                                    yCombo.getSelectedIndex()==0 ||
                                    mCombo.getSelectedIndex()==0 ||
                                    dCombo.getSelectedIndex()==0
                                    ) {
                                err_log+=
                                        ++err_cnt+
                                        "- Please select the date that you were born. \n";

                            }   
                            
                            
                            String date=
                                    yCombo. getSelectedItem()  +"-"+
                                    mCombo.getSelectedIndex()+"-"+
                                    dCombo. getSelectedItem();



                            if (
                                    txt_pass.getText().length()==0 || 
                                    txt_usr.getText().length()==0 || 
                                    txt_nme.getText().length()==0) {
                                err_log+=
                                        ++err_cnt+
                                        "- Please fill all the blank spaces. \n";
                            }

                            String pass=txt_pass.getText();
                            String user=txt_usr.getText();

                            if (!pass.equals(txt_repeat_pass.getText())) {
                                err_log+=
                                        ++err_cnt+
                                        "- The passwords do not match . \n";
                            }

                                
                                if (btnTitle.equals("Sign Up !")) {
                                    if (
                                    isUserValid(user,lockValue,client) )

                                    {
                                        err_log+=
                                            ++err_cnt+
                                            "- Please choose another user name. \n";

                                    }// end if user was valid
                                }//end if equals sign up
                            

                                err_flg=err_cnt==0?false:true;

                                if (err_flg) {
                                    JOptionPane.showMessageDialog(null,
                                            err_log,"You have "+ err_cnt+" error in inputs",
                                            JOptionPane.ERROR_MESSAGE);
                                    client.unlockGetMsg(lockValue);
                                    return;
                                }// end if ( err )


                                String passHashed = null;

                                try {

                                    String salt=user+"passg0rd";
                                    passHashed=hash_this(pass, salt);
                                    
                                    salt=user.hashCode()+
                                            name+family+
                                            family.hashCode()+
                                            date+passHashed.hashCode();
                                    
                                    passHashed=hash_this(passHashed, salt);
                                    
                                    } catch (NoSuchAlgorithmException ex) {
                                        Logger.getLogger
                                                (Sign_Up_Window.class.getName()).
                                                log(Level.SEVERE, null, ex);
                                        err_log+=
                                            ++err_cnt+
                                            "- an error occures in hashing allgorithm, "
                                                + "please update your java runtime . \n";
                                    }//end catch ( hashing password )

                                err_flg=err_cnt==0?false:true;

                                if (err_flg) {
                                    JOptionPane.showMessageDialog(null,
                                            err_log,"You have "+ err_cnt+" error in inputs",
                                            JOptionPane.ERROR_MESSAGE);
                                    client.unlockGetMsg(lockValue);
                                    return;
                                }// end if ( err )


                                if (!err_flg) {
                                    final String n=name,f=family, u=user
                                    , p=passHashed , d=date;
                  
                                            if (btnTitle.equals("Sign Up !")) {
                                                add_row(n, f, u, p, d);
                                                int timeOut=0;
                                                while (!isUserValid(u,lockValue,client)){
                                                    synchronized (lanchat.Sign_Up_Window.this) {
                                                        try {
                                                            lanchat.Sign_Up_Window.this.wait(200);
                                                        } catch (InterruptedException ex) {
                                                            Logger.getLogger(Sign_Up_Window.class.getName()).log(Level.SEVERE, null, ex);
                                                        }//end catch
                                                    }//end sunch
                                                    timeOut++;
                                                    if (timeOut>10)
                                                        break;
                                                }//end while
                                                
                                                if (isUserValid(u,lockValue,client)) {
                                                    
                                                    JOptionPane
                                                            .showMessageDialog(null, 
                                                        "Your account has been "
                                                            + "successfully created !");                                            
                                                    LanChat.showLoginWin(client);
                                                    dispose();

                                                
                                                }// end if 
                                                else{
                                                    JOptionPane
                                                            .showMessageDialog(null, 
                                                        "Sorry, "
                                                            + "Something is wrong !");                                            
                                                    
                                                }//end else
                                            }//end if sign up
                                            else {
                                                    client.getProfile().
                                                            setFullName(n+" "+f);
                                                    edit_row(n, f, u, p, d);
                                                    dispose();
                                            }//end else
                                 
                                }//end !err_flg
                                
                        client.unlockGetMsg(lockValue);
                            }//end main try
                            catch(Exception ex) {
                                Logger.getLogger(Sign_Up_Window.class.getName())
                                                .log(Level.SEVERE, null, ex);
                            }

            }// end action performed
        };//end Absteract Action
        
        btnSignUp.addActionListener(registerAndAdd);
        btnEdit.addActionListener(registerAndAdd);
        
        setMeVisiblePlz();
        
    }//constructor end
    
    
    public Sign_Up_Window(final Client client) 
            throws HeadlessException {
        
        this(client,"register");
        
    }//end generator 2
    
    private void plusCmp
            (Component cmp,int gridX,int gridY) 
    {// plusCmp ver. 2 begin
        
        if (cmp instanceof JTextField)
            plusCmp(cmp,gridX,gridY,3);
        else
            plusCmp(cmp,gridX,gridY,gbc.gridwidth);
        
    }// plusCmp ver. 2 end
    
    private void plusCmp
            (Component cmp,int gridX,int gridY,int width) 
    {//plus component begin
        
        gbc.gridx=gridX;
        gbc.gridy=gridY;
        gbc.gridwidth=width;
        gbl.setConstraints(cmp, gbc);
        add(cmp);
        
    }//plus component end
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(480,300);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                
                if (registerOrEdit.equals("register"))
                            new Login_Window(client);
                
                dispose();
            }
        
            
        });
        
        setVisible(true);
        
    }// set Me ... end 
    
    public static 
            boolean isUserValid(String user_name,String lockVal,Client C) 
             {
        
        if (user_name.equals("Unknown") ||
                user_name.indexOf(",")!=-1 || 
                user_name.indexOf(" ")!=-1||
                user_name.indexOf(":")!=-1 ||
                user_name.indexOf(".")!=-1 ||
                user_name.toLowerCase().equals("server") || 
                user_name.indexOf("confrance")!=-1)
            return false;
        
        try {
            
            C.sendMsg("isUserValid:from:to:"+user_name);
            String serverAnswer=C.getMsg(lockVal);
            System.out.println("server answer :"+serverAnswer);
            
            if (serverAnswer==null)
                return false;
            
            if (serverAnswer.equals("0"))
                return false;
            else 
                return true;
        } // end
        catch (IOException ex) {
            Logger.getLogger(Sign_Up_Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.err.println("Error in is User valid sign up win");
        System.exit(1);
        return false;
        
    }// end 
    
    private void add_row
            (String name , String family ,
            String user,String pass,String birth)  {
        
        
        
        String msgForAddRow=
                name+","+family+","+
                user+","+birth+","+pass;
        
        client.sendMsg("register:from:to:"+msgForAddRow);
        
    }// end adding row to database
    
    private void edit_row
            (String name , String family ,
            String user,String pass,String birth)  {
        
        
        
        String msgForAddRow=
                name+","+family+","+
                user+","+birth+","+pass;
        
        client.sendMsg("update:from:to:"+msgForAddRow);
        
    }// end adding row to database
    
    private static String 
            makeMyMsgSalty(String msg,String salt) {
        
        String local_msg=msg, local_salt=salt;
        int i=1;
        while (local_msg.length()<local_salt.length()) {
            local_msg+=msg.charAt
                    ((msg.length()-(i%msg.length()))
                    %msg.length());
            i++;
        }//end while 
        i=1;
        while (local_salt.length()<local_msg.length()) {
            local_salt+=salt.charAt
                    ((salt.length()-(i%salt.length()))
                    %salt.length());
            i++;
        }//end while 
        if (local_msg.length()!=local_msg.length()) {
            System.err.println(" Different length in "
                    + "makeSaltyMsg calss sign_up .");
            System.exit(1);
        }// end if
        String salty_msg="";
        for (int j=0;j<local_msg.length();j++) {
            salty_msg+=
                    (char)local_msg.charAt(j);
            salty_msg+=
                    (char)local_salt .charAt(j) ;
        }//end for j
   
        return salty_msg;
    }// end making msg salty
    
    public static String hash_this(String msg,String salt) 
            throws NoSuchAlgorithmException {
        
        MessageDigest md=MessageDigest.getInstance("MD5");
        byte[] passHashedByte ;
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer pass_bytes=charset.encode(
                CharBuffer.wrap(makeMyMsgSalty(msg, salt)));
        md.update(pass_bytes);
        passHashedByte=md.digest();
        
        String pass_hashed_str="";
        
        
        for (byte b:passHashedByte)  {
            int val=(char) (b+128);
            String str_val=String.valueOf(val);
            
            if  ( (val<127 && val>58 && val!=92) || 
                   (val<34 && val>32) ||
                    (val<47 && val>34 && val!=39)
                    )
                str_val=String.valueOf((char)val);
            else if (val<10)
                str_val="00"+str_val;
            else if (val<100)
                str_val="0"+str_val;
//            System.out.println(val+" : "+str_val);
            
            if (str_val.equals(","))//seperator for server should be repalace
                str_val="+";
            
            pass_hashed_str+=str_val;    
        }
        
        return pass_hashed_str;
    }// end hash this
    
}
