package com.app.mhwan.easymessage.CustomView;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 3. 24..
 */

public class RandomProfileIcon extends RelativeLayout{
    private DrawCircle drawCircle;
    private ImageView icon;
    private int padding_ic_px;
    private int icon_id;
    private int background_color;
    private int boarder_width;
    private int boarder_color;
    private boolean isDrawStroke = false;
    private boolean isautopadding = false;

    public RandomProfileIcon(Context context) {
        super(context);
        initView(context, null);
    }

    public RandomProfileIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
        setTypedArray(context, attrs);
    }

    public RandomProfileIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
        setTypedArray(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RandomProfileIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
        setTypedArray(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs){
        drawCircle = new DrawCircle(context, attrs);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(drawCircle, params);
        icon = new ImageView(context, attrs);
        params.addRule(CENTER_IN_PARENT, TRUE);
        addView(icon, params);
    }

    private void setTypedArray(Context context, AttributeSet attrs){
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.RandomProfileIcon, 0, 0);
        icon_id = attr.getResourceId(R.styleable.RandomProfileIcon_icon_src, 0);
        isautopadding = attr.getBoolean(R.styleable.RandomProfileIcon_auto_padding, false);
        background_color = attr.getColor(R.styleable.RandomProfileIcon_bg_color, context.getResources().getColor(android.R.color.darker_gray));
        padding_ic_px = (int) (attr.getDimension(R.styleable.RandomProfileIcon_icon_padding, 0)/context.getResources().getDisplayMetrics().density);
        boarder_width = (int) (attr.getDimension(R.styleable.RandomProfileIcon_border_width, 0)/context.getResources().getDisplayMetrics().density);
        boarder_color = attr.getColor(R.styleable.RandomProfileIcon_border_color, context.getResources().getColor(android.R.color.transparent));
        setIconResource(icon_id);
        setBoarder_width(boarder_width);
        setBoarder_color(boarder_color);
        setCircleBackgroundColor(background_color);
        DLog.d(padding_ic_px+"");
        setIconPadding(padding_ic_px);
        attr.recycle();
    }

    public void setBoarder_width(int dp){
        boarder_width = AppUtility.getAppinstance().dpToPx(dp);
        if (boarder_width > 0)
            isDrawStroke = true;
        drawCircle.invalidate();
    }

    public void setBoarder_color(int color){
        boarder_color = color;
        drawCircle.invalidate();
    }
    public void setIconResource(int resource){
        icon_id = resource;
        icon.setImageResource(icon_id);
    }

    public void setCircleBackgroundColor(int color){
        background_color = color;
        drawCircle.invalidate();
    }

    public void setIconPadding(int dp){
        if (!isautopadding) {
            padding_ic_px = AppUtility.getAppinstance().dpToPx(dp);
            icon.setPadding(padding_ic_px, padding_ic_px, padding_ic_px, padding_ic_px);
        }
        else {
            //0 : width, 1: height
            int [] sizes = AppUtility.getAppinstance().getDisplaySize();
            int auto_dp;
            if (sizes[0] < 600)
                auto_dp = 14;
            else if (sizes[0] < 1200)
                auto_dp = 13;
            else
                auto_dp = 12;


            DLog.d("auto dp ::: "+auto_dp);
            int auto_px = AppUtility.getAppinstance().dpToPx((dp == 10)? auto_dp-1 : auto_dp);
            icon.setPadding(auto_px, auto_px, auto_px, auto_px);
        }
    }

    class DrawCircle extends View {
        private Paint circlePaint, borderPaint;
        private int w, h;

        public DrawCircle(Context context) {
            super(context);
            init(context, null);
            initBorder(context, null);
        }

        public DrawCircle(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
            initBorder(context, attrs);
        }

        public DrawCircle(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context, attrs);
            initBorder(context, attrs);
        }

        private void init(Context context, AttributeSet attrs){
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(background_color);
            circlePaint.setStyle(Paint.Style.FILL);
        }

        private void initBorder(Context context, AttributeSet attrs){
            borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            if (isDrawStroke) {
                borderPaint.setStrokeWidth(boarder_width);
                borderPaint.setColor(boarder_color);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            this.w = w;
            this.h = h;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            circlePaint.setColor(background_color);
            int radius = Math.min(w,h)/2;
            canvas.drawCircle(w/2, h/2, radius, circlePaint);
            if (isDrawStroke) {
                borderPaint.setColor(boarder_color);
                borderPaint.setStrokeWidth(boarder_width);
                canvas.drawCircle(w/2, h/2, radius-(boarder_width/2), borderPaint);
            }
        }
    }
}
