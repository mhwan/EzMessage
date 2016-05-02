package com.app.mhwan.easymessage.CustomBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Mhwan on 2016. 4. 26..
 */
public class MessageDBHelper extends SQLiteOpenHelper {
    public static String DB_NAME = "EZ_MESSAGE_DB";
    public static String Message_List_TABLE_NAME = "Message_list";
    public static String SCHEDULE_TABLE_NAME = "Schedule_message_TABLE";
    public static String SCHEDULE_KEY_ID = "_id";
    public static String SCHEDULE_KEY_CONTENT = "mContent";
    public static String SCHEDULE_KEY_NUMBER = "mNumber";
    public static String SCHEDULE_KEY_TIME = "mTime";
    public static String SCHEDULE_KEY_IS_SEND = "mIsSend";

    public MessageDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEDULE_TABLE = "CREATE TABLE "+SCHEDULE_TABLE_NAME+" ("
                +SCHEDULE_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                SCHEDULE_KEY_CONTENT+" TEXT, "+SCHEDULE_KEY_NUMBER+" TEXT, "+SCHEDULE_KEY_TIME+" TEXT, "+SCHEDULE_KEY_IS_SEND+" INTEGER)";
        db.execSQL(CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+SCHEDULE_TABLE_NAME);
        onCreate(db);
    }

    public void removeSchedule(int id){
        String query = "DELETE FROM " + SCHEDULE_TABLE_NAME + " where " + SCHEDULE_KEY_ID + "= " + id ;
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(query);
    }
    //예약 메시지를 db에 저장하고 그 id값을 요청코드로 반환한다.
    public int addSchedule(ScheduleMessageItem item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SCHEDULE_KEY_CONTENT, item.getContent());
        values.put(SCHEDULE_KEY_NUMBER, item.getnumberString());
        values.put(SCHEDULE_KEY_TIME, item.gettimemillis());
        values.put(SCHEDULE_KEY_IS_SEND, (item.getIssend())? 1:0);
        long id = db.insert(SCHEDULE_TABLE_NAME, null, values);
        db.close();
        return (int)id;
    }
    public void editIsSendSchedule(int id, boolean status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SCHEDULE_KEY_IS_SEND, (status)? 1:0);

        db.update(SCHEDULE_TABLE_NAME, values, SCHEDULE_KEY_ID+"="+id, null);
    }
    public ArrayList<ScheduleMessageItem> getAllScheduleMessage(){
        String SelectQuery = "SELECT  * FROM "+SCHEDULE_TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SelectQuery, null);
        ArrayList<ScheduleMessageItem> list = new ArrayList<ScheduleMessageItem>();
        if (cursor.moveToFirst()){
            do {
                ScheduleMessageItem item = new ScheduleMessageItem();
                item.setId(cursor.getInt(0));
                item.setContent(cursor.getString(1));
                item.setNumberlist(cursor.getString(2));
                item.setTimemillis(Long.parseLong(cursor.getString(3)));
                //0 : 안보냄, 1: 보냄
                item.setIssend((cursor.getInt(4) == 0 )? false : true);
                list.add(item);
            } while (cursor.moveToNext());
        }

        return list;
    }
}
