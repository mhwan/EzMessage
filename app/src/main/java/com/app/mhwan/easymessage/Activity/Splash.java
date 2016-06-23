package com.app.mhwan.easymessage.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.R;

public class Splash extends Activity {
    private final Handler handler = new Handler();
    final Runnable startMainActivity = new Runnable() {
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
        setBackground(findViewById(R.id.splash), R.drawable.splash);
        hideEverything();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (new RequestPermission(this, 2).isPermission(findViewById(R.id.splash)) && new RequestPermission(this, 3).isPermission(findViewById(R.id.splash)))
            handler.postDelayed(startMainActivity, 1000);
    }

    private void setBackground(View view, int resource_id){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource_id, options);
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            view.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }
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
}
