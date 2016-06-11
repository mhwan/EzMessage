package com.app.mhwan.easymessage.CustomView;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 6. 7..
 */
public class CustomCheckBoxPreference extends CheckBoxPreference {
    private Context context;
    public CustomCheckBoxPreference(Context context) {
        super(context);
        this.context = context;
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (view instanceof ViewGroup)
            setLayout((ViewGroup) view);
    }

    private void setLayout(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();

        for (int i = 0; i < count; ++i) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) childView;
                int[][] states = new int[][] {
                        new int[] {android.R.attr.state_enabled }, //default
                        new int[] { -android.R.attr.state_checked},
                };
                int[] colors = new int[]{
                        ContextCompat.getColor(context, R.color.colorLightprimary),
                        ContextCompat.getColor(context, R.color.light_grey),
                };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkBox.setButtonTintList(new ColorStateList(states, colors));
                } else
                    checkBox.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                return;
            } else if (childView instanceof ViewGroup)
                setLayout((ViewGroup) childView);
        }
    }
}
