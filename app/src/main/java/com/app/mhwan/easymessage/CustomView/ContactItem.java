package com.app.mhwan.easymessage.CustomView;

import java.io.Serializable;

/**
 * Created by Mhwan on 2016. 3. 23..
 */
public class ContactItem implements Serializable{
    private String user_phNumber, user_Name;
    private int color_Id = -1;
    private boolean isChecked = false;
    private long photo_id=0, person_id=0;
    private Contact_type type = Contact_type.NAME_TYPE;

    public ContactItem(){}
    public ContactItem(String number, Contact_type type){
        user_phNumber = number;
        user_Name = "noName";
        this.type = type;
    }
    public long getPhoto_id(){
        return photo_id;
    }
    public long getPerson_id(){
        return person_id;
    }
    public void setPhoto_id(long id){
        this.photo_id = id;
    }
    public void setPerson_id(long id){
        this.person_id = id;
    }
    public Contact_type getType(){
        return type;
    }
    public int getColor_Id(){return color_Id; }
    public boolean getChecked(){
        return isChecked;
    }
    public void setChecked(boolean b){
        isChecked = b;
    }
    public String getUser_phNumber(){
        return user_phNumber;
    }
    public String getUser_Name(){
        return user_Name;
    }
    public void setUser_phNumber(String string){
        this.user_phNumber = string;
    }
    public String getPhNumberChanged(){
        return user_phNumber.replace("-", "");
    }
    public void setUser_Name(String string){
        this.user_Name = string;
    }
    public void setColor_Id(int id){
        this.color_Id = id;
    }
    @Override
    public String toString() {
        if (type.equals(Contact_type.NUMBER_TYPE))
            return user_phNumber;
        return user_Name;
    }
    public enum Contact_type { NAME_TYPE, NUMBER_TYPE}
}
