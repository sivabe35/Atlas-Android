package com.layer.atlas.messagetypes.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;

/**
 * Created by archit on 1/30/17.
 */

public class DateTimeOptionPickerCellFactory extends AtlasCellFactory<DateTimeOptionPickerCellFactory.CellHolder, DateTimeOptionPickerCellFactory.DateTimeOptionPickerInfo> {

    public DateTimeOptionPickerCellFactory(int cacheBytes) {
        super(cacheBytes);
    }

    @Override
    public boolean isBindable(Message message) {
        return false;
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        return null;
    }

    @Override
    public DateTimeOptionPickerInfo parseContent(LayerClient layerClient, Message message) {
        return null;
    }

    @Override
    public void bindCellHolder(CellHolder cellHolder, DateTimeOptionPickerInfo cached, Message message, CellHolderSpecs specs) {

    }

    @Override
    public boolean isType(Message message) {
        return false;
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        return null;
    }

    public static class CellHolder extends AtlasCellFactory.CellHolder {

    }

    public static class DateTimeOptionPickerInfo implements AtlasCellFactory.ParsedContent {

        @Override
        public int sizeOf() {
            return 0;
        }
    }
}
