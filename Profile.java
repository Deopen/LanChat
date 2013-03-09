/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanchat;

import java.util.HashSet;

/**
 *
 * @author Omid Yaghoubi
 * @author DeOpenMail@Gmail.com
 */
public class Profile {
    
    private String name;
    private String family;
    private String user;
    private HashSet<String> friendList;
    private HashSet<String> banList;
    private HashSet<String> onlineList;
    private boolean updateOnlist=false;
    private boolean updateFlist=false;
    
    public Profile
            (String N,
            String F,
            HashSet<String> fList,
            HashSet<String> bList
            ) {
        
        name=N;
        family=F;
        
        friendList=fList;
        banList=bList;
        onlineList=new HashSet<>();
    }// end generator
    
    
    public void setUserName(String uName) {        
        user=uName;
    }
    
    public String getFullName() {
        return name+" "+family;
    }
    
    public String getUserName(){
        return user;
    }
    
    public String getFullNamePlusUserName() {
        return getFullName()+" ("+getUserName()+")";
    }
    
    
    public HashSet<String> getFriendList() {
             
        return new HashSet<>(friendList);
    }
    
    public HashSet<String> getBanList() {
        return banList;
    }
    
    public boolean isItBan(String name) {
        return banList.contains((String)name);        
    }//end is it ban 
    
    public HashSet<String> getOnList() {
        return onlineList;
    }
    public void setOnList(HashSet<String> l) {
        boolean equality=true;
        
        for (String e:l){
            if (!onlineList.contains(e)){
                equality=false;
                break;
            }//end if
        }//end for
        if (equality){
        for (String e:onlineList){
            if (!l.contains(e)){
                equality=false;
                break;
            }//end if
        }//end for
        }//end if eq
        
        
        if (!equality){
            onlineList=l;
            updateOnlist=true;           
        }
        
    }//end method
    
    public void setFullName(String fullName) {
        name=fullName.split(" ")[0];
        family=fullName.split(" ")[1];
    }

    @Override
    public String toString() {
        return ("name : "+name+" , family : "
                +family+" , user : "+user+
                " , friends : "+friendList+ 
                " , ban_list : "+banList);
    }
    
    public boolean getUpdateOnList() {
        if (updateOnlist) {
            updateOnlist=false;            
            return true;
        }else
            return false;
    }
    
    public boolean getUpdateFList() {
        if (updateFlist) {
            updateFlist=false;           
            return true;
        }else
            return false;
    }
    
    public void addToFriendList(String e) {
        updateFlist=true;
        updateOnlist=true;
        friendList.add(e);
        
    }
    
    public void removeFromFriendList(String e) {
        
        updateFlist=true;
        updateOnlist=true;
        friendList.remove(e);
        onlineList.remove(e);
    }//end remove from friend list
    
    public void removeFromBan(String e) {
        
        updateFlist=true;
        updateOnlist=true;
        banList.remove(e);
    }//end remove from friend list
    
    public void addToBanList(String e) {
        
        removeFromFriendList(e);
        banList.add(e);
        
    }//end remove from friend list
    
}
