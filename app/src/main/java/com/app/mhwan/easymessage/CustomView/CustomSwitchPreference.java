package com.app.mhwan.easymessage.CustomView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 6. 7..
 */
public class CustomSwitchPreference extends SwitchPreference {
    private Context context;
    public CustomSwitchPreference(Context context) {
        super(context);
        this.context = context;
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (view instanceof ViewGroup)
            setLayout((ViewGroup) view);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setLayout(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for(int n = 0; n < count; ++n) {
            View childView = viewGroup.getChildAt(n);
            if(childView instanceof Switch) {
                final Switch switchView = (Switch) childView;
                switchView.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorLightprimary), PorterDuff.Mode.MULTIPLY);
                switchView.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorLightprimary), PorterDuff.Mode.MULTIPLY);
                return;
            }
            else if (childView instanceof ViewGroup)
                setLayout((ViewGroup) childView);
        }
    }
}
