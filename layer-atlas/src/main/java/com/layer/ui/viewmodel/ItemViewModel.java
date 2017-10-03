package com.layer.ui.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.layer.sdk.query.Queryable;
import com.layer.ui.recyclerview.OnItemClickListener;

public class ItemViewModel<ITEM extends Queryable> extends BaseObservable {

    protected ITEM mItem;
    protected OnItemClickListener<ITEM> mItemClickListener;

    public ItemViewModel() {
    }

    @Bindable
    public ITEM getItem() {
        return mItem;
    }

    public void setItem(ITEM item) {
        mItem = item;
    }

    public void setEmpty() {
        mItem = null;
    }

    public void setItemClickListener(OnItemClickListener<ITEM> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public OnItemClickListener<ITEM> getItemClickListener() {
        return mItemClickListener;
    }

    public View.OnClickListener onClickItem() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mItem);
                }
            }
        };
    }
}
