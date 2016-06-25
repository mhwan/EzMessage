package com.app.mhwan.easymessage.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.MessageDBHelper;
import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 5. 30..
 */
public class SettingActivity extends AppCompatActivity {
    public static final String KEY_DELETE_ALL_MESSAGE = "key_message_delete_all";
    public static final String KEY_MESSAGE_LENGTH = "key_message_byte";
    public static final String KEY_SEND_MAIL = "key_send_mail";
    public static final String KEY_NOTIFICATION_TYPE = "key_notification_type";
    public static final String KEY_BOOL_NOTIFICATION = "key_notification_title";
    public static final String KEY_POPUP_NOTIFICATION = "key_notification_check";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initToolbar();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_setting, new SettingFragment()).commit();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.mipmap.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity(){
        setResult(RESULT_OK);
        SettingActivity.this.finish();
    }
    public static class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{
        Preference pref_deleteAll, pref_message_length, pref_sendMail;
        ListPreference pref_notifi_type;
        String[] notify;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            getActivity().setTheme(R.style.MyTheme);
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting_preferences);

            pref_deleteAll = findPreference(KEY_DELETE_ALL_MESSAGE);
            pref_message_length = findPreference(KEY_MESSAGE_LENGTH);
            pref_sendMail = findPreference(KEY_SEND_MAIL);
            pref_notifi_type = (ListPreference) findPreference(KEY_NOTIFICATION_TYPE);

            pref_deleteAll.setOnPreferenceClickListener(this);
            pref_message_length.setOnPreferenceClickListener(this);
            pref_sendMail.setOnPreferenceClickListener(this);
            pref_notifi_type.setOnPreferenceChangeListener(this);

            //메시지 최대 발신 길이는 해외 140, 우리나라 80으로
            pref_message_length.setDefaultValue(getDefaultbyte());

            notify = getActivity().getResources().getStringArray(R.array.notification_type);
            //preference 값 초기화
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            changeSummary(pref_message_length, sharedPreferences.getString("key_message_byte", "80"));
            if (pref_notifi_type.getValue() == null)
                pref_notifi_type.setValueIndex(0);
            changeSummary(pref_notifi_type, notify[Integer.valueOf(sharedPreferences.getString("key_notification_type", "0"))]);

        }


        @Override
        public boolean onPreferenceClick(final Preference preference) {
            String key = preference.getKey();

            if (key.equals(pref_deleteAll.getKey())){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.delete_all_message))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MessageDBHelper dbHelper = new MessageDBHelper(getActivity());
                                dbHelper.removeAll();
                                Toast.makeText(getActivity(), getString(R.string.message_all_delete), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setCancelable(true);

                builder.create().show();


                return true;
            } else if (key.equals(pref_sendMail.getKey())){
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","mhwanbae21@gmail.com", null));
                startActivity(Intent.createChooser(intent, null));
                return true;
            } else if (key.equals(pref_message_length.getKey())){
                final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
                String value = preferences.getString(KEY_MESSAGE_LENGTH, "80");
                final LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogview = inflater.inflate(R.layout.dialog_edittext, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialogview);

                final AlertDialog dialog = builder.create();
                final EditText input = (EditText) dialogview.findViewById(R.id.edit_text);
                input.setText(value);
                Selection.setSelection(input.getText(), value.length());
                dialogview.findViewById(R.id.to_default).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String value = getDefaultbyte();
                        input.setText(value);
                        Selection.setSelection(input.getText(), value.length());
                    }
                });
                dialogview.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = input.getText().toString();
                        if (text.isEmpty() || Integer.valueOf(text) <= 0)
                            Toast.makeText(getActivity(), getString(R.string.invalid_byte_value), Toast.LENGTH_SHORT).show();
                        else {
                            preferences.edit().putString(KEY_MESSAGE_LENGTH, text).commit();
                            dialog.dismiss();
                            changeSummary(pref_message_length, text);
                        }
                    }
                });
                dialogview.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;
            }
            return false;
        }

        private String getDefaultbyte(){
            int byte_value;
            //서비스 네트워크 지역이 한국이라면 80byte, 외국이라면 140으로
            if (AppUtility.getAppinstance().getCountryISO(AppUtility.LOCALE_TYPE.NETWORK).equals("KR"))
                byte_value = 80;
            else
                byte_value = 140;

            return Integer.toString(byte_value);
        }
        //프리퍼런스 값이 바뀔때마다 summury로 설정
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (key.equals(pref_notifi_type.getKey())){
                int i = Integer.valueOf((String)newValue);
                changeSummary(preference, notify[i]);
                showAudiomodeMessage(i);

                return true;
            }
            return false;
        }

        private void changeSummary(Preference preference, String text){
            if (preference.getKey().equals(pref_message_length.getKey())){
                text = String.format(getString(R.string.setting_max_byte_summary), text);
            }
            preference.setSummary(text);
        }

        private void showAudiomodeMessage(int i){
            AudioManager audio = (AudioManager) AppContext.getContext().getSystemService(Context.AUDIO_SERVICE);
            int mode = audio.getRingerMode();

            //무음모드인데 진동 또는 소리 알림 설정할때
            //진동모드인데 소리 알림 설정
            if ((mode == AudioManager.RINGER_MODE_SILENT && i!=2) || (mode == AudioManager.RINGER_MODE_VIBRATE && i==1)){
                Toast.makeText(AppContext.getContext(), getString(R.string.change_notification_mode), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
