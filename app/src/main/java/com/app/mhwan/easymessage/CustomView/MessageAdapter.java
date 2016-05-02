package com.app.mhwan.easymessage.CustomView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
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

    @Override
    public void onBindViewHolder(ItemViewholder holder, int position) {
        MessageItem item = messageItems.get(position);
        //시간은 어떻게 처리할것인가!!?!@
        //받은 메시지
        if (item.getmType() == 1){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT;
            holder.bg.setLayoutParams(params);
            holder.bg.setBackgroundResource(R.drawable.bg_receive_message);
            holder.content.setTextColor(context.getResources().getColor(R.color.colorReceiveMessage));
            //받은메시지는 프로필 부분이 보이도록
            holder.circle.setVisibility(View.VISIBLE);
            if (item.getmColor_id() < 0){
                holder.icon_circle.setVisibility(View.INVISIBLE);
                holder.image_circle.setVisibility(View.VISIBLE);
                holder.image_circle.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), item.getmPerson_id(), item.getmPhoto_id()));
            }
            else {
                holder.image_circle.setVisibility(View.INVISIBLE);
                holder.icon_circle.setVisibility(View.VISIBLE);
                holder.icon_circle.setCircleBackgroundColor(light_color_array[item.getmColor_id()]);
            }
        }
        //보낸 메시지
        else if (item.getmType() == 0){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.RIGHT;
            holder.bg.setLayoutParams(params);
            holder.bg.setBackgroundResource(R.drawable.bg_send_message);
            holder.content.setTextColor(context.getResources().getColor(R.color.colorLightprimary));
            holder.circle.setVisibility(View.GONE);
        }
        holder.content.setText(item.getmContent());
        holder.time.setText(item.getmTime());
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

    public static final class ItemViewholder extends RecyclerView.ViewHolder {
        FrameLayout circle;
        RandomProfileIcon icon_circle;
        CircleImageView image_circle;
        TextView content, time;
        RelativeLayout bg;

        public ItemViewholder(View itemView) {
            super(itemView);
            circle = (FrameLayout) itemView.findViewById(R.id.circle);
            icon_circle = (RandomProfileIcon) itemView.findViewById(R.id.ic_person);
            image_circle = (CircleImageView) itemView.findViewById(R.id.image_person);
            content = (TextView) itemView.findViewById(R.id.message_content);
            time = (TextView) itemView.findViewById(R.id.message_time);
            bg = (RelativeLayout) itemView.findViewById(R.id.message_bg);
        }
    }
}
