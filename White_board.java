/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class White_board extends JFrame {
    
    public ArrayList<Point> points;
    private ArrayList<Point> erasePoints;
    
    private final Client client;
    private myPanel boardPanel;
    public HashMap<Integer,Integer> pointSizes;
    private int lastSize;
    private JFormattedTextField sizeField;
    private JComboBox brushModel;
    private final String from;
    private Thread t_for_send_point;
    private ArrayList<Thread> myThreads;
    private final static int myPoolSize=20;
    private int addLine=0;
    private int rmLine=0;
    private String addMsg="";
    private String rmMsg="";
    private boolean closing=false;
    private Thread refreshThread;
    public White_board(Client c,final String from) throws HeadlessException {
        
        client=c;
        this.from=from;
        
        if (c.getProfile().getUserName().equals(from)){
            JOptionPane.showMessageDialog(null,"You Can't Play With Yourself HERE !!!!!!!!! ");
            c.boardsMap.remove(from);
            dispose();
        }else{
        
        setTitle(from);
        myThreads=new ArrayList<>();
        pointSizes=new HashMap<>();
        points=new ArrayList<>();
        lastSize=12;
          
        sizeField=new JFormattedTextField(NumberFormat.getInstance());
        sizeField.setColumns(3);
        brushModel=new JComboBox(new String[] {"Normal","Eraser"});
        
        JPanel optionsPanel=new JPanel(new FlowLayout());        
        optionsPanel.add(new JLabel("Brush Size :"));
        optionsPanel.add(sizeField);
        optionsPanel.add(new JLabel("          "));
        optionsPanel.add(new JLabel("Brush Model"));
        optionsPanel.add(brushModel);
        
        boardPanel=new myPanel();
        
        add(optionsPanel,BorderLayout.NORTH);
        add(boardPanel,BorderLayout.CENTER);
        setMeVisiblePlz();
    
        refreshThread=new Thread(new Runnable() {

            @Override
            public void run() {
                while (isVisible() && !closing) {
                    
                    String exAddMsg=addMsg;
                    String exRmMsg=rmMsg;
                    synchronized(White_board.this){
                        try {
                            White_board.this.wait(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(White_board.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    if (exAddMsg.equals(addMsg)) {  
                        String prifix="boardAdd:"+client.getProfile().getUserName()+":"+from+":";
                        client.sendMsg(prifix+addMsg);
                        addLine=0;
                        addMsg="";                        
                    }
                    if (exRmMsg.equals(rmMsg)) {

                        String prifix="boardRemove:"+
                                client.getProfile().getUserName()+":"+from+":";
                        client.sendMsg(prifix+rmMsg);
                        rmLine=0;
                        rmMsg="";
                    }
                    
                }
                
                
            }
        });
        
        refreshThread.start();
        }//end else
    }//end generator

    
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(400,400);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() 
        {

            @Override
            public void windowClosing(WindowEvent e) {
                Thread t=new Thread(new Runnable() {

                    @Override
                    public void run() {
                        close();
                    }
                });
                
                t.setPriority(Thread.MAX_PRIORITY);
                t.start();
            }
            
        });
        
        
        setVisible(true);
        
    }// set Me ... end 
    
    public void close() {
        closing =true;
        addMsg="";
        rmMsg="";
        addLine=0;
        rmLine=0;
        
        setVisible(false);
        for (Thread t:myThreads)
            t.interrupt();
        if(t_for_send_point!=null)
            t_for_send_point.interrupt();
        
        client.boardsMap.remove(from);        
        client.sendMsg("boardClose:"+
            client.getProfile().getUserName()+":"+from+":msg");
        dispose();
    }
    
    public void reqToPaint() {
        boardPanel.repaint();
    }
    
    public void reqToRemove(Point p) {
        
        for (int i=0;i<points.size();i++){
            
            if (i>=points.size())
                break;
            int size;
            if(pointSizes.containsKey(i))
                size=pointSizes.get(i);
            else
                size=lastSize;
            
            if ( Math.abs(points.get(i).x-p.x)<size &&  
                    Math.abs(points.get(i).y-p.y)<size   ){
                pointSizes.remove(i);
                points.remove(i);                
                if(i!=0)
                    i--;
            }
        }//end for
        
    }//end req TO remove
    private class myPanel extends JPanel {
        

        public myPanel() {
        
            addMouseMotionListener(new MouseMotionAdapter() 
            {
                

                @Override
                public void mouseDragged(MouseEvent e) {
                if (closing)
                    return;
                    
                    final MouseEvent fe=e;
                    
                t_for_send_point=new Thread(new Runnable() {

                        @Override
                        public void run() {
                            if (brushModel.getSelectedIndex()==0){
                                points.add(new Point(fe.getX(), fe.getY()));
                                try{
                                pointSizes.put(points.size()-1,Integer.valueOf(sizeField.getText()));
                                }catch (NumberFormatException ex) {
                                    pointSizes.put(points.size()-1,lastSize);
                                }
                                if (!sizeField.getText().isEmpty()){
                                    try{                                        
                                    pointSizes.put(points.size()-1,Integer.valueOf(sizeField.getText()));
                                    lastSize=Integer.valueOf(sizeField.getText());
                                    }catch(NumberFormatException ex) {
                                        System.out.println(" :))))))))))) ");
                                    }
                                }//end if
                                addMsg+=
                                        +fe.getX()+" "+fe.getY()+" "+lastSize+";";
                                addLine++;
                                
                                if (addLine>=40){
                                    String prifix="boardAdd:"+client.getProfile().getUserName()+":"+from+":";
                                    client.sendMsg(prifix+addMsg);
                                    addLine=0;
                                    addMsg="";
                                }
                                
                            }
                            else{
                                for (int i=0;i<points.size();i++){
                                    rmLine++;
                                    if (i>=points.size())
                                        break;
                                    
                                    if ( Math.abs(points.get(i).x-fe.getX())<10 &&  
                                         Math.abs(points.get(i).y-fe.getY())<10   ){
                                        rmMsg+=points.get(i).x+" "+points.get(i).y+";";
                                        
                                        
                                        if (rmLine>=40){
                                            String prifix="boardRemove:"+
                                                    client.getProfile().getUserName()+":"+from+":";
                                            client.sendMsg(prifix+rmMsg);
                                            rmLine=0;
                                            rmMsg="";
                                        }
                                        pointSizes.remove(i);
                                        points.remove(i);
                                        if(i!=0)
                                            i--;
                                    }
                                }//end for
                            }//end else
                            repaint();
                        }

                    });
                
                try{
                t_for_send_point.start();
                myThreads.add(t_for_send_point);
                t_for_send_point.setPriority(Thread.MIN_PRIORITY);
                
                if (myThreads.size()>myPoolSize) {
                    for (Thread t:myThreads){
                        t.join();
                    }
                    myThreads.removeAll(myThreads);
                }
                
                try {
                        t_for_send_point.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(White_board.class.getName()).log(Level.SEVERE, null, ex);
                        
                    }
                }catch(Exception ex){}
                }

            });
         
            
            
            
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size;
            for (int i=0;i<points.size();i++){
                if (pointSizes.containsKey(i))
                    size=pointSizes.get(i);
                else{
                    size=lastSize;
                    pointSizes.put(i, size);
                }
                g.fillRoundRect((int)points.get(i).x,(int)points.get(i).y, size,size,size,50);
            }
        }
        
   
        
        
    }//end private class
}
