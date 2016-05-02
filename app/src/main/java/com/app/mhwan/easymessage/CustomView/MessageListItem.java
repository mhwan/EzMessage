package com.app.mhwan.easymessage.CustomView;

/**
 * Created by Mhwan on 2016. 4. 20..
 */

/**
 * 받은 메시지 리스트 아이템
 * string : 상대방 핸드폰 번호, 상대방 이름, 마지막 메시지 내용, 마지막 메시지 시간
 * long : photo_id, person_id
 * int : color_value(none : -1), 안읽은 메시지 갯수
 */
public class MessageListItem {
    private String ph_num, name, last_mContent, last_mTime;
    private long photo_id, person_id;
    private int color_value, count_no_read;

    public MessageListItem(){}

    public int getColor_value() {
        return color_value;
    }

    public void setColor_value(int color_value) {
        this.color_value = color_value;
    }

    public int getCount_no_read() {
        return count_no_read;
    }

    public void setCount_no_read(int count_no_read) {
        this.count_no_read = count_no_read;
    }

    public String getLast_mContent() {
        return last_mContent;
    }

    public void setLast_mContent(String last_mContent) {
        this.last_mContent = last_mContent;
    }

    public String getLast_mTime() {
        return last_mTime;
    }

    public void setLast_mTime(String last_mTime) {
        this.last_mTime = last_mTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPerson_id() {
        return person_id;
    }

    public void setPerson_id(long person_id) {
        this.person_id = person_id;
    }

    public String getPh_num() {
        return ph_num;
    }

    public void setPh_num(String ph_num) {
        this.ph_num = ph_num;
    }

    public long getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(long photo_id) {
        this.photo_id = photo_id;
    }
}
