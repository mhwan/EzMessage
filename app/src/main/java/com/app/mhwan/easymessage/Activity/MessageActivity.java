package com.app.mhwan.easymessage.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.MessageManager;
import com.app.mhwan.easymessage.CustomBase.RecyclerItemClickListener;
import com.app.mhwan.easymessage.CustomBase.RequestPermission;
import com.app.mhwan.easymessage.CustomView.MessageAdapter;
import com.app.mhwan.easymessage.CustomView.MessageItem;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {
    private String uName, uNum;
    private EditText mInputtext;
    private boolean issend = false;
    private ArrayList<MessageItem> messageItems;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        uName = getIntent().getStringExtra(AppUtility.BasicInfo.U_NAME);
        uNum = getIntent().getStringExtra(AppUtility.BasicInfo.U_NUMBER);
        initToolbar(uName, uNum);
        initView();
    }

    private void initView() {
        mInputtext = (EditText) findViewById(R.id.message_input);
        findViewById(R.id.message_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mInputtext.getText().toString().isEmpty())
                    sendMessage();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_content), Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //항상 스크롤은 마지막아이템에
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        addItem();
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

    }

    private void sendMessage(){
        if (new RequestPermission(MessageActivity.this, 0).isPermission(findViewById(R.id.root_layout))){
            ArrayList<String> list = new ArrayList<String>();
            list.add(uNum);
            MessageManager manager = new MessageManager(list, mInputtext.getText().toString());
            manager.sendMessage();
            issend = true;
            mInputtext.setText("");
        }
    }
    private void addItem() {
        messageItems = new ArrayList<MessageItem>();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 2; j++) {
                MessageItem item = new MessageItem();
                item.setmTime("오전 11:30");
                item.setnPhNum("01050574876");
                item.setmColor_id(14);
                //받음
                if (i % 2 == 0) {
                    item.setmType(1);
                    item.setmContent((i==10)?"용꼬리를 잘랐더니 뱀꼬리가 되고 뱀꼬리를 쥐가 잘랐더니 쥐가 쥐도새도 모르게 ":"내용내용내용내용ㅇ요용용요고고고고고노");
                } else {
                    item.setmType(0);
                    item.setmContent((i==11)? "그으대 키어긔 치이난 사랑이 내 안을 파고드는 카시이가 되어 제바알알 가라고오ㅗ오옹 아주 카라고 애써어도오 나를 괴로피는데에엥":"내용내용");
                }
                messageItems.add(item);
            }
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
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setSubtitle(number);
    }

    private void finishActivity() {
        setResult((issend) ? AppUtility.BasicInfo.RESULT_NEW_MESSAGE : AppUtility.BasicInfo.RESULT_NOT_NEW);
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
                        adapter.removeMessage(position);
                        Toast.makeText(MessageActivity.this, getString(R.string.message_delete_successfully), Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        dialog.dismiss();
                        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MessageActivity.this);
                        mBuilder.setTitle(getString(R.string.message_type));
                        String[] strings = getResources().getStringArray(R.array.message_type_array);
                        mBuilder.setItems(getResources().getStringArray(R.array.message_type_array), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MessageActivity.this, SendSMSActivity.class);
                                intent.putExtra(AppUtility.BasicInfo.SEND_TYPE, which);
                                intent.putExtra(AppUtility.BasicInfo.SEND_PHONE_NUMBER, -1);
                                intent.putExtra(AppUtility.BasicInfo.SEND_CONTENT, messageItems.get(position).getmContent());
                                startActivityForResult(intent, AppUtility.BasicInfo.SEND_REQUEST);
                                dialog.dismiss();
                                finish();
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
                        setClipboard(messageItems.get(position).getmContent());
                        Toast.makeText(getApplicationContext(), getString(R.string.message_copy_clipboard), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageItems.get(position).getmContent());
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
        MenuItem searchitem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchitem.getActionView();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_call:
                if (new RequestPermission(this, 1).isPermission(findViewById(R.id.root_layout)))
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + uNum)));
                break;
            case R.id.sub_delete_all:
                adapter.removeAllMessage();
                break;
            case R.id.menu_search:
                DLog.i("click search");
                item.expandActionView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
