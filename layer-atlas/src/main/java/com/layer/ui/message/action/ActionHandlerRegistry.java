package com.layer.ui.message.action;

import android.content.Context;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ActionHandlerRegistry {
    private static Map<String, ActionHandler> sActionHandlers;

    static {
        sActionHandlers = new HashMap<>();
    }

    public static void registerHandler(ActionHandler handler) {
        sActionHandlers.put(handler.getEvent(), handler);
    }

    public static void dispatchEvent(Context context, String event, JsonObject customData) throws UnsupportedOperationException {
        if (sActionHandlers.containsKey(event)) {
            sActionHandlers.get(event).performAction(context, customData);
        } else {
            throw new UnsupportedOperationException("No registered action handler for event: " + event);
        }
    }
}
