package com.app.mhwan.easymessage.CustomView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mhwan on 2016. 4. 29..
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ItemViewholder> {
    private Context context;
    private ArrayList<MessageItem> messageItems;
    private int[] light_color_array;

    public MessageAdapter(Context context, ArrayList<MessageItem> messageItems){
        this.context = context;
        this.messageItems = messageItems;
        light_color_array = context.getResources().getIntArray(R.array.user_color);
    }

    @Override
    public ItemViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ItemViewholder(item);
    }

    public MessageItem getFirstMessageItem(){
        return messageItems.get(0);
    }
    @Override
    public void onBindViewHolder(ItemViewholder holder, int position) {
        MessageItem item = messageItems.get(position);
        DLog.d(item.getPh_number()+" : "+item.is_read()+", "+item.is_last_message());
        //시간은 어떻게 처리할것인가!!?!@
        //받은 메시지
        if (item.getType() == 1){
            holder.r_bg.setVisibility(View.VISIBLE);
            holder.sendwindow.setVisibility(View.GONE);
            holder.r_content.setTextColor(context.getResources().getColor(R.color.colorReceiveMessage));
            holder.r_content.setText(item.getContent());
            if (AppUtility.getAppinstance().isToday(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT))
                holder.r_time.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_TIME_FORMAT));
            else
                holder.r_time.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_DATETIME_FORMAT));
            //받은메시지는 프로필 부분이 보이도록
            holder.circle.setVisibility(View.VISIBLE);
            if (AppUtility.getAppinstance().getColorId(item.getPh_number()) < 0){
                holder.icon_circle.setVisibility(View.INVISIBLE);
                holder.image_circle.setVisibility(View.VISIBLE);
                long[] ids = AppUtility.getAppinstance().getPhotoPersonId(item.getPh_number());
                holder.image_circle.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), ids[0], ids[1]));
            }
            else {
                holder.image_circle.setVisibility(View.INVISIBLE);
                holder.icon_circle.setVisibility(View.VISIBLE);
                int color = item.getColor_id();
                DLog.d(item.getPh_number()+" : "+item.getColor_id());
                holder.icon_circle.setCircleBackgroundColor(light_color_array[(color >= 0 && !AppUtility.getAppinstance().getSaved(item.getPh_number()))? color : AppUtility.getAppinstance().getColorId(item.getPh_number())]);
            }
        }
        //보낸 메시지
        else if (item.getType() == 0){
            holder.sendwindow.setVisibility(View.VISIBLE);
            holder.r_bg.setVisibility(View.GONE);
            holder.s_content.setTextColor(context.getResources().getColor(R.color.colorLightprimary));
            holder.image_schedule.setVisibility((item.getRequest_code() < 0)? View.GONE : View.VISIBLE);
            holder.s_content.setText(item.getContent());
            if (AppUtility.getAppinstance().isToday(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT))
                holder.s_time.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_TIME_FORMAT));
            else
                holder.s_time.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_DATETIME_FORMAT));
        }

    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    public void removeMessage(int position){
        messageItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, messageItems.size());
    }
    public void removeAllMessage(){
        int size = messageItems.size();
        messageItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void updateAllMessage(ArrayList<MessageItem> list){
        messageItems.clear();
        messageItems.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<MessageItem> getMessageItems(){
        return messageItems;
    }
    public static final class ItemViewholder extends RecyclerView.ViewHolder {
        FrameLayout circle;
        RandomProfileIcon icon_circle;
        CircleImageView image_circle;
        TextView r_content, r_time, s_content, s_time;
        RelativeLayout sendwindow, r_bg;
        ImageView image_schedule;
        public ItemViewholder(View itemView) {
            super(itemView);
            circle = (FrameLayout) itemView.findViewById(R.id.r_circle);
            icon_circle = (RandomProfileIcon) itemView.findViewById(R.id.r_ic_person);
            image_circle = (CircleImageView) itemView.findViewById(R.id.r_image_person);
            r_bg = (RelativeLayout) itemView.findViewById(R.id.receive_message_bg);
            s_content = (TextView) itemView.findViewById(R.id.message_content);
            s_time = (TextView) itemView.findViewById(R.id.message_time);
            sendwindow = (RelativeLayout) itemView.findViewById(R.id.send_message_window);
            r_content = (TextView) itemView.findViewById(R.id.r_message_content);
            r_time = (TextView) itemView.findViewById(R.id.r_message_time);
            image_schedule = (ImageView) itemView.findViewById(R.id.image_schedule);
        }
    }
}
