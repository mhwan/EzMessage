package com.app.mhwan.easymessage.CustomBase;

/**
 * Created by Mhwan on 2016. 5. 4..
 */

/**
 * 리시버에서 받은 메시지를 화면에 변화를 주기위한 리스너
 *
 * isreceivemessage : 새로 받은 메시지인가?
 */
public interface NewMessageListener {
    void updateNewMessage(boolean isreceivemessage, String phnumber);
}
