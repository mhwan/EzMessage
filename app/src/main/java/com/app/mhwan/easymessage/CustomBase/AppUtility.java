package com.app.mhwan.easymessage.CustomBase;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.app.mhwan.easymessage.CustomView.ContactItem;
import com.app.mhwan.easymessage.R;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Mhwan on 2016. 4. 5..
 */
public class AppUtility {
    private static AppUtility Appinstance;
    private ArrayList<ContactItem> contactItems;
    private Random random = new Random(System.currentTimeMillis());

    private AppUtility() {
    }

    public synchronized static AppUtility getAppinstance() {
        if (Appinstance == null)
            Appinstance = new AppUtility();
        return Appinstance;
    }

    public Bitmap loadContactPhoto(ContentResolver cr, long id, long photo_id) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input != null)
            return resizingBitmap(BitmapFactory.decodeStream(input));
        else
            DLog.d("PHOTO : first try failed to load photo");

        byte[] photoBytes = null;
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photo_id);
        Cursor c = cr.query(photoUri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
        try {
            if (c.moveToFirst())
                photoBytes = c.getBlob(0);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            c.close();
        }

        if (photoBytes != null)
            return resizingBitmap(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));

        else
            DLog.d("PHOTO : second try also failed");
        return null;
    }

    public Bitmap resizingBitmap(Bitmap oBitmap) {
        if (oBitmap == null)
            return null;
        float width = oBitmap.getWidth();
        float height = oBitmap.getHeight();
        DLog.d("oBitmap : " + width + ", " + height);
        float resizing_size = 120;
        Bitmap rBitmap = null;
        if (width > resizing_size) {
            float mWidth = (float) (width / 100);
            float fScale = (float) (resizing_size / mWidth);
            width *= (fScale / 100);
            height *= (fScale / 100);

        } else if (height > resizing_size) {
            float mHeight = (float) (height / 100);
            float fScale = (float) (resizing_size / mHeight);
            width *= (fScale / 100);
            height *= (fScale / 100);
        }

        DLog.d("rBitmap : " + width + ", " + height);
        rBitmap = Bitmap.createScaledBitmap(oBitmap, (int) width, (int) height, true);
        return rBitmap;
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = AppContext.getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public int getTextByte(String s){
        int en = 0, kr = 0, etc = 0;
        char[] string = s.toCharArray();

        for (int i = 0; i < s.length(); i++) {
            if (string[i] >= 'A' && string[i] <= 'z')
                en++;
            else if (string[i] >= '\uAC00' && string[i] <= '\uD7A3')
                kr += 2;
            else
                etc++;
        }
        return en + kr + etc;
    }

    public String changeNumberFormat(String number){
        number = number.replaceAll("-", "");

        if (getCountryISO(LOCALE_TYPE.NETWORK).equals("KR")) {
            //집 전화번호
            if (number.length() == 10 || number.startsWith("02")) {
                //서울
                if (number.startsWith("02")) {
                    int length = number.length();
                    number = number.substring(0, 2) + "-" + number.substring(2, length - 4) + "-" + number.substring(length - 4, length);
                }
                //나머지
                else
                    number = number.substring(0, 3) + "-" + number.substring(3, 6) + "-" + number.substring(6);
            } else if (number.length() > 8) {
                number = number.substring(0, 3) + "-" + number.substring(3, 7) + "-" + number.substring(7);
            } else if (number.length() == 8) {
                number = number.substring(0, 4) + "-" + number.substring(4);
            }
        } else {
            if (number.length() == 10 && number.length()>=6)
                number = number.substring(0, 3) + "-" + number.substring(3, 6)+ "-"+number.substring(6);
            else if (number.startsWith("1") && number.length() >= 7)
                number = number.substring(0, 1) + "-" + number.substring(1, 4)+ "-"+number.substring(4, 7)+"-"+number.substring(7);
            else if (number.length() >=6)
                number = number.substring(0, 3) + "-" + number.substring(3, 6)+ "-"+number.substring(6);
            else
                number = number.substring(0, 3) + "-" + number.substring(3);
        }

        return number;
    }

    public ArrayList<ContactItem> getContactList() {
        if (contactItems != null) {
            return contactItems;
        } else {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_ID,
                    ContactsContract.Contacts._ID
            };

            String[] selectionArgs = null;

            String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";

            Cursor cursor = AppContext.getContext().getContentResolver().query(uri, projection, null,
                    selectionArgs, sortOrder);

            LinkedHashSet<ContactItem> hashlist = new LinkedHashSet<>();
            if (cursor.moveToFirst()) {
                do {
                    long photo_id = cursor.getLong(2);
                    long person_id = cursor.getLong(3);

                    ContactItem contactItem = new ContactItem();
                    contactItem.setUser_phNumber(changeNumberFormat(cursor.getString(0)));
                    contactItem.setUser_Name(cursor.getString(1));
                    if (photo_id == 0)
                        contactItem.setColor_Id(random.nextInt(AppContext.getContext().getResources().getIntArray(R.array.user_color).length));
                    else
                        contactItem.setColor_Id(-1);

                    contactItem.setPhoto_id(photo_id);
                    contactItem.setPerson_id(person_id);

                    boolean result = hashlist.add(contactItem);
                    DLog.d(contactItem.getUser_Name()+" : "+result);
                } while (cursor.moveToNext());
            }

            contactItems = new ArrayList<>(hashlist);

            for (int i =0; i<contactItems.size(); i++){
                contactItems.get(i).setId(i);
            }
            return contactItems;
        }
    }

    public String getMyPhoneNumber() {
        TelephonyManager tm = (TelephonyManager) AppContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();

        return number.replace("-", "");
    }

    public boolean isSimSupport(){
        TelephonyManager tm = (TelephonyManager) AppContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = AppContext.getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public void finishApplication(Activity activity) {
        activity.moveTaskToBack(true);
        activity.finish();
    }


    /**
     * return value : network (KR, US 등), Language (한국어, 등)
     */
    public String getCountryISO(LOCALE_TYPE type) {
        String countryCode = "";
        if (type.equals(LOCALE_TYPE.NETWORK)) {
            TelephonyManager tm = (TelephonyManager) AppContext.getContext().getSystemService(AppContext.getContext().TELEPHONY_SERVICE);
            countryCode = tm.getNetworkCountryIso();
            DLog.d("Network : " + countryCode);
        } else if (type.equals(LOCALE_TYPE.LANGUAGE)) {
            countryCode = AppContext.getContext().getResources().getConfiguration().locale.getDisplayLanguage();
            DLog.d("Language : " + countryCode);
        }
        return countryCode.toUpperCase();
    }

    public int[] getDisplaySize(){
        Display display = ((WindowManager) AppContext.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width, height;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            width = display.getWidth();
            height = display.getHeight();
        } else {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        return new int[] { width, height };
    }

    public boolean isToday(String sDate, String sFormat){
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sFormat);
        try {
            date = simpleDateFormat.parse(sDate);

            if (date == null)
                throw new IllegalArgumentException("Date is null");
        } catch (ParseException | IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }

        Calendar cToday = Calendar.getInstance();
        Calendar cDate = Calendar.getInstance();
        cDate.setTime(date);

        return (cToday.get(Calendar.ERA) == cDate.get(Calendar.ERA)) && (cToday.get(Calendar.YEAR) == cDate.get(Calendar.YEAR)) && (cToday.get(Calendar.DAY_OF_YEAR) == cDate.get(Calendar.DAY_OF_YEAR));
    }

    public Date getDate(String sDate, String sFormat){
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sFormat);
        try {
            date = simpleDateFormat.parse(sDate);

            if (date == null)
                throw new IllegalArgumentException("Date is null");
        } catch (ParseException | IllegalArgumentException e){
            e.printStackTrace();
        }

        return date;
    }
    public String changeDateTimeFormat(String time, String original_format, String new_format){
        SimpleDateFormat fOriginal = new SimpleDateFormat(original_format);
        SimpleDateFormat fNew = new SimpleDateFormat(new_format);
        String newtime;
        try {
            Date d = fOriginal.parse(time);
            newtime = fNew.format(d);
        } catch (ParseException e){
            e.printStackTrace();
            newtime = time;
        }

        return newtime;
    }
    /**
     * 상대방 휴대폰 번호를 받아 주소록에 저장된 이름을 반환한다.
     * (상대방 번호가 내 번호와 같다면 ME(나) 반환, 이름이 없으면 null
     */
    public String getUserName(String ph_num) {
        if (ph_num.equals(getMyPhoneNumber()))
            return AppContext.getContext().getString(R.string.me);
        ArrayList<ContactItem> list = getContactList();
        for (ContactItem item : list) {
            if (item.getPhNumberChanged().equals(ph_num))
                return item.getUser_Name();
        }
        return null;
    }

    /**
     * long index (0: person, 1: photo)
     *
     * 상대방 photo, person id 반환 (자기 자신, 주소록에 없으면 0, 0)
     */
    public long[] getPhotoPersonId(String ph_num){
        long[] id_list = new long[]{0, 0};
        if (ph_num.equals(getMyPhoneNumber()))
            return id_list;
        ArrayList<ContactItem> list = getContactList();
        for (ContactItem item : list){
            if (item.getPhNumberChanged().equals(ph_num)) {
                id_list[0] = item.getPerson_id();
                id_list[1] = item.getPhoto_id();
                break;
            }
        }
        return id_list;
    }

    /**
     * db에 저장될 컬러아이디, 연락처에 저장되어있거나 자신의 핸드폰 번호일 경우 -1 반환, 랜덤값 반환
     * @param ph_number
     * @return
     */
    public int getColorIdtoDB(String ph_number){
        return (getSaved(ph_number) || getMyPhoneNumber().equals(ph_number))? -1 : random.nextInt(AppContext.getContext().getResources().getIntArray(R.array.user_color).length);
    }

    public boolean getSaved(String ph_number){
        boolean issaved = false;
        ArrayList<ContactItem> list = getContactList();
        for (ContactItem item : list){
            if (item.getPhNumberChanged().equals(ph_number)){
                issaved = true;
                break;
            }
        }

        return issaved;
    }

    /**
     * color id 반환
     *
     * 자기자신이라면 27, 상대방의 저장된 컬러 아이디 반환 (저장이 안되있다면
     */
    public int getColorId(String ph_num){
        if (ph_num.equals(getMyPhoneNumber()))
            return 27;
        int id = -1;
        boolean issaved = false;
        ArrayList<ContactItem> list = getContactList();
        for (ContactItem item : list){
            if (item.getPhNumberChanged().equals(ph_num)){
                id = item.getColor_Id();
                issaved = true;
                break;
            }
        }
        if (!issaved)
            id = 14;
        return id;
    }

    public boolean isAppRunning() {
        boolean a = false;
        try {
            a =  new AppRunningCheck().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            return a;
        }
    }

    public enum LOCALE_TYPE { NETWORK, LANGUAGE }

    private class AppRunningCheck extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            final Context context = AppContext.getContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals("com.app.mhwan.easymessage")) {
                    return true;
                }
            }
            return false;
        }
    }

    public class BasicInfo {
        //Request Permission
        public static final int REQUEST_CALL = 0x23;
        public static final int REQUEST_READ_CONTACT = 0x42;
        public static final int REQUEST_SEND_SMS = 0x0;
        public static final int REQUEST_PHONE_STATE = 0x52;
        public static final int REQUEST_WRITE_STORAGE = 0x68;
        public static final int REQUEST_READ_SMS = 0x62;

        //Send message Result code
        public static final int SEND_REQUEST = 0x121;
        public static final int SEND_ERROR = 0x144;
        public static final int SEND_RESERVED_SUCCESS = 0x113;

        //Send type, to cell phone number
        public static final String SEND_TYPE = "TYPE";
        public static final String SEND_PHONE_NUMBER = "NUMBER_key";
        public static final String SEND_CONTENT = "message_content";

        //select contact
        public static final int REQUEST_SELECT_CONTACT = 0x116;
        public static final String SELECT_CONTACT_LIST = "select_contactlist";
        public static final String KEY_RESULT_SELECTED_ID = "selected_id_list";

        //receiver action
        public static final String SCHEDULED_SEND_ACTION = "Mhwan.eZMessage.ScheduleSMS";

        //receiver message, schedule
        public static final String SEND_SCHEDULE_PHNUM = "SEND_SCHEDULE_NUMBER";
        public static final String SEND_SCHEDULE_MESSAGE = "SEND_SCHEDULE_CONTENT";
        public static final String KEY_PREF_REQUEST = "KEY_REQUEST_CODE";
        public static final String KEY_INTENT_SCHEDULE = "key_intent_scheduelded";
        public static final String KEY_NOTIFICATION_ID = "key_notification_id";

        //time format
        public static final String SEND_SMS_DATETIME_FORMAT = "a HH:mm / yyyy.MM.dd";
        public static final String GENERAL_DATETIME_FORMAT = "yyyy/MM/dd a h:mm";
        public static final String DB_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String GENERAL_TIME_FORMAT = "a h:mm";


        //message activity(name, number), result code(new, not)
        public static final String U_NAME = "name_for_user";
        public static final String U_NUMBER = "phNum_for_user";
        public static final int RESULT_NEW_MESSAGE = 0x119;
        public static final int RESULT_NOT_NEW = 0x211;
        public static final int MESSAGE_REQUEST = 0x214;
        public static final int SETTING_REQUEST = 0x218;
    }
}
