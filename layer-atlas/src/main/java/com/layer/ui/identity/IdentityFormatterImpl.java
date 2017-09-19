package com.layer.ui.identity;

import android.content.Context;
import android.text.TextUtils;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.R;

public class IdentityFormatterImpl implements IdentityFormatter {

    private Context mContext;

    public IdentityFormatterImpl(Context context) {
        mContext = context;
    }

    @Override
    public String getInitials(Identity identity) {
        String first = identity.getFirstName();
        String last = identity.getLastName();
        if (!TextUtils.isEmpty(first)) {
            if (!TextUtils.isEmpty(last)) {
                return getInitials(first) + getInitials(last);
            }
            return getInitials(first);
        } else if (!TextUtils.isEmpty(last)) {
            return getInitials(last);
        } else {
            return getInitials(identity.getDisplayName());
        }
    }

    @Override
    public String getFirstName(Identity identity) {
        return identity.getFirstName();
    }

    @Override
    public String getLastName(Identity identity) {
        return identity.getLastName();
    }

    @Override
    public String getDisplayName(Identity identity) {
        return identity.getDisplayName();
    }

    @Override
    public String getMetaData(Identity identity) {
        return null;
    }

    private static String getInitials(String name) {
        if (TextUtils.isEmpty(name)) return "";
        if (name.contains(" ")) {
            String[] nameParts = name.split(" ");
            int count = 0;
            StringBuilder b = new StringBuilder();
            for (String part : nameParts) {
                String t = part.trim();
                if (t.isEmpty()) continue;
                b.append(("" + t.charAt(0)).toUpperCase());
                if (++count >= 2) break;
            }
            return b.toString();
        } else {
            return ("" + name.trim().charAt(0)).toUpperCase();
        }
    }

    @Override
    public String getUnknownNameString() {
        return mContext.getString(R.string.layer_ui_message_item_unknown_user);
    }
}
