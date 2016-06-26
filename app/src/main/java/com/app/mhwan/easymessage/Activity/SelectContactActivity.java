package com.app.mhwan.easymessage.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomView.ContactItem;
import com.app.mhwan.easymessage.CustomView.SelectContactsAdapter;
import com.app.mhwan.easymessage.R;

import java.util.ArrayList;

public class SelectContactActivity extends AppCompatActivity {
    private ArrayList<ContactItem> contactItems;
    private int num=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        contactItems = (ArrayList<ContactItem>)getIntent().getSerializableExtra(AppUtility.BasicInfo.SELECT_CONTACT_LIST);
        initView();
    }

    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        setToolbarTitle();
        final ListView listView = (ListView)findViewById(R.id.select_listview);
        final SelectContactsAdapter adapter = new SelectContactsAdapter(contactItems, getApplicationContext());
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.header_contact_list, null, true);
        listView.addHeaderView(mView);
        listView.setTextFilterEnabled(true);
        SearchView searchView = (SearchView) mView.findViewById(R.id.search_view);
        setSearchViewColor(searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText))
                    listView.clearTextFilter();
                else
                    listView.setFilterText(newText);
                return true;
            }
        });

        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    ContactItem item = adapter.getItem(position-1);
                    boolean checked = adapter.getOriginalItemChecked(item.getId());
                    if (checked)
                        num--;
                    else
                        num++;

                    setToolbarTitle();
                    item.setChecked(!checked);
                    adapter.setOriginalItemChecked(item.getId(), !checked);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        toolbar.findViewById(R.id.toolbar_okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter !=null) {
                    ArrayList<String> arr = adapter.getCheckedItemIdList();
                    if (arr == null){
                        setResult(RESULT_CANCELED);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        intent.putStringArrayListExtra(AppUtility.BasicInfo.KEY_RESULT_SELECTED_ID, arr);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }

    private void setSearchViewColor(SearchView searchView) {
        LinearLayout ll = (LinearLayout) searchView.getChildAt(0);
        LinearLayout ll2 = (LinearLayout) ll.getChildAt(2);
        LinearLayout ll3 = (LinearLayout) ll2.getChildAt(1);

        SearchView.SearchAutoComplete autoComplete = ((SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text));
        ImageView searchCloseButton = (ImageView) ll3.getChildAt(1);
        ImageView searchIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        ImageView labelView = (ImageView) ll.getChildAt(1);
        autoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, AppUtility.getAppinstance().dpToPx(18));
        autoComplete.setHintTextColor(getResources().getColor(R.color.colorHint));
        autoComplete.setTextColor(getResources().getColor(R.color.colorDarkPrimary));

        searchIcon.getDrawable().setColorFilter(Color.parseColor("#858585"), PorterDuff.Mode.MULTIPLY);
        searchCloseButton.getDrawable().setColorFilter(Color.parseColor("#858585"), PorterDuff.Mode.MULTIPLY);
        labelView.getDrawable().setColorFilter(getResources().getColor(R.color.colorLightprimary), PorterDuff.Mode.MULTIPLY);
    }

    private void setToolbarTitle(){
        String numb = "<font color='#616161'>" + String.format(" %d", num) + "</font>";
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.select_contact_title) + numb));
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
