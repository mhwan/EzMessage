package com.app.mhwan.easymessage.CustomView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mhwan on 2016. 4. 15..
 */
public class SelectContactsAdapter extends BaseAdapter implements Filterable {
    private ArrayList<ContactItem> contactItems;
    private Context context;
    private int[] light_color_array;
    private ArrayList<ContactItem> origList;

    public SelectContactsAdapter(ArrayList<ContactItem> contactItems, Context context){
        this.contactItems = contactItems;
        this.context = context;
        light_color_array = context.getResources().getIntArray(R.array.user_color);
    }

    @Override
    public int getCount() {
        return contactItems.size();
    }

    @Override
    public ContactItem getItem(int position) {
        return contactItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Viewholder holder;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_select_contact, parent, false);
            holder = new Viewholder();
            holder.phNum_textview = (TextView) convertView.findViewById(R.id.select_phNumber);
            holder.userName_textview = (TextView) convertView.findViewById(R.id.select_name);
            holder.circle = (RandomProfileIcon) convertView.findViewById(R.id.select_ic_person);
            holder.user_imageview = (CircleImageView) convertView.findViewById(R.id.select_image_person);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.select_checkbox);

            convertView.setTag(holder);
        }
        else
            holder = (Viewholder)convertView.getTag();
        final ContactItem contactItem = contactItems.get(position);
        holder.phNum_textview.setText(contactItem.getUser_phNumber());
        holder.userName_textview.setText(contactItem.getUser_Name());
        if (contactItem.getColor_Id() < 0){
            holder.circle.setVisibility(View.INVISIBLE);
            holder.user_imageview.setVisibility(View.VISIBLE);
            holder.user_imageview.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), contactItem.getPerson_id(), contactItem.getPhoto_id()));
        }
        else {
            holder.circle.setVisibility(View.VISIBLE);
            holder.user_imageview.setVisibility(View.INVISIBLE);
            holder.circle.setCircleBackgroundColor(light_color_array[contactItem.getColor_Id()]);
        }

        //체크박스의 setchecked를 리스너 앞에 둘 경우 스크롤시 체크상태 초기화되는 현상.
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //getItem(position).setChecked(isChecked);
            }
        });
        holder.checkBox.setChecked(contactItem.getChecked());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults return_filter = new FilterResults();
                final ArrayList<ContactItem> results = new ArrayList<ContactItem>();
                if (origList == null)
                    origList = contactItems;
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
                contactItems = (ArrayList<ContactItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    private int getSelectedItemCount(){
        int num = 0;
        for (ContactItem contactItem : contactItems){
            if (contactItem.getChecked())
                num++;
        }
        return num;
    }

    public ArrayList<String> getCheckedItemIdList(){
        if (getSelectedItemCount() <= 0)
            return null;
        ArrayList<String> array = new ArrayList<String>();

        for (int i = 0; i< contactItems.size(); i++){
            if (contactItems.get(i).getChecked())
                array.add(String.valueOf(i));
        }

        return array;
    }
    class Viewholder {
        TextView phNum_textview, userName_textview;
        RandomProfileIcon circle;
        CircleImageView user_imageview;
        CheckBox checkBox;
    }
}
