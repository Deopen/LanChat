/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/**
 *
 * 
 * This Class is Modified version of Deitel&Deitel TicTacToe ( Java How To Program 6th edt)
 * Modified by Omid Yaghoubi <deopenmail@gmail.com>
 * 
 * 
 */
public final class TicTacToeClient extends JFrame{


    private JPanel boardPanel;
    private JPanel panel2;
    private JLabel youtTurn;
    private Square board[][];
    private Square currentSquare;
    private String winner;
    private String myMark;
    HashMap<Integer,String> map;
    private boolean myTurn;
    private final String X="X";
    private final String O="O";
    private final Client client;
    private final String otherPlayerUsername;
    public TicTacToeClient(String id,String myMark,Client c) {
        youtTurn=new JLabel();
        client=c;
        otherPlayerUsername=id;
        if (!client.getProfile().getOnList().contains(id)) {
            JOptionPane.showMessageDialog(null, "Sorry, "+id+ " is offline !");
            dispose();
            return;
        }
        setTitle(id);
        map=new HashMap<>();
        this.myMark=myMark;
        //first player
        if (myMark.equals("X")){
        client.sendMsg
                ("reqXO:"+
                client.getProfile().
                getUserName()+":"+id+":msg");
        youtTurn.setText("Your Turn . ");
        }else
            youtTurn.setText("Wait ... ");
//        add(youtTurn,BorderLayout.NORTH); 
        
        synchronized(this) {
            try {
                this.wait(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(TicTacToeClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        boardPanel=new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3,0,0));
        myTurn=(myMark.equals(X));
        board= new Square[3][3];
        
        for (int row=0;row<board.length;row++) {
            
            for (int column=0;
                    column<board[row].length;
                    column++) {
                
                board[row][column]=new Square(" ",row*3+column);
                boardPanel.add(board[row][column]);
                
            }// end column
            
            
        }//end for ( row++ )

        
        panel2=new JPanel();
        panel2.add(boardPanel,BorderLayout.CENTER);
        add(panel2,BorderLayout.CENTER);
        JLabel  deitelCopyRight=new JLabel("A Modified Version of Deitel&Deitel TicTacToe ");
        deitelCopyRight.setFont(new Font("Times", 0, 14));                
        add(deitelCopyRight,BorderLayout.SOUTH);
        add(deitelCopyRight,BorderLayout.SOUTH);
        setMeVisiblePlz();
        
    }//end generator
    
    public boolean isAnyOneWon() {
        
        //1  2   3
        //4  5   6
        //7  8   9
        if (map.containsKey(0) && map.containsKey(1) && map.containsKey(2))
        if (map.get(0).equals(map.get(1)) && map.get(1).equals(map.get(2))){
            winner=map.get(0);
            return true;
        }
        if (map.containsKey(0) && map.containsKey(4) && map.containsKey(8))
        if (map.get(0).equals(map.get(4)) && map.get(4).equals(map.get(8))){
            winner=map.get(0);
            return true;
        }
        
        if (map.containsKey(0) && map.containsKey(3) && map.containsKey(6))
        if (map.get(0).equals(map.get(3)) && map.get(3).equals(map.get(6))){
            winner=map.get(0);
            return true;
        }
        if (map.containsKey(1) && map.containsKey(4) && map.containsKey(7))
        if (map.get(1).equals(map.get(4)) && map.get(4).equals(map.get(7))){
            winner=map.get(1);
            return true;
        }
        if (map.containsKey(2) && map.containsKey(4) && map.containsKey(6))
        if (map.get(2).equals(map.get(4)) && map.get(4).equals(map.get(6))){
            winner=map.get(2);
            return true;
        }
        if (map.containsKey(3) && map.containsKey(4) && map.containsKey(5))
        if (map.get(3).equals(map.get(4)) && map.get(4).equals(map.get(5))){
            winner=map.get(3);
            return true;
        }
        if (map.containsKey(2) && map.containsKey(5) && map.containsKey(8))
        if (map.get(2).equals(map.get(5)) && map.get(5).equals(map.get(8))){
            winner=map.get(2);
            return true;
        }
        if (map.containsKey(6) && map.containsKey(7) && map.containsKey(8))
        if (map.get(6).equals(map.get(7)) && map.get(7).equals(map.get(8))){
            winner=map.get(6);
            return true;
        }
       
        
        return false;
    }//end checking
    
    public void setMark(final int index) 
    {
        
        
        final String mark=myMark.equals("X")?"O":"X";
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int i=index/3;
                int j=index%3;
                map.put(index, mark);
                board[i][j].setMark(mark);
                myTurn=true;
                youtTurn.setText("Your Turn .");
                
                if (isAnyOneWon()) {
                    JOptionPane.showMessageDialog(null,"Player "+winner+ " Won !");
                    close();
                }
                
            }
        });
        
    }// end set mark
    
    
    private void setCurrentSquare(Square square) {
        
        currentSquare=square;
        
    }// end setCurrentSquare
    
    
    private void setMeVisiblePlz() {//set me ... begin
        //ok √ (alt + v )
        
        pack();
        setLocationRelativeTo(null);
        
        
        addWindowListener(new WindowAdapter()         
        {

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
            
        
        });
        
        
        setVisible(true);
        
    }// set Me ... end 
 
    public void close() {
        client.sendMsg
                        ("closeXO:"+client.getProfile().getUserName()+
                        ":"+otherPlayerUsername+":msg");
                
        dispose();
    }//end close
    
    private class Square extends JPanel{

        private String mark;
        private int loc;
        
        public Square(String sMark, int sLoc) {
            mark=sMark;
            loc=sLoc;
            
            addMouseListener(new MouseAdapter() 
            
            {

                @Override
                public void mouseReleased(MouseEvent e) {
                    setCurrentSquare(Square.this);                    
                    if (myTurn && !map.containsKey(getLoc())) {
                        
                        map.put(getLoc(), myMark);
                        setMark(myMark);
                        client.sendMsg
                                ("locXO:"+client.getProfile().getUserName()+
                                ":"+otherPlayerUsername+":"+getLoc());
                        myTurn=false;
                        youtTurn.setText("Wait ...");
                        
                        
                        if (isAnyOneWon()) {
                            JOptionPane.showMessageDialog(null,"Player "+winner+ " Won !");
                            close();
                        }
                        
                    }
                }
                
            }// end mouse adapter
                    );
            
        }// end generator

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(30,30);
        }// end get Deminstion

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        
        
        
        public void setMark(String thisMark) {
            mark=thisMark;
            repaint();
        }// end setMark
        
        public int getLoc() {
            return loc;
        }// end get location

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.drawRect(0, 0, 29, 29);
            g.drawString(mark, 11, 20);
        }//end paint componet
        
    }//end inner class (Square)
    
}
