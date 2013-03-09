/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class ListChooser extends JFrame{

    private JList list;
    private DefaultListModel listElemets;
    JTextField statArea;
    private String selectedVal=null;
    public ListChooser(ArrayList<String> l,String msg,String title) {
        
        setTitle(title);
        
        listElemets=new DefaultListModel();
        
        for (String e:l)
            listElemets.addElement(e);
        
        list=new JList(listElemets);
        statArea=new JTextField("Double Click To "+msg);        
        add(new JScrollPane(list),BorderLayout.CENTER);
        statArea.setFont(new Font("Arial", 0, 15));
        statArea.setEditable(false);
        statArea.setBackground(Color.DARK_GRAY);
        statArea.setForeground(Color.WHITE);
        statArea.setSelectionColor(Color.DARK_GRAY);
        statArea.setHorizontalAlignment(JTextField.CENTER);
        add(statArea,BorderLayout.NORTH);
        
        list.addMouseListener(new MouseAdapter() 
        {

            @Override
            public void mouseClicked(MouseEvent e) {
                JList l=(JList)e.getSource();
                if(e.getClickCount()>=2 && !l.isSelectionEmpty() ){
                    selectedVal=(String) listElemets.get(l.getSelectedIndex());
                }
            }
            
        });
        
        
        setMeVisiblePlz();
        
    }//end generator
    
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        setSize(250,290);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
    }// set Me ... end 
    
    public String getSelectedValue() {
        
        while (selectedVal==null) {
            synchronized(this) {
                try {
                    this.wait(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ListChooser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        dispose();
        return selectedVal;
    }//end get choose
    
}
