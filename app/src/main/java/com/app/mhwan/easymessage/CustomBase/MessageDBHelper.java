package com.app.mhwan.easymessage.CustomBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mhwan on 2016. 5. 3..
 */
public class MessageDBHelper extends SQLiteOpenHelper {
    public static String Message_List_TABLE_NAME = "Message_list";

    public MessageDBHelper(Context context){
        super(context, ScheduleMessageDBHelper.DB_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
