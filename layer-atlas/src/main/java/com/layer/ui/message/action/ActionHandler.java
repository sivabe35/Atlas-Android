package com.layer.ui.message.action;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

public abstract class ActionHandler {
    private  String mEvent;
    private JsonObject mData;

    public ActionHandler(String event, JsonObject data) {
        mEvent = event;
        mData = data;
    }

    public String getEvent() {
        return mEvent;
    }

    public JsonObject getData() {
        return mData;
    }

    public void setData(JsonObject data) {
        mData = data;
    }

    public abstract void performAction(@NonNull Context context);

    public void performAction(@NonNull Context context, @NonNull JsonObject customData) {
        // Default behavior is no-op
    }
}
