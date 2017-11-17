package com.layer.ui.message.action;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class Action<CUSTOM_DATA> {
    private  String mEvent;
    private CUSTOM_DATA mData;

    public Action(String event, CUSTOM_DATA data) {
        mEvent = event;
        mData = data;
    }

    public String getEvent() {
        return mEvent;
    }

    public CUSTOM_DATA getData() {
        return mData;
    }

    public void setData(CUSTOM_DATA data) {
        mData = data;
    }

    public abstract void performAction(@NonNull Context context);

    public void performAction(@NonNull Context context, @NonNull CUSTOM_DATA customData) {
        // Default behavior is no-op
    }
}
