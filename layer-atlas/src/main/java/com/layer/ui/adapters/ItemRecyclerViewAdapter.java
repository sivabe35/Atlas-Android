package com.layer.ui.adapters;

import android.content.Context;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.layer.sdk.LayerClient;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.Queryable;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.style.ItemStyle;
import com.layer.ui.util.Log;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class ItemRecyclerViewAdapter<ITEM extends Queryable,
        VIEW_MODEL extends ItemViewModel<ITEM>, BINDING extends ViewDataBinding,
        STYLE extends ItemStyle, VIEW_HOLDER extends ItemViewHolder<ITEM, VIEW_MODEL, BINDING, STYLE>>
        extends RecyclerView.Adapter<VIEW_HOLDER>
        implements RecyclerViewController.Callback {

    private final String TAG;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;

    private RecyclerViewController<ITEM> mQueryController;
    private List<ITEM> mItems;
    private LayerClient mLayerClient;

    private STYLE mStyle;
    private OnItemClickListener<ITEM> mItemClickListener;

    private OnRebindCallback<BINDING> mOnRebindCallback;

    protected ItemRecyclerViewAdapter(Context context, LayerClient layerClient, String tag, boolean hasStableIds) {
        mContext = context;
        mLayerClient = layerClient;
        TAG = tag;
        setHasStableIds(hasStableIds);
        mLayoutInflater = LayoutInflater.from(context);
        mOnRebindCallback = new OnRebindCallback<BINDING>() {
            @Override
            public boolean onPreBind(BINDING binding) {
                return mRecyclerView != null && mRecyclerView.isComputingLayout();
            }

            @Override
            public void onCanceled(BINDING binding) {
                int childAdapterPosition = mRecyclerView.getChildAdapterPosition(binding.getRoot());
                if (childAdapterPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(childAdapterPosition, TAG);
                }
            }
        };
    }

    protected boolean hasNonDataBindingInvalidate(List<Object> payloads, Object validation) {
        for (Object payload : payloads) {
            if (payload != validation) {
                return true;
            }
        }
        return false;
    }

    //==============================================================================================
    // Public API
    //==============================================================================================

    /**
     * Set query
     */

    public void setQuery(Query<ITEM> query, Collection<String> updateAttributes) {
        mQueryController = mLayerClient.newRecyclerViewController(query, updateAttributes, this);
        mItems = null;
    }

    public void setItems(List<ITEM> items) {
        mItems = items;
        mQueryController = null;
        if (mRecyclerView != null) {
            notifyDataSetChanged();
        }

    }

    public void setItems(Set<ITEM> items) {
        List<ITEM> list = new ArrayList<>();
        list.addAll(items);
        setItems(list);
    }

    /**
     * Refreshes this adapter by re-running the underlying Query.
     */
    @CallSuper
    public void refresh() {
        if (mQueryController != null) {
            mQueryController.execute();
        }
    }

    /**
     * Bind an empty/null item. Typically for when the query controller is running a query
     * asynchronously
     */
    @CallSuper
    public void onBindEmpty(VIEW_HOLDER holder) {
        holder.setEmpty();
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public abstract void onDestroy();

    public STYLE getStyle() {
        return mStyle;
    }

    @CallSuper
    public void setStyle(STYLE style) {
        mStyle = style;
    }

    @Override
    public int getItemCount() {
        int itemCount;
        if (mQueryController != null) {
            itemCount = mQueryController.getItemCount();
        } else {
            itemCount = mItems.size();
        }
        return itemCount;
    }

    public Integer getPosition(ITEM item) {
        if (mQueryController != null) {
            return mQueryController.getPosition(item);
        } else {
            return mItems.indexOf(item);
        }
    }

    public Integer getPosition(ITEM item, int lastPosition) {
        if (mQueryController != null) {
            return mQueryController.getPosition(item, lastPosition);
        } else {
            return mItems.indexOf(item);
        }
    }

    public ITEM getItem(int position) {
        if (mQueryController != null) {
            return mQueryController.getItem(position);
        } else {
            return mItems.get(position);
        }
    }

    public ITEM getItem(ItemViewHolder<ITEM, VIEW_MODEL, BINDING, STYLE> viewHolder) {
        return viewHolder.getItem();
    }

    public void setItemClickListener(OnItemClickListener<ITEM> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public OnItemClickListener<ITEM> getItemClickListener() {
        return mItemClickListener;
    }

    //==============================================================================================
    // RecyclerView methods
    //==============================================================================================

    @Override
    public void onBindViewHolder(VIEW_HOLDER holder, int position, List<Object> payloads) {
        if (payloads.isEmpty() || hasNonDataBindingInvalidate(payloads, TAG)) {

            if (mQueryController != null) {
                mQueryController.updateBoundPosition(position);
            }

            ITEM item = getItem(position);
            if (item != null) {
                holder.setItem(item);
                holder.getBinding().executePendingBindings();
            } else {
                onBindEmpty(holder);
            }
        }
    }

    @Override
    public final void onBindViewHolder(VIEW_HOLDER holder, int position) {
        throw new IllegalArgumentException("Use onBindViewHolder(ItemViewHolder<ITEM, VIEW_MODEL, BINDING, STYLE, VIEW_HOLDER> holder, int position, List<Object> payloads) instead");
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
    }

    //==============================================================================================
    // RecyclerViewController.Callback callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        notifyDataSetChanged();

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryDataSetChanged");
        }
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        notifyItemChanged(position);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemChanged. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemRangeChanged. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        notifyItemInserted(position);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemInserted. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemRangeInserted. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        notifyItemRemoved(position);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemRemoved. Position: " + position);
        }
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemRangeRemoved. Position start: " + positionStart + " Count: " + itemCount);
        }
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);

        if (Log.isPerfLoggable()) {
            Log.perf(TAG + " - onQueryItemMoved. From: " + fromPosition + " To: " + toPosition);
        }
    }

    //==============================================================================================
    // Getters
    //==============================================================================================


    protected Context getContext() {
        return mContext;
    }

    protected LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    protected RecyclerViewController<ITEM> getQueryController() {
        return mQueryController;
    }

    protected List<ITEM> getItems() {
        return mItems;
    }

    protected LayerClient getLayerClient() {
        return mLayerClient;
    }

    protected OnRebindCallback<BINDING> getOnRebindCallback() {
        return mOnRebindCallback;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
}