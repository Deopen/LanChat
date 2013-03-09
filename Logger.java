/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class Logger extends JFrame{
    
    
    

        private BufferedReader bufReader;
        private JTextArea logField;
        
        public Logger(InputStreamReader in) 
                throws HeadlessException {
            setTitle("[  Deopen LanChat --- Error Log  ]");
            
            
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            
            bufReader=new BufferedReader(in);
            logField=new JTextArea();
            logField.setEditable(false);
            logField.setBackground(Color.BLACK);
            logField.setForeground(Color.RED);
            logField.setSelectionColor(Color.WHITE);
            logField.setFont(new Font("lucida console",0,13));
           
            
            
            add(new JScrollPane(logField));
      
            
            
            logField.setAutoscrolls(true);
            
            setMeVisiblePlz();
            
            
            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (isVisible()) {

                        try {
                            
                            String line=bufReader.readLine();
                            
                            if (line!=null && line.length()!=0){
                                
                                append(line);
                            }
                            
                        } catch (IOException ex) {
                        }
                        
                    }
                }
            }).start();
            
        
        }//end 
         
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
        
        
        setSize(700,400);
        setLocationRelativeTo(null);
        
        setVisible(true);
        
    
        }// set Me ... end 
        
    
    
}
