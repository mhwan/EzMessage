package com.app.mhwan.easymessage.Fragment;


import android.content.Intent;
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
public class SMSFragment extends Fragment implements MainActivity.BackKeyPressedListner, MainActivity.ActivityResultListner{
    private static final String ARG_PARAM1 = "param1";
    private int position;
    private ListView menulist;
    private BottomSheetLayout bottomSheetLayout;
    private View view;
    private MainActivity activity;
    private FloatingActionButton floatingActionButton;
    private ArrayList<MessageListItem> mListItems;
    public SMSFragment(){

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
        activity = (MainActivity)getActivity();
        activity.setOnBackKeyPressedListner(this);
        activity.setOnActivityResultListner(this);
        initView();

        return view;
    }
    private void initView(){
        //init bottomsheets, fab
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
        final RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, R.drawable.line_divider));
        addmessageitem();
        final MessageListAdapter mAdapter = new MessageListAdapter(activity, mListItems);
        ((MessageListAdapter) mAdapter).setMode(Attributes.Mode.Single);
        recyclerView.setAdapter(mAdapter);
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

    private void swipeOpen(View view){
        if (view instanceof SwipeLayout)
            ((SwipeLayout) view).open(true);
        else if (view instanceof ViewGroup){
            for (int i=0; i < ((ViewGroup) view).getChildCount(); i++){
                View innerview = ((ViewGroup) view).getChildAt(i);
                swipeOpen(innerview);
            }
        }
    }

    private void addmessageitem(){
        mListItems = new ArrayList<MessageListItem>();
        for (int i =0; i<3; i++){
            MessageListItem item = new MessageListItem();
            item.setName("배명환");
            item.setColor_value(5);
            item.setPh_num("010-5057-4876");
            item.setCount_no_read((i==1)? 0 : 100);
            item.setLast_mTime("2014.06.12 12:20");
            item.setLast_mContent("내용을 적으려하니 기분이 참 좋지않습니까라라ㅏ랄라라라라라 환상의 나라로 오세요 즐거운 축제가 열리는 곳 환장의나라 에버랜드");
            mListItems.add(item);
        }
    }
    public void slidetofab(){
        bottomSheetLayout.slideInFab();
    }
    private View.OnClickListener fablistner(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.expandFab();
            }
        };
    }

    @Override
    public void onBackPressed() {
        //fab가 확장되었을경우 다시 들어가도록 처리할것
        if (bottomSheetLayout.isFabExpanded()) {
            DLog.d("FabExpanded : true");
            bottomSheetLayout.slideInFab();
        }
        else
            activity.getDoubleBackKeyPressed().onBackPressed();
    }


    @Override
    public void onActivityResults(int requestcode, int resultcode, Intent data) {
        if (requestcode == AppUtility.BasicInfo.MESSAGE_REQUEST){
            if (resultcode == AppUtility.BasicInfo.RESULT_NEW_MESSAGE){
                DLog.d("new_messsage!@#!@");
            }
            else if (resultcode == AppUtility.BasicInfo.RESULT_NOT_NEW){
                DLog.d("not new_messsage!@#!@");
            }
        }
    }
}
