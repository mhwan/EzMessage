package com.app.mhwan.easymessage.Fragment;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.app.mhwan.easymessage.Activity.MainActivity;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.MessageDBHelper;
import com.app.mhwan.easymessage.CustomBase.NewMessageListener;
import com.app.mhwan.easymessage.CustomBase.SMSReceiver;
import com.app.mhwan.easymessage.CustomView.BottomSheet;
import com.app.mhwan.easymessage.CustomView.BottomSheetAdapter;
import com.app.mhwan.easymessage.CustomView.DividerItemDecoration;
import com.app.mhwan.easymessage.CustomView.MessageListAdapter;
import com.app.mhwan.easymessage.CustomView.MessageListItem;
import com.app.mhwan.easymessage.R;
import com.bowyer.app.fabtransitionlayout.BottomSheetLayout;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SMSFragment extends Fragment implements MainActivity.BackKeyPressedListner, MainActivity.ActivityResultListner, NewMessageListener {
    private static final String ARG_PARAM1 = "param1";
    private int position;
    private ListView menulist;
    private BottomSheetLayout bottomSheetLayout;
    private View view;
    private MainActivity activity;
    private MessageListAdapter mAdapter;
    private FloatingActionButton floatingActionButton;
    private ArrayList<MessageListItem> mListItems;
    private MessageDBHelper messageDBHelper;
    private SMSReceiver receiver;
    private RecyclerView recyclerView;

    public SMSFragment() {

    }

    public static SMSFragment newInstance(int position) {
        SMSFragment fragment = new SMSFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sms, container, false);
        activity = (MainActivity) getActivity();
        messageDBHelper = new MessageDBHelper(activity);
        activity.setOnBackKeyPressedListner(this);
        activity.setOnActivityResultListner(this);

        initView();

        return view;
    }

    private void initView() {
        bottomSheetLayout = (BottomSheetLayout) view.findViewById(R.id.bottom_sheet);
        menulist = (ListView) view.findViewById(R.id.list_menu);
        floatingActionButton = activity.getFloatingActionButton();
        ArrayList<BottomSheet> bottomSheets = new ArrayList<>();
        bottomSheets.add(
                BottomSheet.to().setBottomSheetMenuType(BottomSheet.BottomSheetMenuType.GENERAL_SMS));
        bottomSheets.add(
                BottomSheet.to().setBottomSheetMenuType(BottomSheet.BottomSheetMenuType.SCHEDULED_SMS));
        bottomSheets.add(
                BottomSheet.to().setBottomSheetMenuType(BottomSheet.BottomSheetMenuType.MULTI_SMS));
        final BottomSheetAdapter adapter = new BottomSheetAdapter(getActivity(), bottomSheets, this);
        menulist.setAdapter(adapter);
        bottomSheetLayout.setFab(floatingActionButton);
        floatingActionButton.setOnClickListener(fablistner());

        //init recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, R.drawable.line_divider));
        mListItems = messageDBHelper.getAllLastMessageList();
        mAdapter = new MessageListAdapter(activity, mListItems, messageDBHelper);
        ((MessageListAdapter) mAdapter).setMode(Attributes.Mode.Single);
        recyclerView.setAdapter(mAdapter);
        setSignviewVisible();
        /*
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DLog.d(position+"!!@!@@!!");
                if (!mAdapter.isOpen(position)) {
                    Intent intent = new Intent(activity, MessageActivity.class);
                    intent.putExtra(AppUtility.BasicInfo.U_NAME, mListItems.get(position).getName());
                    intent.putExtra(AppUtility.BasicInfo.U_NUMBER, mListItems.get(position).getPh_num());
                    startActivityForResult(intent, AppUtility.BasicInfo.MESSAGE_REQUEST);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                swipeOpen(recyclerView.getChildAt(position));
            }
        }));*/
    }

    private void swipeOpen(View view) {
        if (view instanceof SwipeLayout)
            ((SwipeLayout) view).open(true);
        else if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerview = ((ViewGroup) view).getChildAt(i);
                swipeOpen(innerview);
            }
        }
    }

    public void slidetofab() {
        bottomSheetLayout.slideInFab();
    }

    private View.OnClickListener fablistner() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.expandFab();
            }
        };
    }

    private void setSignviewVisible(){
        View view = activity.getSignview();
        if (view != null)
            view.setVisibility((mAdapter.getAllNewCount() > 0) ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onBackPressed() {
        //fab가 확장되었을경우 다시 들어가도록 처리할것
        if (bottomSheetLayout.isFabExpanded()) {
            DLog.d("FabExpanded : true");
            bottomSheetLayout.slideInFab();
        } else
            activity.getDoubleBackKeyPressed().onBackPressed();
    }


    @Override
    public void onActivityResults(int requestcode, int resultcode, Intent data) {
           update();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = SMSReceiver.getSmsReceiver(this);
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.addAction(AppUtility.BasicInfo.SCHEDULED_SEND_ACTION);
        activity.registerReceiver(receiver, intentFilter);
        receiver.setListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 리스트를 db로부터 겟하여 어댑터를 업데이트 해준다.
     */
    private void update() {
        DLog.d("fragment update!!");
        mAdapter.updateList(messageDBHelper.getAllLastMessageList());
        setSignviewVisible();
    }

    @Override
    public void updateNewMessage(boolean isreceivemessage, String phnumber) {
        update();
    }

}
