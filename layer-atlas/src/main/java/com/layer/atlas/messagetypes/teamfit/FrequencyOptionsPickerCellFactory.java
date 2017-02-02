package com.layer.atlas.messagetypes.teamfit;

import android.view.View;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archit on 2/2/17.
 */

public class FrequencyOptionsPickerCellFactory extends SKUPickerCellFactory{

    public FrequencyOptionsPickerCellFactory(LayerClient layerClient) {
        super(layerClient);
    }

    @Override
    public boolean isBindable(Message message) {
        return this.isType(message);
    }

    @Override
    public SKUPickerCellInfo parseContent(LayerClient layerClient, Message message) {
        MessagePart part = message.getMessageParts().get(0);
        String text = part.isContentReady() ? new String(part.getData()) : "";

        SKUPickerCellInfo info = new SKUPickerCellInfo();
        List<SKUPickerCellInfo.SKUPickerCell> skuFrequencies = new ArrayList<>();

        skuFrequencies.add(new SKUPickerCellInfo.SKUPickerCell(100, "Single class for $25"));
        skuFrequencies.add(new SKUPickerCellInfo.SKUPickerCell(200, "Pack of five for $100"));
        skuFrequencies.add(new SKUPickerCellInfo.SKUPickerCell(300, "Subscribe monthly for $200"));

        info.setSkus(skuFrequencies);
        return info;
    }

    @Override
    public void bindCellHolder(final CellHolder cellHolder, SKUPickerCellInfo cached, final Message message, CellHolderSpecs specs) {
        cellHolder.getOption1().setText(cached.getSkus().get(0).getName());

        cellHolder.titleText.setText("Ok great! Pick below if you would like to schedule a single class, a pack of 5, or start joining us all the time and subscribe monthly.");

        cellHolder.getOption1().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = cellHolder.getOption1().getText().toString().split("for")[0].trim();
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
                String text = cellHolder.getOption2().getText().toString().split("for")[0].trim();
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
                String text = cellHolder.getOption3().getText().toString().split("for")[0].trim();
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
        return messageParts.size() == 1 && messageParts.get(0).getMimeType().equals("text/plain") && text.equals("[application/frequency-options+json]");
    }
}
