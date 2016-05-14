package com.app.mhwan.easymessage.CustomBase;

import android.content.Context;
import android.telephony.SmsManager;

import com.app.mhwan.easymessage.CustomView.MessageItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mhwan on 2016. 4. 8..
 */
public class MessageManager {
    private ArrayList<String> numberList;
    private String messageContent;
    private SmsManager smsManager;
    private boolean isSuccessed = true;
    private MessageDBHelper dbHelper;
    private Context context;
    public static final String NUM_SEPERATOR = " , ";
    public MessageManager(){
        smsManager = SmsManager.getDefault();
    }
    public MessageManager(ArrayList<String> list, String content){
        this.numberList = list;
        this.messageContent = content;
        smsManager = SmsManager.getDefault();
    }
    public boolean sendMessage(boolean addtodb){
        if (addtodb)
            dbHelper = new MessageDBHelper(context);
        try {
            for (String number : numberList) {
                if (number.isEmpty()) {
                    isSuccessed = false;
                    continue;
                }
                smsManager.sendTextMessage(number, null, messageContent, null, null);
                if (addtodb)
                    addMessageToDB(number);
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

    public void setContext(Context context){
        this.context = context;
    }
    private void addMessageToDB(String number){
        SimpleDateFormat dateFormat = new SimpleDateFormat(AppUtility.BasicInfo.DB_DATETIME_FORMAT);
        MessageItem item = new MessageItem();
        item.setPh_number(number.replace("-", ""));
        item.setContent(messageContent);
        item.setTime(dateFormat.format(new Date(System.currentTimeMillis())));
        item.setType(0);
        item.setIs_last_message(true);
        item.setIs_read(true);
        item.setRequest_code(-1);

        dbHelper.addMessage(item);
    }
}
