package com.app.mhwan.easymessage.CustomBase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 3. 24..
 */
public class RequestPermission {
    private int permission_type;
    private Context context;
    private final String[] MANIFEST_PERMISSION = {Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
    public static final int[] REQUEST_PERMISSION = {AppUtility.BasicInfo.REQUEST_SEND_SMS, AppUtility.BasicInfo.REQUEST_CALL, AppUtility.BasicInfo.REQUEST_READ_CONTACT, AppUtility.BasicInfo.REQUEST_PHONE_STATE};
    private String[] snackkbar_message;
    public RequestPermission(Context context, int permission_type){
        this.context = context;
        this.permission_type = permission_type;
        snackkbar_message = context.getResources().getStringArray(R.array.snackbar_message_array);
    }

    public boolean isPermission(View view){
        if (ActivityCompat.checkSelfPermission(context, MANIFEST_PERMISSION[permission_type])!= PackageManager.PERMISSION_GRANTED) {
            if (permission_type == 2 || permission_type ==3) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(snackkbar_message[permission_type])
                        .setCancelable(false)
                        .setPositiveButton(context.getString(R.string.okay), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{MANIFEST_PERMISSION[permission_type]},
                                        REQUEST_PERMISSION[permission_type]);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ((Activity) context).finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                        MANIFEST_PERMISSION[permission_type])) {
                    DLog.d("권한 : 재요청");
                    Snackbar.make(view, snackkbar_message[permission_type],
                            Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(context.getResources().getColor(R.color.colorLightprimary))
                            .setAction(context.getString(R.string.allow), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions((Activity) context,
                                            new String[]{MANIFEST_PERMISSION[permission_type]},
                                            REQUEST_PERMISSION[permission_type]);
                                }
                            })
                            .show();
                } else {
                    DLog.d("권한 : 첫요청");
                    // Camera permission has not been granted yet. Request it directly.
                    Snackbar.make(view, snackkbar_message[permission_type],
                            Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(context.getResources().getColor(R.color.colorLightprimary))
                            .setAction(context.getString(R.string.allow), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions((Activity) context,
                                            new String[]{MANIFEST_PERMISSION[permission_type]},
                                            REQUEST_PERMISSION[permission_type]);
                                }
                            })
                            .show();
                }
            }
        }
        return ActivityCompat.checkSelfPermission(context, MANIFEST_PERMISSION[permission_type])
                == PackageManager.PERMISSION_GRANTED;
    }
}
