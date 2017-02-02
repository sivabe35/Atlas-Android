package com.layer.atlas.messagetypes.teamfit;

import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.util.List;

/**
 * Created by archit on 2/2/17.
 */

public class FrequencyOptionsPickerCellFactory extends SKUPickerCellFactory{

    @Override
    public boolean isBindable(Message message) {
        return this.isType(message);
    }

    @Override
    public boolean isType(Message message) {
        List<MessagePart> messageParts = message.getMessageParts();
        return messageParts.size() == 1 && messageParts.get(0).getMimeType().equals("application/frequency-options+json");
    }
}
