package com.app.mhwan.easymessage.CustomView;

import com.app.mhwan.easymessage.R;

/**
 * Created by Mhwan on 2016. 3. 18..
 */
public class BottomSheet {
    public enum BottomSheetMenuType {
        GENERAL_SMS(R.mipmap.ic_general, R.string.general_message ), SCHEDULED_SMS(
                R.mipmap.ic_scheduled, R.string.scheduled_message), MULTI_SMS(R.mipmap.ic_multi,
                R.string.multi_message);

        int resId, nameId;

        BottomSheetMenuType(int resId, int nameId) {
            this.resId = resId;
            this.nameId = nameId;
        }

        public int getResId() {
            return resId;
        }

        public int getName() {
            return nameId;
        }
    }

    BottomSheetMenuType bottomSheetMenuType;

    public static BottomSheet to() {
        return new BottomSheet();
    }

    public BottomSheetMenuType getBottomSheetMenuType() {
        return bottomSheetMenuType;
    }

    public BottomSheet setBottomSheetMenuType(BottomSheetMenuType bottomSheetMenuType) {
        this.bottomSheetMenuType = bottomSheetMenuType;
        return this;
    }

}
