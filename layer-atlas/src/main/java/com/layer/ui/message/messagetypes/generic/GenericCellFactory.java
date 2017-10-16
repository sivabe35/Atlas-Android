package com.layer.ui.message.messagetypes.generic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.layer.ui.R;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

/**
 * Mime handles all MIME Types by simply displaying all MessagePart MIME Types as text.
 */
public class GenericCellFactory extends
        CellFactory<GenericCellFactory.CellHolder, GenericCellFactory.ParsedContent> {
    public GenericCellFactory() {
        super(256 * 1024);
    }

    public static String getPreview(Context context, Message message) {
        StringBuilder b = new StringBuilder();
        boolean isFirst = true;
        b.append("No cell factory registered - using GenericCellFactory\n[");
        if (message!=null) {
            for (MessagePart part : message.getMessageParts()) {
                if (!isFirst) b.append(", ");
                isFirst = false;
                b.append(part.getSize()).append("-byte ").append(part.getMimeType());
            }
        }
        b.append("]");
        return b.toString();
    }

    @Override
    public boolean isBindable(Message message) {
        return true;
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        Context context = cellView.getContext();

        View v = layoutInflater.inflate(R.layout.ui_message_item_cell_text, cellView, true);
        v.setBackgroundResource(isMe ? R.drawable.ui_message_item_cell_me : R.drawable.ui_message_item_cell_them);

        TextView t = (TextView) v.findViewById(R.id.cell_text);
        t.setTextColor(context.getResources().getColor(isMe ? R.color.layer_ui_text_white : R.color.layer_ui_text_black));
        return new CellHolder(v);
    }

    @Override
    public ParsedContent parseContent(LayerClient layerClient, Message message) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (MessagePart part : message.getMessageParts()) {
            if (i != 0) builder.append("\n");
            builder.append("[").append(i).append("]: ")
                    .append(part.getSize()).append("-byte `")
                    .append(part.getMimeType()).append("`");
            i++;
        }
        return new ParsedContent(builder.toString());
    }

    @Override
    public boolean isType(Message message) {
        return true;
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        return GenericCellFactory.getPreview(context, message);
    }

    @Override
    public void bindCellHolder(CellHolder cellHolder, ParsedContent string, Message message, CellHolderSpecs specs) {
        cellHolder.mTextView.setText(string.toString());
    }

    public class CellHolder extends CellFactory.CellHolder {
        TextView mTextView;

        public CellHolder(View view) {
            mTextView = (TextView) view.findViewById(R.id.cell_text);
        }
    }

    public static class ParsedContent implements CellFactory.ParsedContent {
        private final String mString;
        private final int mSize;

        public ParsedContent(String string) {
            mString = string;
            mSize = mString.getBytes().length;
        }

        public String getString() {
            return mString;
        }

        @Override
        public int sizeOf() {
            return mSize;
        }

        @Override
        public String toString() {
            return mString;
        }
    }
}
