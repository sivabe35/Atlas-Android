package com.layer.ui.fourpartitem;

import android.databinding.Bindable;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Queryable;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Set;

public abstract class FourPartItemViewModel<ITEM extends Queryable> extends ItemViewModel<ITEM> {
    public FourPartItemViewModel() {
        super();
    }

    @Bindable
    public abstract String getTitle();

    @Bindable
    public abstract String getSubtitle();

    @Bindable
    public abstract String getAccessoryText();

    @Bindable
    public abstract boolean isSecondaryState();

    @Bindable
    public abstract Set<Identity> getIdentities();
}
