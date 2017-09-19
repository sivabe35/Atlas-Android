package com.layer.ui.message;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;

public class MessageItemsListViewModel extends BaseObservable {
    protected MessageItemsAdapter mMessageItemsAdapter;
    protected List<CellFactory> mCellFactories;
    protected SwipeableItem.OnItemSwipeListener<Message> mItemSwipeListener;

    public MessageItemsListViewModel(Context context, LayerClient layerClient,
                                     ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter,
                                     IdentityFormatter identityFormatter) {
        mMessageItemsAdapter = new MessageItemsAdapter(context, layerClient,
                imageCacheWrapper, dateFormatter, identityFormatter);
    }

    @Bindable
    public MessageItemsAdapter getAdapter() {
        return mMessageItemsAdapter;
    }

    @Bindable
    public List<CellFactory> getCellFactories() {
        return mCellFactories;
    }

    public void setCellFactories(List<CellFactory> cellFactories) {
        mCellFactories = cellFactories;
        notifyChange();
    }

    public void setOnItemSwipeListener(SwipeableItem.OnItemSwipeListener<Message> onItemSwipeListener) {
        mItemSwipeListener = onItemSwipeListener;
        notifyChange();
    }

    public SwipeableItem.OnItemSwipeListener<Message> getItemSwipeListener() {
        return mItemSwipeListener;
    }
}
