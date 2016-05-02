package com.app.mhwan.easymessage.CustomBase;

import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Created by Mhwan on 2016. 4. 8..
 */
public class MessageManager {
    private ArrayList<String> numberList;
    private String messageContent;
    private SmsManager smsManager;
    private boolean isSuccessed = true;
    public static final String NUM_SEPERATOR = " , ";
    public MessageManager(){
        smsManager = SmsManager.getDefault();
    }
    public MessageManager(ArrayList<String> list, String content){
        this.numberList = list;
        this.messageContent = content;
        smsManager = SmsManager.getDefault();
    }
    public boolean sendMessage(){
        try {
            for (String number : numberList) {
                if (number.isEmpty()) {
                    isSuccessed = false;
                    continue;
                }
                smsManager.sendTextMessage(number, null, messageContent, null, null);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            isSuccessed = false;
        }
        return isSuccessed == true;
    }
    public void setMessage(ArrayList<String> list, String content){
        this.numberList = list;
        this.messageContent = content;
    }
}
