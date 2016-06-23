package com.app.mhwan.easymessage.Activity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.ExportExcel;
import com.app.mhwan.easymessage.CustomBase.InterverToast;
import com.app.mhwan.easymessage.CustomBase.MessageDBHelper;
import com.app.mhwan.easymessage.CustomBase.MessageManager;
import com.app.mhwan.easymessage.CustomBase.NewMessageListener;
import com.app.mhwan.easymessage.CustomBase.RecyclerItemClickListener;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.CustomBase.SMSReceiver;
import com.app.mhwan.easymessage.CustomBase.ScheduleMessageDBHelper;
import com.app.mhwan.easymessage.CustomBase.SoftKeyboardStateWatcher;
import com.app.mhwan.easymessage.CustomView.MessageAdapter;
import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MessageActivity extends AppCompatActivity implements NewMessageListener {
    private String uName, uNum;
    private EditText mInputtext;
    private boolean ischange = false;
    private ArrayList<MessageItem> messageItems;
    private MessageAdapter adapter;
    private SMSReceiver receiver;
    private MessageDBHelper messageDBHelper;
    private RecyclerView recyclerView;
    //private SearchView searchView;
    //private MenuItem searchitem;
    private int MAXBYTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uName = getIntent().getStringExtra(AppUtility.BasicInfo.U_NAME);
        uNum = getIntent().getStringExtra(AppUtility.BasicInfo.U_NUMBER);
        MAXBYTE = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingActivity.KEY_MESSAGE_LENGTH, "80"));
        messageDBHelper = new MessageDBHelper(this);
        messageDBHelper.changeMessageReadStatus(uNum);
        int nId = getIntent().getExtras().getInt(AppUtility.BasicInfo.KEY_NOTIFICATION_ID);
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(nId);
        ShortcutBadger.removeCount(this);
        initToolbar(uName, uNum);
        initView();
    }

    private void initView() {
        mInputtext = (EditText) findViewById(R.id.message_input);
        findViewById(R.id.message_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputtext.getText().toString();
                if (!input.isEmpty() && AppUtility.getAppinstance().getTextByte(input)<= MAXBYTE)
                    sendMessage();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_content), Toast.LENGTH_SHORT).show();
            }
        });

        mInputtext.addTextChangedListener(new TextWatcher() {
            InterverToast interverToast = new InterverToast(getApplicationContext(), String.format(getString(R.string.over_byte_message), MAXBYTE), Toast.LENGTH_SHORT);
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (AppUtility.getAppinstance().getTextByte(mInputtext.getText().toString()) <= MAXBYTE)
                    interverToast.killAllToast();
            }

            @Override
            public void afterTextChanged(Editable s) {
                int text_byte = AppUtility.getAppinstance().getTextByte(mInputtext.getText().toString());
                //80바이트를 넘을경우 진동과 함께 토스트를 띄움
                if (text_byte > MAXBYTE) {
                    interverToast.show();
                }
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //항상 스크롤은 마지막아이템에
        //manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        messageItems = messageDBHelper.getAllMessageList(uNum);
        adapter = new MessageAdapter(this, messageItems);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                createOptionDialog(position).show();
            }
        }));
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        //소프트 키보드 상태변화 리스너
        new SoftKeyboardStateWatcher(findViewById(R.id.root_layout), new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                DLog.d("key board is open!!!");
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onSoftKeyboardClosed() {
                DLog.d("key board is closed!!!");
            }
        });
    }


    private void sendMessage() {
        if (new RequestPermission(MessageActivity.this, 0).isPermission(findViewById(R.id.root_layout))) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(uNum);
            MessageManager manager = new MessageManager(list, mInputtext.getText().toString());
            manager.setContext(MessageActivity.this);
            manager.sendMessage(true);
            ischange = true;
            mInputtext.setText("");
            updateNewMessage(false, uNum);
        }
    }

    private void initToolbar(String name, String number) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });
        if (name != null) {
            getSupportActionBar().setTitle(name);
            getSupportActionBar().setSubtitle(AppUtility.getAppinstance().changeNumberFormat(number));
        } else
            getSupportActionBar().setTitle(AppUtility.getAppinstance().changeNumberFormat(number));
    }

    private void finishActivity() {
        setResult((ischange) ? AppUtility.BasicInfo.RESULT_NEW_MESSAGE : AppUtility.BasicInfo.RESULT_NOT_NEW);
        finish();
    }

    private AlertDialog createOptionDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_option));
        builder.setItems(R.array.option_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //0:삭제 1:전달 2:복사 3:공유
                switch (which) {
                    case 0:
                        dialog.dismiss();
                        if (messageItems.get(position).getRequest_code() < 0) {
                            messageDBHelper.removeMessage(messageItems.get(position).getId());
                            adapter.removeMessage(position);
                            Toast.makeText(MessageActivity.this, getString(R.string.message_delete_successfully), Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder sbuilder = new AlertDialog.Builder(MessageActivity.this);
                            sbuilder.setTitle(getString(R.string.delete_schedulemessage_title));
                            sbuilder.setMessage(getString(R.string.delete_schedulemessage_content));
                            sbuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            sbuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MessageActivity.this, messageItems.get(position).getRequest_code(), intent, 0);
                                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                    alarmManager.cancel(pendingIntent);
                                    pendingIntent.cancel();
                                    ScheduleMessageDBHelper scheduleMessageDBHelper = new ScheduleMessageDBHelper(MessageActivity.this);
                                    scheduleMessageDBHelper.editIsSendSchedule(messageItems.get(position).getRequest_code(), true);
                                    messageDBHelper.removeMessage(messageItems.get(position).getId());
                                    adapter.removeMessage(position);
                                    dialog.dismiss();

                                    Toast.makeText(MessageActivity.this, getString(R.string.schedule_message_cancel_successfully), Toast.LENGTH_SHORT).show();
                                }
                            });
                            sbuilder.create().show();
                        }
                        break;
                    case 1:
                        dialog.dismiss();
                        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MessageActivity.this);
                        mBuilder.setTitle(getString(R.string.message_type));
                        mBuilder.setItems(getResources().getStringArray(R.array.message_type_array), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MessageActivity.this, SendSMSActivity.class);
                                intent.putExtra(AppUtility.BasicInfo.SEND_TYPE, which);
                                intent.putExtra(AppUtility.BasicInfo.SEND_PHONE_NUMBER, -1);
                                intent.putExtra(AppUtility.BasicInfo.SEND_CONTENT, messageItems.get(position).getContent());
                                startActivityForResult(intent, AppUtility.BasicInfo.SEND_REQUEST);
                                dialog.dismiss();
                                ischange = true;
                                finishActivity();
                            }
                        });
                        mBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        mBuilder.create().show();
                        break;
                    case 2:
                        setClipboard(messageItems.get(position).getContent());
                        Toast.makeText(getApplicationContext(), getString(R.string.message_copy_clipboard), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageItems.get(position).getContent());
                        startActivity(Intent.createChooser(sharingIntent, getString(R.string.message_option_share)));
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private void setClipboard(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void closeDialog() {
        if (mInputtext.getText().toString().isEmpty()) {
            finishActivity();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.if_close_send_activity))
                    .setCancelable(true)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishActivity();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        closeDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);

        /*
        검색 익스팬딩 오류..
        searchitem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchitem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchitem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        });
*/
        return true;
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            //문자전송 권한에 대한 콜백을 받음
            case AppUtility.BasicInfo.REQUEST_SEND_SMS:
                //권한을 승인한경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    sendMessage();
                    //권한을 승인하지 않은경우
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_sms_send_permission), Toast.LENGTH_SHORT).show();
                return;
        }
    }*/



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_call:
                if (new RequestPermission(this, 1).isPermission(findViewById(R.id.root_layout)))
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + uNum)));
                break;
            case R.id.sub_delete_all:
                messageDBHelper.removeAllMessage(uNum);
                adapter.removeAllMessage();
                Toast.makeText(MessageActivity.this, getString(R.string.message_delete_successfully), Toast.LENGTH_SHORT).show();
                break;
            case R.id.sub_export_excel:
                /*
                Intent intent = new Intent(getApplicationContext(), ExportActivity.class);
                intent.putParcelableArrayListExtra("messagelist", messageItems);
                startActivity(intent);*/
                ExportBottomSheetFragment bottomSheetFragment = new ExportBottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //리시버 등록
        if (receiver == null) {
            IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.addAction(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION);
            receiver = SMSReceiver.getSmsReceiver(this);
            registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void updateNewMessage(boolean isreceivemessage, String phnumber) {
        DLog.d("call_receive_new!!!");
        ischange = true;
        if (isreceivemessage && phnumber.equals(uNum))
            messageDBHelper.changeMessageReadStatus(uNum);
        adapter.updateAllMessage(messageDBHelper.getAllMessageList(uNum));
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            //문자전송 권한에 대한 콜백을 받음
            case AppUtility.BasicInfo.REQUEST_WRITE_STORAGE :
                //권한을 승인한경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.okay_write_storage_permission), Toast.LENGTH_SHORT).show();
                    //권한을 승인하지 않은경우
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_write_storage_permission), Toast.LENGTH_SHORT).show();
                return;
        }
    }

    public class ExportBottomSheetFragment extends BottomSheetDialogFragment{
        private TextView start_date, end_date, date_period;
        private Button export_button;
        private EditText file_name;
        private Spinner type_spinner;
        private boolean isfirst = true;
        private int sposition;
        private BottomSheetBehavior bottombehavior;
        private View bottom;
        private Calendar start_calendar = Calendar.getInstance();
        private Calendar end_calendar = Calendar.getInstance();
        private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };

        @Override
        public void setupDialog(Dialog dialog, int style) {
            super.setupDialog(dialog, style);
            bottom = View.inflate(getContext(), R.layout.fragment_export_sheet, null);
            dialog.setContentView(bottom);

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) bottom.getParent()).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();

            if( behavior != null && behavior instanceof BottomSheetBehavior ) {
                bottombehavior = (BottomSheetBehavior) behavior;
                bottombehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
                bottombehavior.setPeekHeight(AppUtility.getAppinstance().dpToPx(800));
            }

            start_date = (TextView) bottom.findViewById(R.id.export_text_start_date);
            end_date = (TextView) bottom.findViewById(R.id.export_text_end_date);
            date_period = (TextView) bottom.findViewById(R.id.export_period);
            file_name = (EditText) bottom.findViewById(R.id.export_file_name);
            type_spinner = (Spinner) bottom.findViewById(R.id.export_type_spinner);
            export_button = (Button) bottom.findViewById(R.id.export_button);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                export_button.setBackgroundTintList(MessageActivity.this.getResources().getColorStateList(R.color.colorPrimary));
            } else {
                export_button.setBackgroundColor(ContextCompat.getColor(MessageActivity.this, R.color.colorPrimary));
            }
            export_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExport()) {
                        String filename = file_name.getText().toString();
                        ExportExcel exportExcel = new ExportExcel(adapter.getMessageItems(), sposition, filename, start_calendar, end_calendar);
                        boolean result = exportExcel.writeToExcel();
                        dismiss();

                        if (result)
                            Toast.makeText(getApplicationContext(), getString(R.string.success_export)+"\n"+exportExcel.getComplete_path(), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), getString(R.string.fail_export), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            bottom.findViewById(R.id.export_start).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDatePicker(start_calendar, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            start_calendar.set(year, monthOfYear, dayOfMonth);
                            start_calendar.set(Calendar.HOUR_OF_DAY, 0);
                            start_calendar.set(Calendar.MINUTE, 0);
                            start_calendar.set(Calendar.SECOND, 0);
                            changeDatetext(start_date, start_calendar.getTime());
                        }
                    });
                }
            });
            bottom.findViewById(R.id.export_end).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDatePicker(end_calendar, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            end_calendar.set(year, monthOfYear, dayOfMonth);
                            end_calendar.set(Calendar.HOUR_OF_DAY, 23);
                            end_calendar.set(Calendar.MINUTE, 59);
                            end_calendar.set(Calendar.SECOND, 59);
                            changeDatetext(end_date, end_calendar.getTime());
                        }
                    });
                }
            });

            type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sposition = position;
                    ((TextView)parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorLightprimary));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            MessageItem first = adapter.getFirstMessageItem();
            start_calendar.setTime(AppUtility.getAppinstance().getDate(first.getTime(), AppUtility.BasicInfo.DB_DATETIME_FORMAT));
            changeDatetext(start_date, start_calendar.getTime());
            changeDatetext(end_date, end_calendar.getTime());
        }

        private void changeDatetext(TextView textView, Date date){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            textView.setText(dateFormat.format(date));
            if (start_calendar.after(end_calendar))
                Toast.makeText(getApplicationContext(), getString(R.string.uncorrect_period), Toast.LENGTH_SHORT).show();

            if (!isfirst) {
                int diffInDays = (int) ((end_calendar.getTime().getTime() - start_calendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
                date_period.setText(diffInDays + getString(R.string.period_days));
            }
        }

        private void openDatePicker(Calendar calendar, DatePickerDialog.OnDateSetListener listener){
            isfirst = false;
            DatePickerDialog datepicker = DatePickerDialog.newInstance(listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datepicker.vibrate(true);
            datepicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
            datepicker.setTitle(getString(R.string.date_picker_title));
            datepicker.show(MessageActivity.this.getFragmentManager(), "datepicker");
        }

        private boolean isExport(){
            if (!new RequestPermission(getActivity(), 4).isPermission(bottom))
                return false;
            if (start_calendar.after(end_calendar)) {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_period_export), Toast.LENGTH_SHORT).show();
                return false;
            }

            String name = file_name.getText().toString();
            if (name.isEmpty()||!Pattern.matches("^[a-zA-Z0-9가-힣]*$", name)){
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_filename_export), Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        }
    }
}
