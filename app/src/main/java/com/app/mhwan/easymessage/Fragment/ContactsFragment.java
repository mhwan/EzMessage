package com.app.mhwan.easymessage.Fragment;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.app.mhwan.easymessage.Activity.MainActivity;
import com.app.mhwan.easymessage.CustomBase.AppUtility;
import com.app.mhwan.easymessage.CustomBase.DLog;
import com.app.mhwan.easymessage.CustomView.ContactAdapter;
import com.app.mhwan.easymessage.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements MainActivity.BackKeyPressedListner{
    private static final String ARG_PARAM1 = "param1";
    private View view;
    private ListView listView;
    private int position;
    private ContactAdapter adapter;
    private MainActivity activity;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance(int position) {
        ContactsFragment fragment = new ContactsFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        activity = (MainActivity) getActivity();
        activity.setOnBackKeyPressedListner(this);
        initView(inflater);
        return view;
    }

    public void initView(LayoutInflater inflater){
        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new ContactAdapter(AppUtility.getAppinstance().getContactList(), getActivity(), view.findViewById(R.id.contact_root));
        adapter.setMode(Attributes.Mode.Single);
        View mView = inflater.inflate(R.layout.header_contact_list, null, true);
        listView.addHeaderView(mView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSwipeLayout(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                openSwipeLayout(position);
                return false;
            }
        });
        listView.setTextFilterEnabled(true);
        final SearchView searchView = (SearchView) mView.findViewById(R.id.search_view);
        setSearchViewColor(searchView);
        searchView.setIconifiedByDefault(false);
        //closelistener 작동x 버튼 아이디에 접근하여 클릭리스너를 달음
        searchView.findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DLog.d("close!!");
                adapter.closeAllItems();
                searchView.setQuery("", false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText))
                    listView.clearTextFilter();
                else {
                    adapter.closeAllItems();
                    listView.setFilterText(newText);
                }
                return true;
            }
        });

    }


    private void openSwipeLayout(int position){
        ((SwipeLayout) (listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
    }
    /**
     * searchview id
     *
     * serchicon : android.support.v7.appcompat.R.id.search_mag_icon
     * closeicon : R.id.search_close_btn
     * autocomplete : R.id.search_src_text
     * @param searchView
     */
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

    @Override
    public void onBackPressed() {
        activity.getDoubleBackKeyPressed().onBackPressed();
    }
}
