package com.app.mhwan.easymessage.CustomBase;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;

import com.app.mhwan.easymessage.Activity.MainActivity;
import com.app.mhwan.easymessage.Activity.MessageActivity;
import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mhwan on 2016. 4. 2..
 */
public class SMSReceiver extends BroadcastReceiver {
    private static NewMessageListener slistener;
    private ScheduleMessageDBHelper dbHelper;
    private static SMSReceiver smsReceiver;
    private MessageDBHelper messageDBHelper;

    public synchronized static SMSReceiver getSmsReceiver(NewMessageListener listener){
        if (smsReceiver == null)
            smsReceiver = new SMSReceiver();
        slistener = listener;

        return smsReceiver;
    }

    public SMSReceiver() {
    }


    /*
    public SMSReceiver(NewMessageListener listener) {
        this.listener = listener;
    }*/
    public void setListener(NewMessageListener listener){ slistener = listener; }
    /*
    public String getListnerClassName(){
        DLog.d("listener name : "+listener.getClass().getName().toString());
        return listener.getClass().getName().toString();
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        String string = intent.getAction();
        dbHelper = new ScheduleMessageDBHelper(context);
        messageDBHelper = new MessageDBHelper(context);
        //예약문자인 경우
        if (string.equals(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION)) {
            ArrayList<String> phNum = intent.getStringArrayListExtra(AppUtility.BasicInfo.SEND_SCHEDULE_PHNUM);
            String msContent = intent.getStringExtra(AppUtility.BasicInfo.SEND_SCHEDULE_MESSAGE);
            int request = intent.getExtras().getInt(AppUtility.BasicInfo.KEY_PREF_REQUEST);
            DLog.d("예약문자 : " + phNum + ", " + msContent + ", " + request);
            dbHelper.editIsSendSchedule(request, true);
            MessageManager manager = new MessageManager(phNum, msContent);
            boolean isSuccessSend = manager.sendMessage(false);
            messageDBHelper.changeMessageScheduleStatus(request);

            NotificationManager nManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            if (isSuccessSend) {
                Intent intent1;
                if (phNum.size()>1) {
                    intent1 = new Intent(context, MainActivity.class);
                    intent1.putExtra(AppUtility.BasicInfo.KEY_INTENT_SCHEDULE, false);
                }
                else {
                    intent1 = new Intent(context, MessageActivity.class);
                    intent1.putExtra(AppUtility.BasicInfo.U_NAME, AppUtility.getAppinstance().getUserName(phNum.get(0)));
                    intent1.putExtra(AppUtility.BasicInfo.U_NUMBER, phNum.get(0));
                }
                android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setLights(0xaa00ff, 3000, 100)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(context.getString(R.string.send_schedule_succeessfully))
                        .setContentText(msContent)
                        .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(msContent))
                        .setContentIntent(PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT));
                nManager.notify(3, builder.build());

                if (slistener != null)
                    slistener.updateNewMessage(false, null);
            }
        } else if (string.equals("android.intent.action.BOOT_COMPLETED")) {
            ArrayList<ScheduleMessageItem> itemArrayList = dbHelper.getAllScheduleMessage();
            if (itemArrayList.size() > 0) {
                for (ScheduleMessageItem item : itemArrayList) {
                    //현재시간보다 예약시간이 뒤에 있을때 펜딩인텐트에 등록, 현재시간보다 이전이라면 메시지 전송상태를 보낸것으로 체크
                    if (System.currentTimeMillis() <= item.gettimemillis() && !item.getIssend()) {
                        int request = item.getId();
                        DLog.i("recovery message : " + request + ", " + item.getNumberArraylist() + ", " + item.getContent());
                        Intent mIntent = new Intent(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION);
                        mIntent.putExtra(AppUtility.BasicInfo.SEND_SCHEDULE_PHNUM, item.getNumberArraylist());
                        mIntent.putExtra(AppUtility.BasicInfo.SEND_SCHEDULE_MESSAGE, item.getContent());
                        mIntent.putExtra(AppUtility.BasicInfo.KEY_PREF_REQUEST, request);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, request, mIntent, 0);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.gettimemillis(), pendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, item.gettimemillis(), pendingIntent);
                        }
                    } else {
                        dbHelper.editIsSendSchedule(item.getId(), true);
                        messageDBHelper.changeMessageScheduleStatus(item.getId());
                    }
                }
            }
        } else if (string.equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                if (pdusObj != null) {
                    SmsMessage[] smsMessage = new SmsMessage[pdusObj.length];
                    String message = "";
                    String number = null;
                    Date date = null;
                    for (int i = 0; i < smsMessage.length; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = bundle.getString("format");
                            smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i], format);
                        } else
                            smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        //메시지는 길이에따라 쪼개질 수 있음. 번호와 시간은 처음에만 저장.
                        message += smsMessage[i].getDisplayMessageBody();
                        if (number == null && date == null) {
                            number = smsMessage[i].getDisplayOriginatingAddress();
                            date = new Date(smsMessage[i].getTimestampMillis());
                        }
                    }
                    DLog.d("new : "+message+" "+number);
                    SimpleDateFormat format = new SimpleDateFormat(AppUtility.BasicInfo.DB_DATETIME_FORMAT);
                    String sDate = format.format(date);
                    number = number.replace("-", "");

                    MessageItem item = new MessageItem();
                    item.setPh_number(number);
                    item.setContent(message);
                    item.setTime(sDate);
                    item.setIs_read(false);
                    item.setIs_last_message(true);
                    item.setType(1);
                    item.setRequest_code(-1);
                    int code = messageDBHelper.getSavedColorId(number);
                    //-1이라면 db에 저장이 안되있으므로 새로 저장할 코드 생성
                    item.setColor_id((code < 0)? AppUtility.getAppinstance().getColorIdtoDB(number) : code);
                    messageDBHelper.addMessage(item);

                    if (slistener != null)
                        slistener.updateNewMessage(true, number);
                }
            }
        }
    }
}
