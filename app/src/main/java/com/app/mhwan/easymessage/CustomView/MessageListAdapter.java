package com.app.mhwan.easymessage.CustomView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mhwan.easymessage.Activity.MessageActivity;
import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.MessageDBHelper;
import com.app.mhwan.easymessage.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mhwan on 2016. 4. 28..
 */
public class MessageListAdapter extends RecyclerSwipeAdapter<MessageListAdapter.ListViewHolder> {
    private Context context;
    private ArrayList<MessageListItem> mListItems;
    private int[] light_color_array;
    private MessageDBHelper messageDBHelper;

    public MessageListAdapter(Context context, ArrayList<MessageListItem> items, MessageDBHelper messageDBHelper){
        this.context = context;
        this.mListItems = items;
        this.messageDBHelper = messageDBHelper;
        light_color_array = context.getResources().getIntArray(R.array.user_color);
    }
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListViewHolder viewHolder, final int position) {
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        viewHolder.swipeLayout.setClickToClose(true);
        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                layout.findViewById(R.id.trash_can).startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_swipe_start));
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
        viewHolder.swipe_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //지우기 task작업을 모두 마친 결과(issuccess) 인터페이스 구현
                final RemoveTask task = new RemoveTask(new RemoveCallback() {
                    @Override
                    public void finishedRemove(boolean issuccess) {
                        if (issuccess){
                            messageDBHelper.removeAllMessage(mListItems.get(position).getPh_number());
                            mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                            mListItems.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, mListItems.size());
                            mItemManger.closeAllItems();
                            Toast.makeText(context, context.getString(R.string.message_delete_successfully), Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(context, context.getString(R.string.message_delete_failed), Toast.LENGTH_SHORT).show();
                    }
                });
                task.execute();
            }
        });
        MessageListItem item = mListItems.get(position);
        String name = item.getName();
        DLog.d(name+" : "+item.getCount_no_read());
        viewHolder.mName.setText((name != null)? name : AppUtility.getAppinstance().changeNumberFormat(item.getPh_number()));
        viewHolder.mContent.setText(item.getLast_mContent());
        if (AppUtility.getAppinstance().isToday(item.getLast_mTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT))
            viewHolder.mDate.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getLast_mTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_TIME_FORMAT));
        else
            viewHolder.mDate.setText(AppUtility.getAppinstance().changeDateTimeFormat(item.getLast_mTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT, AppUtility.BasicInfo.GENERAL_DATETIME_FORMAT));

        if (item.getCount_no_read()>0){
            viewHolder.image_schedule.setVisibility(View.GONE);
            viewHolder.mCount.setVisibility(View.VISIBLE);
            viewHolder.mCount.setText((item.getCount_no_read()>99)? "99+" : String.valueOf(item.getCount_no_read()));
        }
        else {
            viewHolder.mCount.setVisibility(View.GONE);
            viewHolder.image_schedule.setVisibility((item.getRequest_code() < 0)? View.GONE : View.VISIBLE);
        }
        if (AppUtility.getAppinstance().getColorId(item.getPh_number()) < 0){
            viewHolder.circle.setVisibility(View.INVISIBLE);
            viewHolder.user_imageview.setVisibility(View.VISIBLE);
            long[] ids = AppUtility.getAppinstance().getPhotoPersonId(item.getPh_number());
            viewHolder.user_imageview.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), ids[0], ids[1]));
        }
        else {
            viewHolder.circle.setVisibility(View.VISIBLE);
            viewHolder.user_imageview.setVisibility(View.INVISIBLE);
            int color = item.getColor_id();
            DLog.d(item.getPh_number()+" : "+item.getColor_id());
            viewHolder.circle.setCircleBackgroundColor(light_color_array[(color >= 0 && !AppUtility.getAppinstance().getSaved(item.getPh_number()))? color : AppUtility.getAppinstance().getColorId(item.getPh_number())]);
        }
        //리사이클러뷰에 리스너를 달경우 스와이프상태일때 포커스를 가져가버림
        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra(AppUtility.BasicInfo.U_NAME, mListItems.get(position).getName());
                intent.putExtra(AppUtility.BasicInfo.U_NUMBER, mListItems.get(position).getPh_number());
                ((Activity) context).startActivityForResult(intent, AppUtility.BasicInfo.MESSAGE_REQUEST);
            }
        });
        viewHolder.parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewHolder.swipeLayout.open(true);
                return false;
            }
        });
        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public void updateList(ArrayList<MessageListItem> list){
        mListItems.clear();
        mListItems.addAll(list);
        notifyDataSetChanged();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        SwipeLayout swipeLayout;
        TextView mDate, mName, mContent, mCount;
        Button swipe_delete;
        RandomProfileIcon circle;
        CircleImageView user_imageview;
        RelativeLayout parent;
        ImageView image_schedule;

        public ListViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            mName = (TextView) itemView.findViewById(R.id.message_list_name);
            mContent = (TextView) itemView.findViewById(R.id.message_list_content);
            mCount = (TextView) itemView.findViewById(R.id.message_list_new);
            mDate = (TextView) itemView.findViewById(R.id.message_list_date);
            swipe_delete = (Button) itemView.findViewById(R.id.delete);
            circle = (RandomProfileIcon) itemView.findViewById(R.id.ic_person);
            user_imageview = (CircleImageView) itemView.findViewById(R.id.image_person);
            parent = (RelativeLayout) itemView.findViewById(R.id.parent);
            image_schedule = (ImageView) itemView.findViewById(R.id.image_schedule);
        }
    }

    private class RemoveTask extends AsyncTask<Void, Void, Boolean>{
        private ProgressDialog progressDialog = new ProgressDialog(context);
        private RemoveCallback callback;
        private boolean isSuccessDelete = false;

        public RemoveTask(RemoveCallback callback){
            this.callback = callback;
        }
        @Override
        protected void onPreExecute() {
            DLog.d("pre!@@");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getString(R.string.deleting_messages));
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    ProgressBar v = (ProgressBar)progressDialog.findViewById(android.R.id.progress);
                    v.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorLightprimary),
                            android.graphics.PorterDuff.Mode.MULTIPLY);
                }
            });
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    cancelTask();
                }
            });
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            isSuccessDelete = false;
            DLog.d("CANCEL!@!@");
            super.onCancelled(aBoolean);
            callback.finishedRemove(isSuccessDelete);
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            progressDialog.dismiss();
            isSuccessDelete = !this.isCancelled();
            callback.finishedRemove(isSuccessDelete);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DLog.d("DOING!!@!!@!@!");
            return isSuccessDelete;
        }
        private void cancelTask(){
            this.cancel(true);
        }
    }

    public int getAllNewCount(){
        int count = 0;
        for (MessageListItem item : mListItems)
            count += item.getCount_no_read();

        return count;
    }
    private interface RemoveCallback{
       void finishedRemove(boolean issuccess);
    }
}
