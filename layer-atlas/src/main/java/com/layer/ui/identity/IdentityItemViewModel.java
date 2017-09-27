package com.layer.ui.identity;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.fourpartitem.FourPartItemViewModel;

import java.util.HashSet;
import java.util.Set;

public class IdentityItemViewModel extends FourPartItemViewModel<Identity> {
    public IdentityItemViewModel(Context context, LayerClient layerClient) {
        super(context, layerClient);
    }

    @Override
    public void setItem(Identity identity) {
        super.setItem(identity);
        notifyChange();
    }

    @Override
    public String getTitle() {
        return getIdentityFormatter().getDisplayName(getItem());
    }

    @Override
    public String getSubtitle() {
        return getIdentityFormatter().getMetaData(getItem());
    }

    @Override
    public String getAccessoryText() {
        if (getItem().getLastSeenAt() == null) {
            return "";
        }
        return getDateFormatter().formatTimeDay(getItem().getLastSeenAt());
    }

    @Override
    public boolean isSecondaryState() {
        return false;
    }

    @Override
    public Set<Identity> getIdentities() {
        Set<Identity> identities = new HashSet<>(1);
        identities.add(getItem());

        return identities;
    }
}
