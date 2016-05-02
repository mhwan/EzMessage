package com.app.mhwan.easymessage.CustomView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.mhwan.easymessage.Activity.SendSMSActivity;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.Fragment.SMSFragment;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;

/**
 * Created by Mhwan on 2016. 3. 18..
 */
public class BottomSheetAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BottomSheet> bottomSheets;
    private Fragment fragment;

    public BottomSheetAdapter(Context context, ArrayList<BottomSheet> bottomSheets, Fragment fragment) {
        this.context = context;
        this.bottomSheets = bottomSheets;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return bottomSheets.size();
    }

    @Override
    public Object getItem(int position) {
        return bottomSheets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Viewholder viewholder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_bottom_sheet, parent, false);
            viewholder = new Viewholder();
            viewholder.menuTitle = (TextView) convertView.findViewById(R.id.menu_title);
            viewholder.menuImage = (ImageView) convertView.findViewById(R.id.menu_icon);
            viewholder.menuButton = (RelativeLayout) convertView.findViewById(R.id.bottom_sheet_root);
            convertView.setTag(viewholder);
        } else
            viewholder = (Viewholder) convertView.getTag();
        BottomSheet sheet = (BottomSheet) getItem(position);
        viewholder.menuImage.setImageResource(sheet.getBottomSheetMenuType().getResId());
        viewholder.menuTitle.setText(context.getString(sheet.getBottomSheetMenuType().getName()));
        viewholder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SendSMSActivity.class);
                intent.putExtra(AppUtility.BasicInfo.SEND_TYPE, position);
                intent.putExtra(AppUtility.BasicInfo.SEND_PHONE_NUMBER, -1);
                ((SMSFragment)fragment).slidetofab();
                ((Activity) context).startActivityForResult(intent, AppUtility.BasicInfo.SEND_REQUEST);
            }
        });
        return convertView;
    }

    class Viewholder {
        RelativeLayout menuButton;
        TextView menuTitle;
        ImageView menuImage;
    }
}
