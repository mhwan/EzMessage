package com.app.mhwan.easymessage.CustomBase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.app.mhwan.easymessage.Activity.MainActivity;
import com.app.mhwan.easymessage.Activity.MessageActivity;
import com.app.mhwan.easymessage.Activity.SettingActivity;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by Mhwan on 2016. 6. 7..
 */
public class NotificationHelper {
    private Context context;
    public static final String KEY_TITLE_NOTIFICATION = "title_notification";
    public static final String KEY_CONTENT_NOTIFICATION = "content_notification";
    public static final String KEY_DETAIL_NOTIFICATION = "detail_notification";
    public static final int ID_RECEIVE_NOTIFICATION = 0x72;
    public static final int ID_SCHEDULE_NOTIFICATION = 0x37;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void showNotfification(NotificationType type, Map<String, Object> map) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isnotifi = preferences.getBoolean(SettingActivity.KEY_BOOL_NOTIFICATION, true);
        boolean ispopup = preferences.getBoolean(SettingActivity.KEY_POPUP_NOTIFICATION, true);

        //알림, 팝업 알림이 모두 참일경우를 제외하고 알림x
        if (!(isnotifi && ispopup))
            return;

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setLights(0xaa00ff, 3000, 100);

        int notifi_id = 0;
        Intent intent;
        switch (type) {
            case RECEIVE_MESSAGE:
                notifi_id = ID_RECEIVE_NOTIFICATION;
                String snumber = (String) map.get(KEY_TITLE_NOTIFICATION);
                String scontent = (String) map.get(KEY_CONTENT_NOTIFICATION);
                long time = ((Date) map.get(KEY_DETAIL_NOTIFICATION)).getTime();
                MessageDBHelper dbHelper = new MessageDBHelper(context);
                //안읽은 메시지가 여러개일때
                int number = dbHelper.getNoReadMessageNumber();
                boolean isdifferent = dbHelper.isDifferentDestination();
                if (number > 1 && isdifferent) {
                    intent = new Intent(context, MainActivity.class);
                    intent.putExtra(AppUtility.BasicInfo.KEY_INTENT_SCHEDULE, false);
                } else {
                    intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(AppUtility.BasicInfo.U_NAME, AppUtility.getAppinstance().getUserName(snumber));
                    intent.putExtra(AppUtility.BasicInfo.U_NUMBER, snumber);
                }
                intent.putExtra(AppUtility.BasicInfo.KEY_NOTIFICATION_ID, notifi_id);

                String name = AppUtility.getAppinstance().getUserName(snumber);
                builder.setContentTitle((name != null)? name : snumber)
                        .setContentText(scontent)
                        .setNumber(number)
                        .setWhen(time)
                        .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                break;
            case SCHEDULE_MESSAGE:
                notifi_id = ID_SCHEDULE_NOTIFICATION;
                String title = (String) map.get(KEY_TITLE_NOTIFICATION);
                String content = (String) map.get(KEY_CONTENT_NOTIFICATION);
                ArrayList<String> numberlist = (ArrayList<String>) map.get(KEY_DETAIL_NOTIFICATION);
                if (numberlist.size() > 1) {
                    intent = new Intent(context, MainActivity.class);
                    intent.putExtra(AppUtility.BasicInfo.KEY_INTENT_SCHEDULE, false);
                } else {
                    intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(AppUtility.BasicInfo.U_NAME, AppUtility.getAppinstance().getUserName(numberlist.get(0)));
                    intent.putExtra(AppUtility.BasicInfo.U_NUMBER, numberlist.get(0));
                }
                intent.putExtra(AppUtility.BasicInfo.KEY_NOTIFICATION_ID, notifi_id);

                builder.setContentTitle(title)
                        .setContentText(content)
                        .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(content))
                        .setNumber(numberlist.size())
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                break;
        }

        Notification notification = builder.build();
        switch (Integer.parseInt(preferences.getString(SettingActivity.KEY_NOTIFICATION_TYPE, ""))) {
            //진동
            case 0:
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                break;

            //소리
            case 1:
                notification.defaults |= Notification.DEFAULT_SOUND;
                break;
        }
        nManager.notify(notifi_id, notification);

    }

    public enum NotificationType {RECEIVE_MESSAGE, SCHEDULE_MESSAGE}
}
