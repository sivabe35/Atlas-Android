package com.layer.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.query.Queryable;
import com.layer.ui.style.ItemStyle;
import com.layer.ui.viewmodel.ItemViewModel;

public class ItemViewHolder<ITEM extends Queryable, VIEW_MODEL extends ItemViewModel<ITEM>, BINDING extends ViewDataBinding, STYLE extends ItemStyle>
        extends RecyclerView.ViewHolder {

    protected VIEW_MODEL mViewModel;
    protected STYLE mStyle;
    protected BINDING mBinding;
    protected LayoutInflater mLayoutInflater;

    public ItemViewHolder(BINDING binding, VIEW_MODEL viewModel) {
        super(binding.getRoot());
        mBinding = binding;
        mViewModel = viewModel;
        mLayoutInflater = LayoutInflater.from(binding.getRoot().getContext());
    }

    public ItemViewHolder(ViewGroup parent, @LayoutRes int layoutId, VIEW_MODEL viewModel) {
        this((BINDING) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false), viewModel);
    }

    public BINDING getBinding() {
        return mBinding;
    }

    protected void setBinding(BINDING binding) {
        mBinding = binding;
    }

    @CallSuper
    public void setItem(ITEM item) {
        mViewModel.setItem(item);

        getBinding().getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (getViewModel().getItemClickListener() != null) {
                    return getViewModel().getItemClickListener().onItemLongClick(getItem());
                }

                return false;
            }
        });
    }

    @CallSuper
    public void setEmpty() {

    }

    public ITEM getItem() {
        return mViewModel.getItem();
    }

    public STYLE getStyle() {
        return mStyle;
    }

    @CallSuper
    public void setStyle(STYLE style) {
        mStyle = style;
    }

    public VIEW_MODEL getViewModel() {
        return mViewModel;
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }
}
