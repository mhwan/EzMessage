package com.app.mhwan.easymessage.Activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomBase.DoubleBackKeyPressed;
import com.app.mhwan.easymessage.Fragment.ContactsFragment;
import com.app.mhwan.easymessage.Fragment.SMSFragment;
import com.app.mhwan.easymessage.R;

public class MainActivity extends AppCompatActivity {
    private DoubleBackKeyPressed doubleBackKeyPressed;
    private ViewPager viewPager;
    private String[] title;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private int[] ic_resource = {R.drawable.selector_contacts, R.drawable.selector_message};
    private BackKeyPressedListner backKeyPressedListner;
    private FloatingActionButton floatingActionButton;
    private View [] views;
    private ActivityResultListner activityResultListner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doubleBackKeyPressed = new DoubleBackKeyPressed(this, findViewById(R.id.root_layout));
        title = getResources().getStringArray(R.array.title_array);
        initView();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab_button);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabpagerAdapter adapter = new TabpagerAdapter(getSupportFragmentManager());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setActionbarTitle(position);
                initTabicon(InitTabType.NOT_FIST);
                switch (position) {
                    case 0:
                        floatingActionButton.hide();
                        break;
                    case 1:
                        floatingActionButton.show();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        Bundle bundle = getIntent().getExtras();
        if ((bundle!=null) ? bundle.getBoolean(AppUtility.BasicInfo.KEY_INTENT_SCHEDULE) : true) {
            //탭레이아웃에 아이콘 달아준다.
            initTabicon(InitTabType.DEFAULT_FIRST);
            setActionbarTitle(0);
            floatingActionButton.hide();
        }
        else {
            initTabicon(InitTabType.NOTIFICATION);
            (tabLayout.getTabAt(1)).select();
            setActionbarTitle(1);
            floatingActionButton.show();
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(3);
        }

        //탭끼리 간격조정
        int betweenSpace = 60;
        ViewGroup slidingTabStrip = (ViewGroup) tabLayout.getChildAt(0);
        for (int i = 0; i < slidingTabStrip.getChildCount() - 1; i++) {
            View v = slidingTabStrip.getChildAt(i);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.rightMargin = betweenSpace;
            if (i == 0)
                params.leftMargin = betweenSpace - 40;
        }
    }

    private void initTabicon(InitTabType type){
        //앱 초기실행시 첫번째 탭이 선택이 되지 않는 현상 해결하기 위해서. 처음에는 직접 리소스 지정
        switch (type){
            case DEFAULT_FIRST:
                views = new View[2];
                views[0] = getLayoutInflater().inflate(R.layout.custom_tab, null);
                views[0].findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_contact_select);
                tabLayout.getTabAt(0).setCustomView(views[0]);
                views[1] = getLayoutInflater().inflate(R.layout.custom_tab, null);
                views[1].findViewById(R.id.icon).setBackgroundResource(R.mipmap.ic_recievemessage_none);
                tabLayout.getTabAt(1).setCustomView(views[1]);
                break;
            case NOT_FIST:
                for (int i = 0; i < 2; i++) {
                    views[i].findViewById(R.id.icon).setBackgroundResource(ic_resource[i]);
                    tabLayout.getTabAt(i).setCustomView(views[i]);
                }
                break;
            case NOTIFICATION:
                views = new View[2];
                for (int i=0; i<2; i++){
                    views[i] = getLayoutInflater().inflate(R.layout.custom_tab, null);
                    views[i].findViewById(R.id.icon).setBackgroundResource(ic_resource[i]);
                    tabLayout.getTabAt(i).setCustomView(views[i]);
                }
        }
    }

    public DoubleBackKeyPressed getDoubleBackKeyPressed() {
        return doubleBackKeyPressed;
    }

    public FloatingActionButton getFloatingActionButton(){
        if (floatingActionButton == null)
            DLog.e("fab : null");
        else
            DLog.i("fab : not null");
        return floatingActionButton;
    }

    private void setActionbarTitle(int position) {
        DLog.d("name : "+ title[position]);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(title[position]);
    }

    public class TabpagerAdapter extends FragmentPagerAdapter {
        public TabpagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = ContactsFragment.newInstance(position);
                    break;
                case 1:
                    fragment = SMSFragment.newInstance(position);
                    break;
                default:
                    fragment = null;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return title.length;
        }
    }

    @Override
    public void onBackPressed() {
        if (backKeyPressedListner != null)
            backKeyPressedListner.onBackPressed();
        else
            super.onBackPressed();
    }

    public void setOnBackKeyPressedListner(BackKeyPressedListner listner) {
        backKeyPressedListner = listner;
    }

    public void setOnActivityResultListner(ActivityResultListner listner){
        this.activityResultListner = listner;
    }

    public interface BackKeyPressedListner {
        void onBackPressed();
    }
    public interface ActivityResultListner{
        void onActivityResults(int requestcode, int resultcode, Intent data);
    }

    //메시지함에서 나왔을때라면 메시지를 새로 업데이트 하기위해 프래그먼트에서 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppUtility.BasicInfo.SEND_REQUEST) {
            if (resultCode == RESULT_OK)
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_message_send), Toast.LENGTH_SHORT).show();
            else if (resultCode == AppUtility.BasicInfo.SEND_ERROR)
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fail_message_send), Toast.LENGTH_SHORT).show();
            else if (resultCode == AppUtility.BasicInfo.SEND_RESERVED_SUCCESS)
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_message_reserved), Toast.LENGTH_SHORT).show();
            else
                DLog.d("sender, close");
        }
        else if (requestCode == AppUtility.BasicInfo.MESSAGE_REQUEST)
            activityResultListner.onActivityResults(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        DLog.d("callback, true");
        switch (requestCode) {
            //문자전송 권한에 대한 콜백을 받음
            case AppUtility.BasicInfo.REQUEST_SEND_SMS :
                //권한을 승인한경우
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.okay_call_permission), Toast.LENGTH_SHORT).show();
                    //권한을 승인하지 않은경우
                else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_call_permission), Toast.LENGTH_SHORT).show();
                return;
        }
    }
    private enum InitTabType{ DEFAULT_FIRST, NOT_FIST, NOTIFICATION }
}
