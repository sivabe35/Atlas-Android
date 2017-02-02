package com.layer.atlas.messagetypes.teamfit;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.layer.atlas.R;
import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.util.List;

/**
 * Created by archit on 2/1/17.
 */

public class SKUPickerCellFactory extends AtlasCellFactory<SKUPickerCellFactory.CellHolder, SKUPickerCellFactory.SKUPickerCellInfo> {

    public SKUPickerCellFactory() {
        super(256 * 1024);
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View v = layoutInflater.inflate(R.layout.sku_picker_cell, cellView, true);
        v.setBackgroundResource(isMe ? R.drawable.atlas_message_item_cell_me : R.drawable.atlas_message_item_cell_them);
        ((GradientDrawable) v.getBackground()).setColor(isMe ? mMessageStyle.getMyBubbleColor() : mMessageStyle.getOtherBubbleColor());

        return new CellHolder(v);
    }

    @Override
    public SKUPickerCellInfo parseContent(LayerClient layerClient, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";
        String name;
        Identity sender = message.getSender();

        if (sender != null) {
            name = Util.getDisplayName(sender) + ": ";
        } else {
            name = "";
        }

        Gson gson = new Gson();
        return gson.fromJson(text, SKUPickerCellInfo.class);
    }

    @Override
    public void bindCellHolder(CellHolder cellHolder, SKUPickerCellInfo cached, Message message, CellHolderSpecs specs) {
        cellHolder.getOption1().setText(cached.getSkus().get(0).getName());
        cellHolder.getOption1().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        cellHolder.getOption2().setText(cached.getSkus().get(1).getName());
        cellHolder.getOption2().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        cellHolder.getOption3().setText(cached.getSkus().get(2).getName());
        cellHolder.getOption3().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public boolean isType(Message message) {
        List<MessagePart> messageParts = message.getMessageParts();
        return messageParts.size() == 1 && messageParts.get(0).getMimeType().equals("application/skus+json");
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        if (isType(message)) {
            MessagePart part = message.getMessageParts().get(0);
            // For large text content, the MessagePart may not be downloaded yet.
            return part.isContentReady() ? new String(part.getData()) : "";
        } else {
            throw new IllegalArgumentException("Message is not of the correct type - Text");
        }
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

    public static class SKUPickerCellInfo implements AtlasCellFactory.ParsedContent {
        private List<SKUPickerCell> skus;

        public SKUPickerCellInfo() {
        }

        public List<SKUPickerCell> getSkus() {
            return skus;
        }

        public void setSkus(List<SKUPickerCell> skus) {
            this.skus = skus;
        }

        @Override
        public int sizeOf() {
            return 0;
        }

        public static class SKUPickerCell {
            private int id;
            private String name;

            public SKUPickerCell() {
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
