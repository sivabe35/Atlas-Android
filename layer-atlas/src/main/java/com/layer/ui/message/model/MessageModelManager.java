package com.layer.ui.message.model;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MessageModelManager {
    private final Map<String, Constructor<?>> mIdentifierToConstructorMap;

    private Context mApplicationContext;
    private LayerClient mLayerClient;


    public MessageModelManager(Context applicationContext, LayerClient layerClient) {
        mIdentifierToConstructorMap = new HashMap<>();
        mApplicationContext = applicationContext;
        mLayerClient = layerClient;
    }

    public <T extends MessageModel> void registerModel(@NonNull String modelIdentifier, @NonNull Class<T> messageModelClass) {
        try {
            Constructor<?> constructor = messageModelClass.getConstructor(Context.class, LayerClient.class);
            mIdentifierToConstructorMap.put(modelIdentifier, constructor);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement required constructor");
        }
    }

    public boolean hasModel(@NonNull String modelIdentifier) {
        return mIdentifierToConstructorMap.containsKey(modelIdentifier);
    }

    public void remove(@NonNull String modelIdentifier) {
        if (mIdentifierToConstructorMap.containsKey(modelIdentifier)) {
            mIdentifierToConstructorMap.remove(modelIdentifier);
        }
    }

    public <T extends MessageModel> T getModel(@NonNull String modelIdentifier) {
        try {
            Constructor<T> constructor;
            if (mIdentifierToConstructorMap.containsKey(modelIdentifier)) {
                constructor = (Constructor<T>) mIdentifierToConstructorMap.get(modelIdentifier);
                return constructor.newInstance(mApplicationContext, mLayerClient);
            }
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (InvocationTargetException e) {
        }

        return null;
    }
}
