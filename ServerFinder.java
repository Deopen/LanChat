/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;


import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
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


public class ServerFinder extends JFrame{

    private HashSet<String> checkIpThreads;
    
    
    private HashMap<String,Integer> clientsThreadCounter;
    private JList serverList;
    private DefaultListModel listElements;
    private JProgressBar prog;
    private JButton scanBtn;
    private boolean scanBtnFlag=true;
    private JTextField statArea;
    private boolean stopScanFlag=false;
    
    public ServerFinder() throws HeadlessException {
        
        

        checkIpThreads=new HashSet<>();
        setTitle("Lan Chat Server Finder");
        statArea=new JTextField();
        statArea.setEditable(false);
        statArea.setBackground(Color.DARK_GRAY);
        statArea.setForeground(Color.WHITE);
        statArea.setSelectionColor(Color.DARK_GRAY);
        statArea.setHorizontalAlignment(JTextField.CENTER);        
        statArea.setFont(new Font("Arial", 0, 15));
        clientsThreadCounter=new HashMap<>();

        getRootPane().
                    getInputMap().put
                    (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
                    "stop");
         getRootPane().
                    getActionMap().put("stop", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopScanFlag=true;
            }
        });
        
         getRootPane().
                 getInputMap().put
                 (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),
                 "scan");
         getRootPane().
                 getActionMap().
                 put("scan",new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            if (scanBtnFlag)
                                scan();
                        }
                    }).start();
                
             
            }
        });
        scanBtn=new JButton("Scan !");
        
        listElements= new DefaultListModel<>();
        serverList=new JList(listElements);
        serverList.setSelectionMode
                (ListSelectionModel.SINGLE_SELECTION);
        prog=new JProgressBar();
        prog.setStringPainted(true);
        
        add(new JScrollPane(serverList)
                ,BorderLayout.CENTER);
        add(scanBtn,BorderLayout.SOUTH);
        add(statArea,BorderLayout.NORTH);
        scanBtn.addActionListener(new ActionListener() {

            
            
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        scan();
                    }
                }).start();
            }
        });
        
        serverList.addMouseListener(new MouseAdapter() 
        {

            @Override
            public void mouseClicked(MouseEvent e) {
                
                JList l=(JList)e.getSource();
                
                if (e.getClickCount()>=2 && scanBtnFlag) {
                    
                    int i=l.locationToIndex(e.getPoint());
                    String serverAddr=(String) listElements.get(i);
                    Client c=new Client(serverAddr);
                    c.runClinet();
                    Login_Window loginWin=new Login_Window(c);              
                    dispose();
                }
                
                
            }//end mouse clicked
        
            
        });        
        
        setMeVisiblePlz();
        
    }//end generator
    
    private void scan() {        
        swapScanWithProgressBar();
        checkIpThreads=new HashSet<>();
        
        setProgVal(0);
        resetList();
        
        setIpRange s=new setIpRange();
        String[][] getRagneRes=s.getRange();
        s.dispose();
         
        if (scanBtnFlag)
            return;
        
        String prifix="192.168.";
        int start_i;
        int end_i;
        int start_j;
        int end_j;
        
        
        start_i=
                Math.abs(Integer.valueOf(getRagneRes[0][0]));
        end_i=
                Math.abs(Integer.valueOf(getRagneRes[0][1]));
        
        start_j=
                Math.abs(Integer.valueOf(getRagneRes[1][0]));
        end_j=
                Math.abs(Integer.valueOf(getRagneRes[1][1]));
        
        if (end_i>254)
            end_i=254;
        
        if (end_j>254)
            end_j=254;
        final int size=(((end_i+1)-start_i)*( (end_j+1)-start_j));
        setProgMax(size);
        final int maximum_thread_size=20;
        
        setStat("Press Esc to stop scanning");
        ArrayList<Thread> myThreads=new ArrayList<>();
        int icmpCount=1;
        int icmpTimeOut=2000;
        if (size<510) {
            icmpCount=2;
            icmpTimeOut=3000; 
        }
        
        
        for (int i=start_i;i<=end_i;i++) {

            String IP=prifix+i+".";
            
            if (stopScanFlag)
                break;
            
            for (int j=start_j;j<=end_j;j++) {

                    final String main_IP=IP+j;
                    
                    if (main_IP.equals("192.168.0.0"))//reserver IP
                        continue;
                    
                    final int c=icmpCount;
                    final int to=icmpTimeOut;
                    Runnable r=new Runnable() {
                        

                        @Override
                        public void run() {
                                if(ping(main_IP,c,to)){
                                    checkThisIP(main_IP);
                                }
                                else{
                                    plusProgVal();
                                }
                                setProgText(main_IP);
                    
                        }//end Run
                        };
                    
                    
                    if (myThreads.size()>maximum_thread_size) {
                        for (Thread t:myThreads) {
                            if (t.isAlive())
                                try {
                                t.join();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
                            }
        
                            
                        }//end for
                        myThreads.removeAll(myThreads);
                    }//end if
                    
                    myThreads.add(new Thread(r));
                    myThreads.get(myThreads.size()-1).start();

                    if (stopScanFlag)
                        break;
                
            }//end for j
            
        }// end for i
        

        for (Thread t:myThreads) {
            if (t.isAlive())
                try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
            }


        }//end for
        myThreads.removeAll(myThreads);
        int checkIPSize=checkIpThreads.size();
        while(!checkIpThreads.isEmpty()){            
            setProgVal
                    (checkIPSize-checkIpThreads.size());
            synchronized(this){
                try {
                    this.wait(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
                }//end catch
            }//end synch
        }//end while
        
        if (!listElements.isEmpty())
            setStat("Double click to connect");
        else
            setStat("");
        
        stopScanFlag=false;
        swapScanWithProgressBar();
        
        
    }// end scanning
    
    public void setProgText(final String txt) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                prog.setString(txt);
            }
        });
        
    }//end changing progText
    public void checkThisIP(final String IP) {
        checkThisIP(IP,2,3000);
    }//end 
    
    public void checkThisIP(final String IP,int icmpCount,int icmpTimeOut) {

        final Client c=new Client(IP);
        if (!(System.getProperty("os.name").startsWith("Windows"))){
            if (!icmpPing(IP,icmpCount,icmpTimeOut)){
                plusProgVal();
                return;
            }
        }
        
        
        
        Thread t
                =new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkIpThreads.add(IP);
                        
                        c.runClinet();
                        if (c.isConnected()) {
                            
                            
                            try {
                                Random rnd=new Random();
                                long rndNum=rnd.nextLong();
                                c.lockGetMsg("ServerFinder"+rndNum);
                                c.sendMsg("areYouLanchat:from:to:msg");
                                if (c.getMsg("ServerFinder"+rndNum)
                                        .equals("Yes-My-Son !"))
                                    addToList(IP);
                                c.unlockGetMsg("ServerFinder"+rndNum);
                                c.closeConnection();
                            } //end if is connected
                            catch (IOException ex) {
                                System.err.println("IO exeption");
                                Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
                            }//end catch

                        }//end if is connected
                        
                        plusProgVal();
                        checkIpThreads.remove(IP);
                    }//end run
    
                });
                    
        t.start();
        
        synchronized (t) {
            try {
                t.join(6000);                
                
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
        }//end synch
        
        
    }//end check This IP
    
    
    private void setStat(final String stat) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                statArea.setText(" "+stat);
            }
        });
        
    }//end sett status
    

    
    private void setProgVal(final int val) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                prog.setValue(val);
            }
        });
        
    }//end set progress value
    
    private void setProgMax(final int val) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                prog.setMaximum(val);
            }
        });
        
    }//end set progress value
    
    private void plusProgVal() {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                
                prog.setValue(prog.getValue()+1);
            }
        });
        
    }// end set prog max
    
    private void addToList(final String item) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listElements.addElement((String)item);
            }
        });
        
    }//end adding to server list
    
    private void resetList() {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                listElements.removeAllElements();
            }
        });
        
    }//end reset list
    
    
    
    private void swapScanWithProgressBar() {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (scanBtnFlag){
                    remove(scanBtn);
                    add(prog,BorderLayout.SOUTH);
                    scanBtnFlag=!scanBtnFlag;
                    repaint();                    
                    setVisible(true);                    
                }else{
                    remove(prog);
                    add(scanBtn,BorderLayout.SOUTH);
                    scanBtnFlag=!scanBtnFlag;                    
                    repaint();                    
                    setVisible(true);
                }//end else
            }//end run
        });//end invoke later
        
    }//end swapping scanBtn with progress bar 
    
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(250,290);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
    }// set Me ... end 

    private class setIpRange extends JFrame {

        
        private JFormattedTextField[] from;
        private JFormattedTextField[] to;
        private JButton setBtn;
        private JPanel centerPanel;
        private HashMap<String,Integer> clientsThreadCounter;
        
        private boolean rangeSetFlag=false;
        
        public setIpRange()  {
            
            
            
            setTitle("IP Range");
            
            from=new JFormattedTextField[4];
            to=new JFormattedTextField[4];
            setBtn=new JButton("Set");
            add(setBtn,BorderLayout.SOUTH);
            
            
            //=======center pannel=========
            centerPanel=new JPanel(new GridLayout(2, 1));
            
            JPanel innerPanel1=new JPanel(new FlowLayout());
            innerPanel1.add(new JLabel("From :"));
            for (int i=0;i<4;i++){
                from[i]=
                        new JFormattedTextField
                                (NumberFormat.getInstance());
                
                from[i].setColumns(3);
                innerPanel1.add(from[i]);
                if (i!=3)
                    innerPanel1.add(new JLabel(".",SwingConstants.CENTER));
            }
            
            centerPanel.add(innerPanel1);
            JPanel innerPanel2=new JPanel(new FlowLayout());
            innerPanel2.add(new JLabel("To     :"));
            for (int i=0;i<4;i++){
                to[i]=new JFormattedTextField(NumberFormat.getInstance());
                to[i].setColumns(3);
                innerPanel2.add(to[i]);
                if (i!=3)
                    innerPanel2.add(new JLabel(".", SwingConstants.CENTER));
            }
            
            
            final Thread t=new Thread(new Runnable() {

                            int start=-1;
                            int end=-1;
                            String prifix="192.168.";
                        @Override
                        public void run() {
                            ArrayList<Thread> myMiniPool=new ArrayList<>();//po0o0ooollll party :)))
                            final int maximumSizeOfPool=7;//lucky number :)
                            for (int i=1;i<244;i++) {
                                final int fi=i;//:)))))
                                Thread st=new Thread (new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                        
                                            if (icmpPing(prifix+fi+".1",1,3000)){
                                                if (start>=fi || start==-1){
                                                    start=fi;
                                                    from[2].setText(String.valueOf(start) );
                                                }
                                                end=fi;
                                                if (to[2].getText().isEmpty())
                                                    to[2].setText(String.valueOf(end));
                                                else if (Integer.valueOf(to[2].getText())<end)
                                                    to[2].setText(String.valueOf(end));
                                            }//end if
                                        
                                        }catch(Exception ex) {

                                        }//end catch
                                    }
                                });
                                
                                st.start();
                                myMiniPool.add(st);
                                
                                if (myMiniPool.size()>maximumSizeOfPool ) {
                                    for (Thread t:myMiniPool){
                                        try {
                                            t.join();
                                        } catch (InterruptedException ex) {
//                                            Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }//end for join
                                    myMiniPool.removeAll(myMiniPool);
                                }//end if my mini blah blah .,..
                            }//end for  i
                        
                        }//end run t
            });
            t.start();
            centerPanel.add(innerPanel2);
            //=======end center panel=======
            add(centerPanel,BorderLayout.CENTER);
            
            for (int i=0;i<2;i++) {
                from[i].setEditable(false);
                to[i].setEditable(false);
                
            }
            from[0].setText("192");
            to[0].setText("192");
            from[1].setText("168");
            to[1].setText("168");
            from[3].setText("1");
            to[3].setText("254");
            

            
            AbstractAction set=new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean noNum=true;
                    for (JTextField txt:from){
                        if (!isItNumber(txt.getText())){
                            noNum=false;
                            break;
                        }//end if
                    }//end for
                    
                    for (JTextField txt:to){
                        if (!isItNumber(txt.getText())){
                            noNum=false;
                            break;
                        }//end if
                    }//end for
                    
                    if (noNum){
                        t.interrupt();
                        rangeSetFlag=true;
                    }
                }
            };
            
            setBtn.addActionListener(set);//end addActionListener                    
            getRootPane().getInputMap().put
                    (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Set");
            getRootPane().getActionMap().put("Set", set);
            
            
            
            setVisible();
            
        }//end generator
        
        private boolean isItNumber(String e) {
            
            try{
                int i=Integer.valueOf(e);
            }catch(NumberFormatException ex) {
                return false;
            }
            
            return true;
        }//end isItNumber
        
        private void setVisible() {
            
            pack();
            setVisible(true);
            setDefaultCloseOperation
                    (JFrame.DO_NOTHING_ON_CLOSE);
            
            addWindowListener(new WindowAdapter() 
            {

                @Override
                public void windowClosing(WindowEvent e) {
                    swapScanWithProgressBar();
                    dispose();
                }
                
                
                
            });
            
            
            setLocationRelativeTo(null);
            
        }//end set visible
        
        public String[][] getRange(){
            
            String[][] res=new String[2][2];
            
            while(!rangeSetFlag && isVisible()){
                synchronized(setIpRange.this){
                    try {
                        setIpRange.this.wait(50);
                    } catch (InterruptedException ex) {
                        Logger.getLogger
                                (ServerFinder.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }//end catch
                }//end synch
           }//end while

            
            res[0][0]=from[2].getText();
            res[0][1]=to[2].getText();
            
            res[1][0]=from[3].getText();
            res[1][1]=to[3].getText();
            
            return res;
            
        }   //end getRange     
        
    }//end inner class Set ip range
    
    
    public boolean ping(String addr) {
        return ping(addr,2,2000);
    }
    public boolean ping(String addr,int icmpCount,int icmpTimeOut) {
        
        ///==========end if itsn't win================
        if (!(System.getProperty("os.name").startsWith("Windows"))){
            try {
                    return (InetAddress.getByName(addr).isReachable(300));
                } catch (UnknownHostException ex) {
                    return false;
                }catch(IOException ex) {
                    ex.printStackTrace();
                }
        
        }
        ///==========end if it isn't win===============
        
        
        return icmpPing(addr,icmpCount,icmpTimeOut);
    }//end ping
    
    private boolean icmpPing(final String addr) {
        return icmpPing(addr,2,2000);
    }
    private boolean icmpPing(final String addr,int count,final int timeOut) {
        
        final String arg=System.getProperty("os.name").startsWith("Windows")?"n":"c";
        
        
        try {
            final Process p1;
            p1= 
            java.lang.Runtime.getRuntime().
            exec("ping -"+arg+" "+count+" "+addr);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized(ServerFinder.this){
                        try {
                            ServerFinder.this.wait(timeOut);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
                        }//end catch
                
                        p1.destroy();
                        
                    }
                }//end run
            }).start();
            try {
                p1.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader in=new BufferedReader
                    (new InputStreamReader(p1.getInputStream()));
            String line=in.readLine();
            boolean ttl=false;
            while(line!=null) {
                
                if (line.length()>0) {
                    
                    
                    line=line.toUpperCase();
                    
                    if (
                            line.contains("TTL=") &&
                            !line.contains("TTL=251") && 
                            !line.contains("TTL=252") &&
                            !line.contains("TTL=250") &&
                            !line.contains("TTL=248") &&
                            !line.contains("TTL=249") )
                            ttl=true;
                        
                
                    if (line.contains("TIME") && ttl)
                        return true;
                    else if (line.contains("TIME") && !ttl)
                        return false;
                }//end if line is not empty

                line=in.readLine();
                
            }//end while line != null
            
            
        } //end ping
        catch (IOException ex) {
            return false;
        }
        
        return false;
    }//end winPing
    

    
    
}
