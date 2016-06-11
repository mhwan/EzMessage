package com.app.mhwan.easymessage.CustomBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.CustomView.MessageListItem;

import java.util.ArrayList;

/**
 * Created by Mhwan on 2016. 5. 3..
 */
public class MessageDBHelper extends SQLiteOpenHelper {
    public static String Message_List_TABLE_NAME = "Message_list";
    public static String MESSAGE_DB_NAME = "EZ_MESSAGE_DB";
    public static String MESSAGE_KEY_ID = "_id";
    public static String MESSAGE_KEY_NUMBER = "mPhone_number";
    public static String MESSAGE_KEY_CONTENT = "mContent";
    public static String MESSAGE_KEY_TIME = "mTime";
    public static String MESSAGE_KEY_TYPE = "Message_Type";
    public static String MESSAGE_KEY_ISREAD = "Is_Read";
    public static String MESSAGE_KEY_ISLAST = "Is_Last_Message";
    public static String MESSAGE_KEY_ISSCHEDULE = "Is_Schedule";
    public static String MESSAGE_KEY_COLORID = "Color_ID";

    public MessageDBHelper(Context context){
        super(context, MESSAGE_DB_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        DLog.i("db생성!!");
        String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS "+Message_List_TABLE_NAME+" ("+
                MESSAGE_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MESSAGE_KEY_NUMBER+" TEXT NOT NULL, "+MESSAGE_KEY_CONTENT+" TEXT NOT NULL, "+MESSAGE_KEY_TIME+" TEXT NOT NULL, "+
                MESSAGE_KEY_TYPE+" INTEGER NOT NULL, "+MESSAGE_KEY_ISREAD+" INTEGER NOT NULL, "+MESSAGE_KEY_ISLAST+" INTEGER NOT NULL, "+MESSAGE_KEY_ISSCHEDULE+" INTEGER, "+MESSAGE_KEY_COLORID+" INTEGER)";
        db.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+Message_List_TABLE_NAME);
        onCreate(db);
    }

    /**
     * 메시지의 번호와 같은 is_last값을 모두 0으로 바꾸고 새로운 메시지를 추가한다.
     */
    public void addMessage(MessageItem item){
        changeMessageisLast(item.getPh_number());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MESSAGE_KEY_NUMBER, item.getPh_number());
        values.put(MESSAGE_KEY_CONTENT, item.getContent());
        values.put(MESSAGE_KEY_TIME, item.getTime());
        values.put(MESSAGE_KEY_TYPE, item.getType());
        values.put(MESSAGE_KEY_ISREAD, (item.is_read())? 1:0);
        values.put(MESSAGE_KEY_ISLAST, (item.is_last_message())? 1:0);
        values.put(MESSAGE_KEY_ISSCHEDULE, item.getRequest_code());
        values.put(MESSAGE_KEY_COLORID, item.getColor_id());
        db.insert(Message_List_TABLE_NAME, null, values);
        db.close();
    }

    private void changeMessageisLast(String number){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE "+Message_List_TABLE_NAME+" SET "+MESSAGE_KEY_ISLAST+" = \'0\' WHERE "+MESSAGE_KEY_NUMBER+" = \""+number+"\"";
        db.execSQL(query);
        db.close();
    }

    /**
     * 특정 메시지 삭제
     */
    public void removeMessage(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM " + Message_List_TABLE_NAME + " WHERE " + MESSAGE_KEY_ID + "= \"" + id+"\"";
        db.execSQL(query);
    }

    /**
     * 해당 번호의 모든 메시지 삭제(리스트에서 메시지 삭제)
     */
    public void removeAllMessage(String number){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM " + Message_List_TABLE_NAME + " WHERE " + MESSAGE_KEY_NUMBER + "= \""+number+"\"";
        db.execSQL(query);
    }

    /**
     * 메시지 전체 삭제
     */
    public void removeAll(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM "+Message_List_TABLE_NAME;
        db.execSQL(query);
    }
    /**
     *
     * @return 마지막 메시지 리스트를 시간순 (내림차순)로 반환한다.
     */
    public ArrayList<MessageListItem> getAllLastMessageList(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ Message_List_TABLE_NAME +" WHERE "+MESSAGE_KEY_ISLAST +" = \'1\' ORDER BY datetime("+MESSAGE_KEY_TIME+") DESC";
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<MessageListItem> list = new ArrayList<MessageListItem>();
        if (cursor.moveToFirst()){
            do {
                MessageListItem item = new MessageListItem();
                item.setId(cursor.getInt(0));
                item.setPh_number(cursor.getString(1));
                item.setLast_mContent(cursor.getString(2));
                item.setLast_mTime(cursor.getString(3));
                item.setType(cursor.getInt(4));
                item.setIs_read((cursor.getInt(5)==0)? false : true);
                item.setIs_last_message((cursor.getInt(6)==0)? false : true);
                item.setRequest_code(cursor.getInt(7));
                item.setName(AppUtility.getAppinstance().getUserName(cursor.getString(1)));
                item.setColor_id(cursor.getInt(8));

                list.add(item);
            }while (cursor.moveToNext());
        }

        //읽지 않은 카운트를 세어 추가해주고 리턴.
        return countNoReadMessage(list);
    }

    private ArrayList<MessageListItem> countNoReadMessage(ArrayList<MessageListItem> list){
        SQLiteDatabase db = this.getReadableDatabase();
        String queryformat = "SELECT COUNT(*) FROM "+Message_List_TABLE_NAME+" WHERE "+MESSAGE_KEY_NUMBER+" = \"%s\" AND "+MESSAGE_KEY_ISREAD+" = \'0\'";
        for (int i=0; i < list.size(); i++){
            String phnumber = list.get(i).getPh_number();
            int count = (int) DatabaseUtils.longForQuery(db, String.format(queryformat, phnumber), null);
            DLog.d("query!! "+String.format(queryformat, phnumber));
            DLog.d(phnumber+" : "+count);
            list.get(i).setCount_no_read(count);
        }

        return list;
    }

    public int getNoReadMessageNumber(){
        ArrayList<MessageListItem> listItems = getAllLastMessageList();
        int count = 0;
        for (MessageListItem item : listItems){
            int n = 0;
            if ((n = item.getCount_no_read()) > 0)
                count+=n;
        }

        return count;
    }

    /**
     * 다른 사람한테 여러 메시지 왔다면 true, 아니면 false반환
     * @return
     */
    public boolean isDifferentDestination(){
        ArrayList<MessageListItem> listItems = getAllLastMessageList();
        boolean status = false;
        boolean flag = false;

        for (MessageListItem item : listItems){
            //처음에는 플래그가 트루가 될테고 그 이후부터 0개 이상이라는 거는 여러사람한테 new가 왔다는것
            if (item.getCount_no_read() > 0 && flag == false)
                flag = true;
            else if (item.getCount_no_read() > 0 && flag == true)
                status = true;
        }

        return status;
    }

    /**
     *
     * @param number 핸드폰 번호를 전달하여
     * @return 그 핸드폰 번호로 등록된 모든 메시지를 시간순으로(오름차순) 갖고온다
     */
    public ArrayList<MessageItem> getAllMessageList(String number){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+Message_List_TABLE_NAME+" WHERE "+MESSAGE_KEY_NUMBER+" = \""+number+"\" ORDER BY datetime("+MESSAGE_KEY_TIME+") ASC";
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<MessageItem> list = new ArrayList<MessageItem>();
        if (cursor.moveToFirst()){
            do {
                MessageItem item = new MessageItem();
                item.setId(cursor.getInt(0));
                item.setPh_number(cursor.getString(1));
                item.setContent(cursor.getString(2));
                item.setTime(cursor.getString(3));
                item.setType(cursor.getInt(4));
                item.setIs_read((cursor.getInt(5)==0)? false : true);
                item.setIs_last_message((cursor.getInt(6)==0)? false : true);
                item.setRequest_code(cursor.getInt(7));
                item.setColor_id(cursor.getInt(8));
                list.add(item);
            }while (cursor.moveToNext());
        }

        return list;
    }

    /**
     * 핸드폰 번호로 메시지 리스트에 저장된 컬러값을 반환한다.
     * 저장되지 않았다면 음수반환
     * @param number
     * @return
     */
    public int getSavedColorId(String number){
        ArrayList<MessageListItem> listItems = getAllLastMessageList();
        int id = -1;
        for (MessageListItem item : listItems){
            if (item.getPh_number().equals(number)){
                id = item.getColor_id();
                break;
            }
        }

        return id;
    }

    /**
     * 해당 메시지 화면에 들어왔을때
     *
     * 해당 휴대폰 번호로 되어있는 메시지를 모두 읽음 상태로 바꾼다.
     */
    public void changeMessageReadStatus(String number){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE "+Message_List_TABLE_NAME+" SET "+MESSAGE_KEY_ISREAD+" = \'1\' WHERE "+MESSAGE_KEY_NUMBER+" = \""+number+"\"";

        db.execSQL(query);
    }

    /**
     * 해당 요청코드로 되어있는 db의 요청코드를 모두 -1로 바꾼다.
     */
    public void changeMessageScheduleStatus(int request){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE "+Message_List_TABLE_NAME+" SET "+MESSAGE_KEY_ISSCHEDULE+" = \'-1\' WHERE "+MESSAGE_KEY_ISSCHEDULE+" = \""+request+"\"";

        db.execSQL(query);
    }
}
