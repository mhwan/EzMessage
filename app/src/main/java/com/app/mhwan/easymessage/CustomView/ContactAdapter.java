package com.app.mhwan.easymessage.CustomView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.app.mhwan.easymessage.Activity.SendSMSActivity;
import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mhwan on 2016. 3. 23..
 */
public class ContactAdapter extends BaseSwipeAdapter implements Filterable {
    private ArrayList<ContactItem> contactItemList;
    private Context context;
    private int[] light_color_array;
    private View root_view;
    private ArrayList<ContactItem> origList;

    public ContactAdapter(ArrayList<ContactItem> list, Context context, View view) {
        this.contactItemList = list;
        this.context = context;
        light_color_array = context.getResources().getIntArray(R.array.user_color);
        root_view = view;
    }

    @Override
    public int getCount() {
        return contactItemList.size();
    }

    @Override
    public ContactItem getItem(int position) {
        return contactItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact_list, null);
        final SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.setRightSwipeEnabled(false);
        swipeLayout.setClickToClose(true);

        return view;
    }

    private AlertDialog createSelectDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.message_type));
        String[] strings = context.getResources().getStringArray(R.array.message_type_array);
        builder.setItems(context.getResources().getStringArray(R.array.message_type_array), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, SendSMSActivity.class);
                intent.putExtra(AppUtility.BasicInfo.SEND_TYPE, which);
                intent.putExtra(AppUtility.BasicInfo.SEND_PHONE_NUMBER, position);
                ((Activity) context).startActivityForResult(intent, AppUtility.BasicInfo.SEND_REQUEST);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void fillValues(final int position, final View convertView) {
        Viewholder viewholder = new Viewholder();
        viewholder.circle = (RandomProfileIcon) convertView.findViewById(R.id.ic_person);
        viewholder.phNum_textview = (TextView) convertView.findViewById(R.id.contact_list_phNumber);
        viewholder.userName_textview = (TextView) convertView.findViewById(R.id.contact_list_name);
        viewholder.user_imageview = (CircleImageView) convertView.findViewById(R.id.image_person);
        ContactItem contactItem = contactItemList.get(position);
        viewholder.phNum_textview.setText(contactItem.getUser_phNumber());
        viewholder.userName_textview.setText(contactItem.getUser_Name());
        if (contactItem.getColor_Id() < 0){
            viewholder.circle.setVisibility(View.INVISIBLE);
            viewholder.user_imageview.setVisibility(View.VISIBLE);
            viewholder.user_imageview.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), contactItem.getPerson_id(), contactItem.getPhoto_id()));
        }
        else {
            viewholder.circle.setVisibility(View.VISIBLE);
            viewholder.user_imageview.setVisibility(View.INVISIBLE);
            viewholder.circle.setCircleBackgroundColor(light_color_array[contactItem.getColor_Id()]);
        }
        convertView.findViewById(R.id.swipe_message).setOnTouchListener(touchListener);
        convertView.findViewById(R.id.swipe_call).setOnTouchListener(touchListener);
        convertView.findViewById(R.id.swipe_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new RequestPermission(context, 1).isPermission(root_view))
                    context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + getItem(position).getUser_phNumber())));
            }
        });
        convertView.findViewById(R.id.swipe_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSelectDialog(position).show();
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults return_filter = new FilterResults();
                final ArrayList<ContactItem> results = new ArrayList<ContactItem>();
                if (origList == null)
                    origList = contactItemList;
                if (constraint != null){
                    if (origList != null && origList.size()>0){
                        for (final ContactItem c : origList){
                            if (c.getUser_Name().toLowerCase().contains(constraint.toString()))
                                results.add(c);
                            else if (c.getPhNumberChanged().contains(constraint.toString()))
                                results.add(c);
                            else if (c.getUser_phNumber().contains(constraint.toString()))
                                results.add(c);
                        }
                    }
                    return_filter.values = results;
                }
                return return_filter;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactItemList = (ArrayList<ContactItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getBackground().setColorFilter(context.getResources().getColor(R.color.background_default_touch_color), PorterDuff.Mode.SRC_OVER);
                v.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() ==MotionEvent.ACTION_CANCEL) {
                v.getBackground().clearColorFilter();
                v.invalidate();
            }
            return false;
        }
    };

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    class Viewholder {
        TextView phNum_textview, userName_textview;
        RandomProfileIcon circle;
        CircleImageView user_imageview;
    }
}