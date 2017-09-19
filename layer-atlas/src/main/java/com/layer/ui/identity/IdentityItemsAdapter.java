package com.layer.ui.identity;

import android.content.Context;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.databinding.UiFourPartItemBinding;
import com.layer.ui.style.FourPartItemStyle;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import com.layer.ui.fourpartitem.FourPartItemViewHolder;

public class IdentityItemsAdapter extends ItemRecyclerViewAdapter<Identity, IdentityItemViewModel,
        UiFourPartItemBinding, FourPartItemStyle, FourPartItemViewHolder<Identity, IdentityItemViewModel>> {

    protected static final String TAG = "IdentityItemsAdapter";

    protected IdentityFormatter mIdentityFormatter;
    protected DateFormatter mDateFormatter;
    protected ImageCacheWrapper mImageCacheWrapper;

    public IdentityItemsAdapter(Context context, LayerClient layerClient,
                                ImageCacheWrapper imageCacheWrapper, IdentityFormatter identityFormatter,
                                DateFormatter dateFormatter) {
        super(context, layerClient, TAG, false);
        mIdentityFormatter = identityFormatter;
        mDateFormatter = dateFormatter;
        mImageCacheWrapper = imageCacheWrapper;
    }

    @Override
    public FourPartItemViewHolder<Identity, IdentityItemViewModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        UiFourPartItemBinding binding = UiFourPartItemBinding.inflate(getLayoutInflater(), parent, false);
        IdentityItemViewModel viewModel = new IdentityItemViewModel();
        viewModel.setItemClickListener(getItemClickListener());
        viewModel.setIdentityFormatter(mIdentityFormatter);
        viewModel.setDateFormatter(mDateFormatter);
        FourPartItemViewHolder<Identity, IdentityItemViewModel> viewHolder = new FourPartItemViewHolder<>(binding, viewModel, getStyle(), mImageCacheWrapper, mIdentityFormatter);

        binding.addOnRebindCallback(mOnRebindCallback);

        return viewHolder;
    }

    @Override
    public void onBindEmpty(FourPartItemViewHolder<Identity, IdentityItemViewModel> holder) {
        super.onBindEmpty(holder);
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public void onDestroy() {
        // NO OP
    }
}
