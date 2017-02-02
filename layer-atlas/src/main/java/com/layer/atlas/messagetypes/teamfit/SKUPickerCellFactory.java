package com.layer.atlas.messagetypes.teamfit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.layer.atlas.R;
import com.layer.atlas.messagetypes.AtlasCellFactory;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archit on 2/1/17.
 */

public class SKUPickerCellFactory extends AtlasCellFactory<SKUPickerCellFactory.CellHolder, SKUPickerCellFactory.SKUPickerCellInfo> {

    protected LayerClient mLayerClient;

    public SKUPickerCellFactory(LayerClient layerClient) {
        super(256 * 1024);
        this.mLayerClient = layerClient;
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View v = layoutInflater.inflate(R.layout.sku_picker_cell, cellView, true);
        //((GradientDrawable) v.getBackground()).setColor(isMe ? mMessageStyle.getMyBubbleColor() : mMessageStyle.getOtherBubbleColor());

        return new CellHolder(v);
    }

    @Override
    public SKUPickerCellInfo parseContent(LayerClient layerClient, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";

        SKUPickerCellInfo info = new SKUPickerCellInfo();
        List<SKUPickerCellInfo.SKUPickerCell> skus = new ArrayList<>();

        skus.add(new SKUPickerCellInfo.SKUPickerCell(123, "Crossfit"));
        skus.add(new SKUPickerCellInfo.SKUPickerCell(456, "Olympic Weightlifting"));
        skus.add(new SKUPickerCellInfo.SKUPickerCell(789, "Beginners Introduction"));

        info.setSkus(skus);
        return info;
    }

    @Override
    public void bindCellHolder(final CellHolder cellHolder, SKUPickerCellInfo cached, final Message message, CellHolderSpecs specs) {
        cellHolder.getOption1().setText(cached.getSkus().get(0).getName());
        cellHolder.getOption1().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = cellHolder.getOption1().getText().toString();
                MessagePart part = mLayerClient.newMessagePart(text);
                PushNotificationPayload pushNotificationPayload = new PushNotificationPayload.Builder().text(text).build();
                Message reply = mLayerClient.newMessage(new MessageOptions().defaultPushNotificationPayload(pushNotificationPayload), part);
                message.getConversation().send(reply);
            }
        });

        cellHolder.getOption2().setText(cached.getSkus().get(1).getName());
        cellHolder.getOption2().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = cellHolder.getOption2().getText().toString();
                MessagePart part = mLayerClient.newMessagePart(text);
                PushNotificationPayload pushNotificationPayload = new PushNotificationPayload.Builder().text(text).build();
                Message reply = mLayerClient.newMessage(new MessageOptions().defaultPushNotificationPayload(pushNotificationPayload), part);
                message.getConversation().send(reply);
            }
        });

        cellHolder.getOption3().setText(cached.getSkus().get(2).getName());
        cellHolder.getOption3().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = cellHolder.getOption3().getText().toString();
                MessagePart part = mLayerClient.newMessagePart(text);
                PushNotificationPayload pushNotificationPayload = new PushNotificationPayload.Builder().text(text).build();
                Message reply = mLayerClient.newMessage(new MessageOptions().defaultPushNotificationPayload(pushNotificationPayload), part);
                message.getConversation().send(reply);
            }
        });
    }

    @Override
    public boolean isType(Message message) {
        List<MessagePart> messageParts = message.getMessageParts();
        MessagePart part = messageParts.get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";
        return messageParts.size() == 1 && messageParts.get(0).getMimeType().equals("text/plain") && text.equals("[application/skus+json]");
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
        TextView titleText;

        public CellHolder(View v) {
            option1 = (Button) v.findViewById(R.id.sku_picker_option_0);
            option2 = (Button) v.findViewById(R.id.sku_picker_option_1);
            option3 = (Button) v.findViewById(R.id.sku_picker_option_2);
            titleText = (TextView) v.findViewById(R.id.card_title);
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
            int size = 0;
            for (SKUPickerCell cell : skus) {
                size += Integer.SIZE + cell.name.getBytes().length;
            }
            return size;
        }

        public static class SKUPickerCell {
            private int id;
            private String name;

            public SKUPickerCell() {
            }

            public SKUPickerCell(int id, String name) {
                this.id = id;
                this.name = name;
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
