package com.layer.ui.message.action;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;

public abstract class ActionHandler {
    private  String mEvent;
    private LayerClient mLayerClient;

    public ActionHandler(LayerClient layerClient, String event) {
        mEvent = event;
    }

    public String getEvent() {
        return mEvent;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    public void performAction(@NonNull Context context, JsonObject data) {
        // Default behavior is no-op
    }
}
