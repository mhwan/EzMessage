package com.app.mhwan.easymessage.CustomView;

/**
 * Created by Mhwan on 2016. 5. 10..
 */

/**
 * photo, person id (저장된 사진을 얻기위함)
 * type (0: 내가 보냄, 1: 받음)
 * request_code (예약메시지 요청코드, 예약이 아닐경우 -1)
 * is_read (true(1) 읽음 - 보낸 메시지일 경우, 읽었을경우, false(0) 읽지않음 - 받은메시지가 처음 도착했을경우)
 * is_last 각 번호와 주고받은 마지막 메시지 (db에 새로 저장할경우 무조건 last)
 */
public class MessageItem {
    protected String ph_number, content, time;
    protected int type, request_code;
    protected boolean is_read, is_last_message;
    private int id, color_id = -1;

    public int getColor_id(){
        return color_id;
    }
    public void setColor_id(int color_id){
        this.color_id = color_id;
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean is_last_message() {
        return is_last_message;
    }

    public void setIs_last_message(boolean is_last_message) {
        this.is_last_message = is_last_message;
    }

    public boolean is_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public String getPh_number() {
        return ph_number;
    }

    public void setPh_number(String ph_number) {
        this.ph_number = ph_number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRequest_code() {
        return request_code;
    }

    public void setRequest_code(int request_code) {
        this.request_code = request_code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageItem item = (MessageItem) o;

        if (type != item.type) return false;
        if (request_code != item.request_code) return false;
        if (is_read != item.is_read) return false;
        if (is_last_message != item.is_last_message) return false;
        if (id != item.id) return false;
        if (color_id != item.color_id) return false;
        if (!ph_number.equals(item.ph_number)) return false;
        if (content != null ? !content.equals(item.content) : item.content != null) return false;
        return time.equals(item.time);

    }

    @Override
    public int hashCode() {
        int result = ph_number.hashCode();
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + time.hashCode();
        result = 31 * result + type;
        result = 31 * result + request_code;
        result = 31 * result + (is_read ? 1 : 0);
        result = 31 * result + (is_last_message ? 1 : 0);
        result = 31 * result + id;
        result = 31 * result + color_id;
        return result;
    }
}
