package com.app.mhwan.easymessage.CustomBase;

import java.util.ArrayList;

/**
 * Created by Mhwan on 2016. 4. 26..
 */
public class ScheduleMessageItem {
    private String content, numberlist;
    private long timemillis;
    private boolean issend;
    private int id;

    public ScheduleMessageItem(){
    }
    public ScheduleMessageItem(String content, String numberlist, boolean issend, long settimemillis){
        this.content = content;
        this.numberlist = numberlist;
        this.issend = issend;
        this.timemillis = settimemillis;
    }
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
    public String getContent(){
        return content;
    }
    public long gettimemillis(){
        return timemillis;
    }
    public boolean getIssend(){
        return issend;
    }
    public ArrayList<String> getNumberArraylist(){
        String[] strings = numberlist.split(MessageManager.NUM_SEPERATOR);
        ArrayList<String> list = new ArrayList<String>();
        for (String a : strings)
            list.add(a);

        return list;
    }

    public String getnumberString(){
        return numberlist;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setNumberlist(String numberlist){
        this.numberlist = numberlist;
    }
    public void setTimemillis(long time){
        timemillis = time;
    }
    public void setIssend(boolean issend){
        this.issend = issend;
    }
}
