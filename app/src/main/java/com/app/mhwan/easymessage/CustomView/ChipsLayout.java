package com.app.mhwan.easymessage.CustomView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 4. 9..
 */
public class ChipsLayout extends LinearLayout {
    public ChipsLayout(Context context) {
        super(context);
    }

    public ChipsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChipsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChipsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        TextView v = (TextView)findViewById(R.id.chips_name);
        v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        /*
        if (selected) {
            v.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.close_x, 0);
        } else {
            v.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }*/
    }

}
