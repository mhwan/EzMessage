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
public class MessageListItem extends MessageItem{
    private int count_no_read;
    private String name;

    public int getCount_no_read() {
        return count_no_read;
    }

    public void setCount_no_read(int count_no_read) {
        this.count_no_read = count_no_read;
    }

    public String getLast_mContent() {
        return content;
    }

    public void setLast_mContent(String last_mContent) {
        this.content = last_mContent;
    }

    public String getLast_mTime() {
        return time;
    }

    public void setLast_mTime(String last_mTime) {
        this.time = last_mTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
