package com.app.mhwan.easymessage.CustomBase;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.app.mhwan.easymessage.Activity.SendSMSActivity;
import com.app.mhwan.easymessage.CustomView.ContactItem;
import com.app.mhwan.easymessage.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

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

    public ArrayList<ContactItem> getContactList() {
        if (contactItems != null) {
            return contactItems;
        }
        else {
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

            contactItems = new ArrayList<ContactItem>();
            if (cursor.moveToFirst()) {
                do {
                    String number = cursor.getString(0).replaceAll("-", "");
                    if (number.length() == 10) {
                        if (number.startsWith("02"))
                            number = number.substring(0, 2) + "-" + number.substring(2, 6) + "-" + number.substring(6);
                        else
                            number = number.substring(0, 3) + "-" + number.substring(3, 6) + "-" + number.substring(6);
                    } else if (number.length() > 8) {
                        number = number.substring(0, 3) + "-" + number.substring(3, 7) + "-" + number.substring(7);
                    }
                    else if (number.length() == 8){
                        number = number.substring(0, 4) + "-" +number.substring(4);
                    }
                    long photo_id = cursor.getLong(2);
                    long person_id = cursor.getLong(3);

                    ContactItem contactItem = new ContactItem();
                    contactItem.setUser_phNumber(number);
                    contactItem.setUser_Name(cursor.getString(1));
                    if (photo_id == 0)
                        contactItem.setColor_Id(random.nextInt(AppContext.getContext().getResources().getIntArray(R.array.user_color).length));
                    else {
                        contactItem.setPhoto_id(photo_id);
                        contactItem.setPerson_id(person_id);
                    }
                    contactItems.add(contactItem);
                } while (cursor.moveToNext());
            }

            return contactItems;
        }
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

    public String getCountryISO(SendSMSActivity.LOCALE_TYPE type) {
        String countryCode = "";
        if (type.equals(SendSMSActivity.LOCALE_TYPE.NETWORK)) {
            TelephonyManager tm = (TelephonyManager) AppContext.getContext().getSystemService(AppContext.getContext().TELEPHONY_SERVICE);
            countryCode = tm.getNetworkCountryIso();
        } else if (type.equals(SendSMSActivity.LOCALE_TYPE.LANGUAGE)) {
            countryCode = AppContext.getContext().getResources().getConfiguration().locale.getDisplayLanguage();
        }
        DLog.i("country ; " + countryCode);
        return countryCode.toUpperCase();
    }

    public class BasicInfo{
        //Request Permission
        public static final int REQUEST_CALL = 0x23;
        public static final int REQUEST_READ_CONTACT = 0x42;
        public static final int REQUEST_SEND_SMS = 0x0;

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

        //time format
        public static final String SEND_SMS_DATETIME_FORMAT = "a HH:mm / yyyy.MM.dd";
        public static final String GENERAL_DATETIME_FORMAT = "yyyy/MM/dd a K:mm";
        public static final String DB_DATETIME_FORMAT = "yyyy-MM-dd HH.mm";


        //message activity(name, number), result code(new, not)
        public static final String U_NAME = "name_for_user";
        public static final String U_NUMBER = "phNum_for_user";
        public static final int RESULT_NEW_MESSAGE = 0x119;
        public static final int RESULT_NOT_NEW = 0x211;
        public static final int MESSAGE_REQUEST = 0x214;
    }
}
