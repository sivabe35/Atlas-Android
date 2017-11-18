package com.layer.ui.message.model;

import com.google.gson.JsonObject;

public class Action {
    private String mEvent;
    private JsonObject mData;

    public Action(String event) {
        mEvent = event;
        mData = new JsonObject();
    }

    public String getEvent() {
        return mEvent;
    }

    public void setEvent(String event) {
        mEvent = event;
    }

    public JsonObject getData() {
        return mData;
    }

    public void setData(JsonObject data) {
        mData = data;
    }
}
