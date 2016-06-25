package com.app.mhwan.easymessage.Activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mhwan.easymessage.CustomBase.AppContext;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.InterverToast;
import com.app.mhwan.easymessage.CustomBase.MessageDBHelper;
import com.app.mhwan.easymessage.CustomBase.MessageManager;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.CustomBase.ScheduleMessageDBHelper;
import com.app.mhwan.easymessage.CustomBase.ScheduleMessageItem;
import com.app.mhwan.easymessage.CustomView.ContactItem;
import com.app.mhwan.easymessage.CustomView.ContactsChipsView;
import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.CustomView.RandomProfileIcon;
import com.app.mhwan.easymessage.R;
import com.mogua.localization.KoreanTextMatcher;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SendSMSActivity extends AppCompatActivity implements TokenCompleteTextView.TokenListener {
    private EditText messageContent, multi_input;
    private ContactsChipsView chipsView;
    private ArrayList<ContactItem> contactsList;
    private TextView schedule;
    private int sendType; //0은 일반 1은 예약 2는 반복
    private Calendar scheduled_date;
    private volatile boolean issetSchedule_time = false;
    private ScheduleMessageDBHelper dbHelper;
    private String forwardcontent = null;
    private int MAX_BYTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        int temp_Id=-1;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sendType = bundle.getInt(AppUtility.BasicInfo.SEND_TYPE);
            temp_Id = bundle.getInt(AppUtility.BasicInfo.SEND_PHONE_NUMBER);
            forwardcontent = bundle.getString(AppUtility.BasicInfo.SEND_CONTENT, null);
        }
        MAX_BYTE = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingActivity.KEY_MESSAGE_LENGTH, "80"));
        DLog.d(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingActivity.KEY_NOTIFICATION_TYPE, ""));
        dbHelper = new ScheduleMessageDBHelper(this);
        initToolbar();
        initChipsView(temp_Id);
        initContent();

        Runnable runnable = new CurrentTimeRunner();
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void initContent() {
        messageContent = (EditText) findViewById(R.id.sms_content);
        //전달된 메시지라면 내용설정
        if (forwardcontent!=null)
            messageContent.setText(forwardcontent);
        RelativeLayout schedule_window = (RelativeLayout) findViewById(R.id.schedule_window);
        RelativeLayout multy_window = (RelativeLayout) findViewById(R.id.multy_window);
        schedule = (TextView) findViewById(R.id.schedule_input);
        multi_input = (EditText) findViewById(R.id.multy_input);
        switch (sendType) {
            case 0:
                schedule_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invalidMessageBuilder(sendType, getResources().getString(R.string.is_schedule));
                    }
                });
                multi_input.setFocusable(false);
                multi_input.setClickable(false);
                multy_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invalidMessageBuilder(sendType, getResources().getString(R.string.is_repitition));
                    }
                });
                break;
            case 1:
                schedule_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDateTimePicker();
                    }
                });
                multi_input.setFocusable(false);
                multi_input.setClickable(false);
                multy_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invalidMessageBuilder(sendType, getResources().getString(R.string.is_repitition));
                    }
                });
                break;
            case 2:
                schedule_window.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invalidMessageBuilder(sendType, getResources().getString(R.string.is_schedule));
                    }
                });
                multi_input.setClickable(true);
                multi_input.setFocusable(true);
                break;
        }
        setupUI(findViewById(R.id.root_layout));
        setByteTextview(0);
        messageContent.addTextChangedListener(new TextWatcher() {
            InterverToast interverToast = new InterverToast(getApplicationContext(), String.format(getString(R.string.over_byte_message), MAX_BYTE), Toast.LENGTH_SHORT);
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (AppUtility.getAppinstance().getTextByte(messageContent.getText().toString()) <= MAX_BYTE){
                    if (interverToast != null)
                        interverToast.killAllToast();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int text_byte = AppUtility.getAppinstance().getTextByte(messageContent.getText().toString());
                //80바이트를 넘을경우 진동과 함께 토스트를 띄움
                if (text_byte > MAX_BYTE)
                    interverToast.show();
                setByteTextview(text_byte);
            }
        });
    }

    private void setByteTextview(int now){
        ((TextView) findViewById(R.id.textbyte)).setText(String.format("%02d/%d byte", now, MAX_BYTE));
    }
    private void setupUI(final View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(view);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void initChipsView(int id){
        final int [] light_color_array = getResources().getIntArray(R.array.user_color);

        findViewById(R.id.ic_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendSMSActivity.this, SelectContactActivity.class);
                intent.putExtra(AppUtility.BasicInfo.SELECT_CONTACT_LIST, contactsList);
                startActivityForResult(intent, AppUtility.BasicInfo.REQUEST_SELECT_CONTACT);
            }
        });
        chipsView = (ContactsChipsView) findViewById(R.id.contacts_chipsview);
        contactsList = AppUtility.getAppinstance().getContactList();
        ArrayAdapter<ContactItem> adapter = new FilteredArrayAdapter<ContactItem>(this, R.layout.item_chips_list, contactsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_chips_list, parent, false);
                }
                ContactItem contactItem = getItem(position);
                ((TextView) convertView.findViewById(R.id.chips_list_phNumber)).setText(contactItem.getUser_phNumber());
                ((TextView) convertView.findViewById(R.id.chips_list_name)).setText(contactItem.getUser_Name());

                RandomProfileIcon dC = (RandomProfileIcon) convertView.findViewById(R.id.chips_ic_person);
                CircleImageView c = (CircleImageView) convertView.findViewById(R.id.chips_image_person);
                if (contactItem.getColor_Id()<0){
                    dC.setVisibility(View.INVISIBLE);
                    c.setVisibility(View.VISIBLE);
                    c.setImageBitmap(AppUtility.getAppinstance().loadContactPhoto(AppContext.getContext().getContentResolver(), contactItem.getPerson_id(), contactItem.getPhoto_id()));
                }
                else {
                    dC.setVisibility(View.VISIBLE);
                    c.setVisibility(View.INVISIBLE);
                    dC.setCircleBackgroundColor(light_color_array[contactItem.getColor_Id()]);
                }
                return convertView;
            }

            @Override
            protected boolean keepObject(ContactItem obj, String mask) {
                mask = mask.toLowerCase();
                return obj.getUser_Name().contains(mask) || obj.getUser_phNumber().contains(mask) || obj.getPhNumberChanged().contains(mask) || KoreanTextMatcher.isMatch(obj.getUser_Name(), mask);
            }
        };
        chipsView.setAdapter(adapter);
        chipsView.setTokenListener(this);
        chipsView.setThreshold(1);
        char[] splitChar = {',', ';', ' ', '\n', '.'};
        chipsView.setSplitChar(splitChar);
        chipsView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);

        //만약 연락처에서 들어왔다면 추가해줄것
        if (id>=0)
            chipsView.addObject(contactsList.get(id));
    }

    private void initToolbar() {
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
        findViewById(R.id.toolbar_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void openDateTimePicker() {
        scheduled_date = Calendar.getInstance();
        DatePickerDialog datepicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
                TimePickerDialog timePicker = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        scheduled_date.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                        setTimeText(scheduled_date.getTime());
                        issetSchedule_time = true;
                    }
                }, scheduled_date.get(Calendar.HOUR_OF_DAY), scheduled_date.get(Calendar.MINUTE), true);
                timePicker.vibrate(true);
                timePicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
                timePicker.setTitle(getString(R.string.time_picker_title));
                timePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                timePicker.show(getFragmentManager(), "timepicker");
            }
        }, scheduled_date.get(Calendar.YEAR), scheduled_date.get(Calendar.MONTH), scheduled_date.get(Calendar.DAY_OF_MONTH));
        datepicker.vibrate(true);
        datepicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
        datepicker.setTitle(getString(R.string.date_picker_title));
        datepicker.show(getFragmentManager(), "datepicker");
    }

    private void invalidMessageBuilder(int type, String sType) {
        String messagebuilder="";
        if (AppUtility.getAppinstance().getCountryISO(AppUtility.LOCALE_TYPE.LANGUAGE).equals("한국어")) {
            String[] messagetype = {"일반 메시지", "예약 메시지", "반복 메시지"};
            messagebuilder = messagetype[type] + "에서는 " + sType + " 변경하실 수 없습니다.";
        }
        else {
            String[] messagetype = {"Normal Message", "Scheduled Message", "Repeated Message"};
            messagebuilder = "You can\'t change "+sType+" in "+messagetype[type];
        }
        Snackbar.make(findViewById(R.id.root_layout), messagebuilder, Snackbar.LENGTH_SHORT).show();
    }

    private void setTimeText(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(AppUtility.BasicInfo.SEND_SMS_DATETIME_FORMAT, Locale.US);
        schedule.setText(format.format(date));
        //return format.format(date);
    }

    @Override
    public void onTokenAdded(Object token) {
        if (token instanceof ContactItem){
            String n = ((ContactItem) token).getPhNumberChanged();
            ArrayList<String> list = getAllNumberList();
            DLog.d(list.size()+"");
            if (list.subList(0, list.size()-1).contains(n) && list.size() != 1) {
                chipsView.removeObject((ContactItem) token);
                Toast.makeText(getApplicationContext(), getString(R.string.duplicate_number), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTokenRemoved(Object token) {
    }


    private class CurrentTimeRunner implements Runnable{
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && !issetSchedule_time){
                try {
                    //쓰레드 내에서 ui를 바꿀수 있는 쓰레드
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTimeText(new Date(System.currentTimeMillis()));
                        }
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    //중간에 닫기버튼이나 뒤로가기를 누를경우
    private void finishActivity() {
        //메시지 내용, 입력된 전화번호가 없을경우
        if (messageContent.getText().toString().isEmpty() && getAllNumberList().isEmpty())
            finish();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.if_close_send_activity))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private ArrayList<String> getAllNumberList(){
        ArrayList<String> number = new ArrayList<String>();
        List<ContactItem> people  = chipsView.getObjects();
        for (ContactItem cPeople : people)
            number.add(cPeople.getPhNumberChanged());
        return number;
    }

    private void sendMessage() {
        final ArrayList<String> numberList = getAllNumberList();
        final String msContent = messageContent.getText().toString();
        final String multi_num = multi_input.getText().toString();
        if (isVaild(numberList, msContent, multi_num) && new RequestPermission(SendSMSActivity.this, 0).isPermission(findViewById(R.id.root_layout))) {
            switch (sendType) {
                case 0:
                    MessageManager messageManager = new MessageManager(numberList, msContent);
                    messageManager.setContext(SendSMSActivity.this);
                    if (messageManager.sendMessage(true))
                        finishedSendSMS(SMS_RESULT.SEND_SUCCESS);
                    else
                        finishedSendSMS(SMS_RESULT.SEND_FAIL);
                    break;
                case 1:
                    //예약문자로 들어왔으나 시간을 설정하지 않았다면 타입을 0번으로 만들것.
                    if (!issetSchedule_time) {
                        sendType = 0;
                        sendMessage();
                        return;
                    }
                    if (System.currentTimeMillis() <= scheduled_date.getTimeInMillis()){
                        ScheduleMessageItem item = new ScheduleMessageItem();
                        item.setNumberlist(TextUtils.join(MessageManager.NUM_SEPERATOR, numberList));
                        item.setContent(msContent);
                        item.setIssend(false);
                        item.setTimemillis(scheduled_date.getTimeInMillis());
                        int request = dbHelper.addSchedule(item);
                        addScheduleToDB(numberList, msContent, scheduled_date.getTime(), request);
                        Intent intent = new Intent(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION);
                        intent.putExtra(AppUtility.BasicInfo.SEND_SCHEDULE_PHNUM, numberList);
                        intent.putExtra(AppUtility.BasicInfo.SEND_SCHEDULE_MESSAGE, msContent);
                        intent.putExtra(AppUtility.BasicInfo.KEY_PREF_REQUEST, request);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(SendSMSActivity.this, request, intent, 0);
                        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduled_date.getTimeInMillis(), pendingIntent);
                        }
                        else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, scheduled_date.getTimeInMillis(), pendingIntent);
                        }
                        finishedSendSMS(SMS_RESULT.SEND_RESERVED);
                    }
                    else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.if_time_set_past), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getResources().getString(R.string.warning_multi_send))
                            .setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    MultiSMSSendTask task = new MultiSMSSendTask();
                                    task.setMultiMessage(Integer.parseInt(multi_num), numberList, msContent);
                                    task.execute();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                    break;
            }
        }
    }

    private void addScheduleToDB(ArrayList<String> numberlist, String content, Date date, int request){
        SimpleDateFormat dateFormat = new SimpleDateFormat(AppUtility.BasicInfo.DB_DATETIME_FORMAT);
        MessageDBHelper messageDBHelper = new MessageDBHelper(this);
        for (String number : numberlist){
            long[] ids = AppUtility.getAppinstance().getPhotoPersonId(number.replace("-", ""));
            number = number.replace("-", "");
            MessageItem item = new MessageItem();
            item.setPh_number(number);
            item.setContent(content);
            item.setTime(dateFormat.format(date));
            item.setType(0);
            item.setIs_last_message(true);
            item.setIs_read(true);
            item.setRequest_code(request);
            int code = messageDBHelper.getSavedColorId(number);
            //-1이라면 db에 저장이 안되있으므로 새로 저장할 코드 생성
            item.setColor_id((code < 0)? AppUtility.getAppinstance().getColorIdtoDB(number) : code);
            messageDBHelper.addMessage(item);
        }
    }

    private void finishedSendSMS(SMS_RESULT result){
        if (result == SMS_RESULT.SEND_SUCCESS)
            setResult(RESULT_OK);
        else if (result == SMS_RESULT.SEND_FAIL)
            setResult(AppUtility.BasicInfo.SEND_ERROR);
        else if (result == SMS_RESULT.SEND_RESERVED)
            setResult(AppUtility.BasicInfo.SEND_RESERVED_SUCCESS);
        finish();
    }

    private class MultiSMSSendTask extends AsyncTask<Void, Integer, Void> {
        ProgressDialog watingDialog = new ProgressDialog(SendSMSActivity.this);
        private int count;
        private String content;
        private ArrayList<String> numberList;
        private final String default_format_message = getResources().getString(R.string.sending_message);
        private boolean issuccesfull = true;
        private MessageManager messageManager;
        public void setMultiMessage(int count, ArrayList<String> list, String content) {
            this.count = count;
            this.numberList = list;
            this.content = content;
            messageManager = new MessageManager();
            messageManager.setMessage(numberList, this.content);
            messageManager.setContext(SendSMSActivity.this);
        }

        //전송전
        @Override
        protected void onPreExecute() {
            watingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            watingDialog.setCanceledOnTouchOutside(false);
            watingDialog.setCancelable(false);
            watingDialog.show();
            super.onPreExecute();
        }

        //전송후
        @Override
        protected void onPostExecute(Void aVoid) {
            watingDialog.dismiss();
            if (issuccesfull)
                finishedSendSMS(SMS_RESULT.SEND_SUCCESS);
            else if (!issuccesfull)
                finishedSendSMS(SMS_RESULT.SEND_FAIL);
            super.onPostExecute(aVoid);
        }
        //진행상태 업데이트
        @Override
        protected void onProgressUpdate(Integer... progress) {
            watingDialog.setMessage(String.format(default_format_message, progress[0], count));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                long second = 200;
                //메시지 개수에대한 대기시간 설정
                long during_second;
                if (count < 2)
                    during_second = 0;
                else if (count < 7)
                    during_second = 600;
                else if (count < 12)
                    during_second = 1100;
                else
                    during_second = 1800;
                for (int i = 0; i < count; i++) {
                    publishProgress(i+1);
                    issuccesfull = messageManager.sendMessage(true);
                    if (i != count-1) {
                        second += during_second;
                        Thread.sleep(second);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private boolean isVaild(ArrayList<String> number_list, String content, String multi_num) {
        if (content.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_content), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (multi_num.isEmpty() || Integer.parseInt(multi_num) == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_multi_num), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (number_list.size()==0) {
            Toast.makeText(getApplicationContext(), getString(R.string.empty_number), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (AppUtility.getAppinstance().getTextByte(content) > MAX_BYTE){
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.over_byte_message), MAX_BYTE), Toast.LENGTH_SHORT).show();
            return false;
        }

        for (String number : number_list){
            //핸드폰 번호도 일반 집전화번호도 아닐 경우 국가 대한민국.
            if (AppUtility.getAppinstance().getCountryISO(AppUtility.LOCALE_TYPE.NETWORK).equals("KR")) {
                if ((!Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", number) && (!Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", number)))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            //문자전송 권한에 대한 콜백을 받음
            case AppUtility.BasicInfo.REQUEST_SEND_SMS :
                //권한을 승인한경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    sendMessage();
                    //권한을 승인하지 않은경우
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_sms_send_permission), Toast.LENGTH_SHORT).show();
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppUtility.BasicInfo.REQUEST_SELECT_CONTACT){
            if (resultCode == RESULT_OK){
                ArrayList<String> arr = data.getStringArrayListExtra(AppUtility.BasicInfo.KEY_RESULT_SELECTED_ID);
                for (String a : arr)
                    chipsView.addObject(contactsList.get(Integer.parseInt(a)));

            }
        }
    }

    private void hideKeyboard(View v) {
        InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public enum SMS_RESULT{ SEND_SUCCESS, SEND_FAIL, SEND_RESERVED }
}
