package com.app.mhwan.easymessage.CustomView;

/**
 * Created by Mhwan on 2016. 4. 29..
 */

/**
 * 메시지
 * string 휴대폰번호(상대방), 내용, 시간
 * long person_id, photo_id;
 * bool isread 읽었는지에 대한 상태 (받은메시지의 첫상태는 읽지 않음, 들어왔을때 읽음 처리됨, 보낸 메시지는 무조건 읽음 처리)
 * int type 0 : 내가 보낸 메시지, 1: 받은메시지
 */
public class MessageItem {
    private String nPhNum, mContent, mTime;
    private long mPerson_id, mPhoto_id;
    private int mType;
    private int mColor_id;
    private boolean isread;

    public MessageItem(){}

    public boolean isread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public long getmPerson_id() {
        return mPerson_id;
    }

    public void setmPerson_id(long mPerson_id) {
        this.mPerson_id = mPerson_id;
    }

    public long getmPhoto_id() {
        return mPhoto_id;
    }

    public void setmPhoto_id(long mPhoto_id) {
        this.mPhoto_id = mPhoto_id;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public String getnPhNum() {
        return nPhNum;
    }

    public int getmColor_id() {
        return mColor_id;
    }

    public void setmColor_id(int mColor_id) {
        this.mColor_id = mColor_id;
    }

    public void setnPhNum(String nPhNum) {
        this.nPhNum = nPhNum;
    }
}
