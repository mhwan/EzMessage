package com.app.mhwan.easymessage.CustomView;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mhwan.easymessage.R;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by Mhwan on 2016. 4. 9..
 */
public class ContactsChipsView extends TokenCompleteTextView<ContactItem> {
    private Context context;
    public ContactsChipsView(Context context){
        super(context);
        this.context = context;
    }
    public ContactsChipsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
    public ContactsChipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    @Override
    protected View getViewForObject(ContactItem object) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = (View) inflater.inflate(R.layout.item_contact_chips, (ViewGroup) ContactsChipsView.this.getParent(), false);
        String str = "";
        if (object.getType().equals(ContactItem.Contact_type.NAME_TYPE))
            str = object.getUser_Name();
        else if (object.getType().equals(ContactItem.Contact_type.NUMBER_TYPE))
            str = object.getUser_phNumber();

        ((TextView) view.findViewById(R.id.chips_name)).setText(str);
        return view;
    }

    @Override
    protected ContactItem defaultObject(String completionText) {
        String nString = completionText.replaceAll("-", "");
        if (isNumber(nString))
            return new ContactItem(completionText, ContactItem.Contact_type.NUMBER_TYPE);
        else {
            Toast.makeText(context, context.getResources().getString(R.string.new_number_invalid), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean isNumber(String s){
        char[] c = s.toCharArray();
        for (char c1 : c){
            if (c1<'0'|| c1>'9')
                return false;
        }
        return true;
    }
}
