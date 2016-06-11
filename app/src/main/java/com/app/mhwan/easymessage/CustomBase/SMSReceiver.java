package com.app.mhwan.easymessage.CustomBase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public void setListener(NewMessageListener listener){ slistener = listener; }


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

            if (isSuccessSend) {
                if (slistener != null)
                    slistener.updateNewMessage(false, null);
                if (!AppUtility.getAppinstance().isAppRunning()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(NotificationHelper.KEY_TITLE_NOTIFICATION, context.getString(R.string.send_schedule_succeessfully));
                    map.put(NotificationHelper.KEY_CONTENT_NOTIFICATION, msContent);
                    map.put(NotificationHelper.KEY_DETAIL_NOTIFICATION, phNum);

                    NotificationHelper helper = new NotificationHelper(context);
                    helper.showNotfification(NotificationHelper.NotificationType.SCHEDULE_MESSAGE, map);
                }
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

                    if (!AppUtility.getAppinstance().isAppRunning()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(NotificationHelper.KEY_TITLE_NOTIFICATION, number);
                        map.put(NotificationHelper.KEY_CONTENT_NOTIFICATION, message);
                        map.put(NotificationHelper.KEY_DETAIL_NOTIFICATION, date);

                        NotificationHelper helper = new NotificationHelper(context);
                        helper.showNotfification(NotificationHelper.NotificationType.RECEIVE_MESSAGE, map);
                    }
                }
            }
        }
    }
}
