package com.layer.atlas.messagetypes.teamfit;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.layer.atlas.R;
import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.util.List;

/**
 * Created by archit on 2/2/17.
 */

public class SKUTimePickerCellFactory extends AtlasCellFactory<SKUTimePickerCellFactory.CellHolder, SKUTimePickerCellFactory.SKUTimePickerCellInfo> {

    public SKUTimePickerCellFactory() {
        super(256 * 1024);
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View v = layoutInflater.inflate(R.layout.sku_picker_cell, cellView, true);
        ((GradientDrawable) v.getBackground()).setColor(isMe ? mMessageStyle.getMyBubbleColor() : mMessageStyle.getOtherBubbleColor());

        return new CellHolder(v);
    }

    @Override
    public SKUTimePickerCellInfo parseContent(LayerClient layerClient, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";

        Gson gson = new Gson();
        return gson.fromJson(text, SKUTimePickerCellInfo.class);
    }

    @Override
    public void bindCellHolder(CellHolder cellHolder, SKUTimePickerCellInfo cached, Message message, CellHolderSpecs specs) {
        cellHolder.getOption1().setText(cached.getSkuTimes().get(0).getName());
        cellHolder.getOption1().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        cellHolder.getOption2().setText(cached.getSkuTimes().get(1).getName());
        cellHolder.getOption2().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        cellHolder.getOption3().setText(cached.getSkuTimes().get(2).getName());
        cellHolder.getOption3().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public boolean isType(Message message) {
        List<MessagePart> messageParts = message.getMessageParts();
        return messageParts.size() == 1 && messageParts.get(0).getMimeType().equals("application/sku-times+json");
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";

        return text;
    }

    public static class CellHolder extends AtlasCellFactory.CellHolder {
        Button option1, option2, option3;

        public CellHolder(View v) {
            option1 = (Button) v.findViewById(R.id.sku_picker_option_0);
            option2 = (Button) v.findViewById(R.id.sku_picker_option_1);
            option3 = (Button) v.findViewById(R.id.sku_picker_option_2);
        }

        public Button getOption1() {
            return option1;
        }

        public Button getOption2() {
            return option2;
        }

        public Button getOption3() {
            return option3;
        }
    }

    public static class SKUTimePickerCellInfo implements AtlasCellFactory.ParsedContent {
        @SerializedName("sku-times")
        private List<SKUTime> skuTimes;

        public SKUTimePickerCellInfo() {
        }

        @Override
        public int sizeOf() {
            return 0;
        }

        public List<SKUTime> getSkuTimes() {
            return skuTimes;
        }

        public void setSkuTimes(List<SKUTime> skuTimes) {
            this.skuTimes = skuTimes;
        }

        public static class SKUTime {
            int id;
            String name;

            public SKUTime() {
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }
}
