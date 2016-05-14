package com.app.mhwan.easymessage.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.R;

public class Splash extends Activity {
    private ImageView bg;
    private final Handler handler = new Handler();
    final Runnable startMainAcitivity = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        hideEverything();
        bg = (ImageView)findViewById(R.id.bg_splash);
        //애니메이션이 불러와지지 않아서 잠시 없애놓음
        //bg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.ani_fade_in));
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        AnimationSet set = new AnimationSet(true);
        Animation fadeOut = FadeOut(800);
        fadeOut.setStartOffset(0);
        set.addAnimation(fadeOut);
        Animation fadeIn = FadeIn(800);
        fadeIn.setStartOffset(900);
        set.addAnimation(fadeIn);
        bg.startAnimation(set);*/
        if (new RequestPermission(this, 2).isPermission(findViewById(R.id.root_splash)) && new RequestPermission(this, 3).isPermission(findViewById(R.id.root_splash)))
            handler.postDelayed(startMainAcitivity, 1100);
    }

    private void hideEverything(){
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case AppUtility.BasicInfo.REQUEST_READ_CONTACT :
            case AppUtility.BasicInfo.REQUEST_PHONE_STATE :
                //권한을 승인한경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    return;
                    //권한을 승인하지 않은경우
                else
                    finish();
                return;
        }
    }
    private Animation FadeIn(int t) {
        Animation fade;
        fade = new AlphaAnimation(0.1f,1.0f);
        fade.setDuration(t);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("anim", "is start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("anim", "is end");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fade.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_interpolator));
        //fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
    private Animation FadeOut(int t) {
        Animation fade;
        fade = new AlphaAnimation(1.0f,0.0f);
        fade.setDuration(t);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
}
