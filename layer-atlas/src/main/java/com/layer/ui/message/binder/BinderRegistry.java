package com.layer.ui.message.binder;

import android.content.Context;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.message.MessageCell;
import com.layer.ui.message.MessagePartUtils;
import com.layer.ui.message.image.ImageMessageModel;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.model.MessageModelManager;
import com.layer.ui.message.text.TextMessageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinderRegistry {

    /**
     * Number of permissible CellFactory view type cells, including my cell and their cell per type
     */
    public final int NUMBER_OF_LEGACY_VIEW_TYPES = 10000;

    public final int VIEW_TYPE_UNKNOWN;
    public final int VIEW_TYPE_HEADER;
    public final int VIEW_TYPE_FOOTER;

    public final int VIEW_TYPE_LEGACY_START;
    public final int VIEW_TYPE_LEGACY_END;
    public final int VIEW_TYPE_CARD;

    protected LayerClient mLayerClient;

    // Legacy Message binding with CellFactory and MessageCells
    protected final List<CellFactory> mCellFactories;
    protected final Map<Integer, MessageCell> mCellTypesByViewType;
    protected final Map<CellFactory, Integer> mMyViewTypesByCell;
    protected final Map<CellFactory, Integer> mTheirViewTypesByCell;

    // XDK Message Type Registry
    protected final MessageModelManager mMessageModelManager;

    public BinderRegistry(@NonNull Context context, LayerClient layerClient) {
        this(context, layerClient, 0, 1, -1);
    }

    public BinderRegistry(@NonNull Context context, @NonNull LayerClient layerClient, final int headerViewType, final int footerViewType, final int unknownViewType) {
        mLayerClient = layerClient;

        mCellFactories = new ArrayList<>();
        mCellTypesByViewType = new HashMap<>();
        mMyViewTypesByCell = new HashMap<>();
        mTheirViewTypesByCell = new HashMap<>();

        if ((headerViewType == footerViewType)
                || (headerViewType == unknownViewType)
                || (footerViewType == unknownViewType)) {
            throw new IllegalArgumentException("Header, Footer and Unknown View Types must be distinct");
        }

        if (unknownViewType > headerViewType || unknownViewType > footerViewType) {
            throw new IllegalArgumentException("Please use a lower integer value than headerViewType or footerViewtype for unknownViewType ");
        }

        VIEW_TYPE_UNKNOWN = unknownViewType;
        VIEW_TYPE_HEADER = headerViewType;
        VIEW_TYPE_FOOTER = footerViewType;
        VIEW_TYPE_LEGACY_START = Math.max(headerViewType, footerViewType) + 1;
        VIEW_TYPE_LEGACY_END = VIEW_TYPE_LEGACY_START + NUMBER_OF_LEGACY_VIEW_TYPES;
        VIEW_TYPE_CARD = VIEW_TYPE_LEGACY_END + 1;

        mMessageModelManager = new MessageModelManager(context.getApplicationContext(), layerClient);
        initMessageTypeModelRegistry();
    }

    public boolean isLegacyMessageType(Message message) {
        for (MessagePart messagePart : message.getMessageParts()) {
            if (MessagePartUtils.isRoleRoot(messagePart)) return false;
        }
        return true;
    }

    public int getViewType(Message message) {
        if (!isLegacyMessageType(message)) {
            String rootMimeType = MessagePartUtils.getRootMimeType(message);
            if (mMessageModelManager.hasModel(rootMimeType)) {
                return rootMimeType.hashCode();
            }
        }

        Identity authenticatedUser = mLayerClient.getAuthenticatedUser();
        boolean isMe = authenticatedUser != null && authenticatedUser.equals(message.getSender());
        for (CellFactory factory : mCellFactories) {
            if (!factory.isBindable(message)) continue;
            return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
        }

        return VIEW_TYPE_UNKNOWN;
    }


    //==============================================================================================
    //  Legacy Bindings/CellFactory related methods
    //==============================================================================================

    public CellFactory getCellFactory(Message message) {
        for (CellFactory factory : mCellFactories) {
            if (factory.isBindable(message)) {
                return factory;
            }
        }

        return null;
    }

    public void cacheContent(Message message) {
        if (isLegacyMessageType(message)) {
            CellFactory cellFactory = getCellFactory(message);
            if (cellFactory != null) {
                cellFactory.getParsedContent(mLayerClient, message);
            }
        }
    }

    public void notifyScrollStateChange(int newState) {
        for (CellFactory factory : mCellFactories) {
            factory.onScrollStateChanged(newState);
        }
    }

    /**
     * Registers one or more CellFactories for the MessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and mBinding.
     *
     * @param cellFactories Cells to register.
     */
    public void setCellFactories(List<CellFactory> cellFactories) {
        mCellFactories.clear();
        mCellTypesByViewType.clear();
        mMyViewTypesByCell.clear();
        mTheirViewTypesByCell.clear();

        if (cellFactories.size() * 2 > VIEW_TYPE_LEGACY_END) {
            throw new IllegalArgumentException("Too many cell factories. " +
                    "Cannot support more than " + NUMBER_OF_LEGACY_VIEW_TYPES / 2);
        }

        int viewTypeCounter = VIEW_TYPE_LEGACY_START;

        for (CellFactory cellFactory : cellFactories) {
            mCellFactories.add(cellFactory);

            viewTypeCounter++;
            MessageCell me = new MessageCell(true, cellFactory);
            mCellTypesByViewType.put(viewTypeCounter, me);
            mMyViewTypesByCell.put(cellFactory, viewTypeCounter);

            viewTypeCounter++;
            MessageCell notMe = new MessageCell(false, cellFactory);
            mCellTypesByViewType.put(viewTypeCounter, notMe);
            mTheirViewTypesByCell.put(cellFactory, viewTypeCounter);
        }
    }

    public MessageCell getMessageCellForViewType(int viewType) {
        return mCellTypesByViewType.get(viewType);
    }

    //==============================================================================================
    //  XDK Message Binding related methods
    //==============================================================================================

    private void initMessageTypeModelRegistry() {
        mMessageModelManager.registerModel(TextMessageModel.ROOT_MIME_TYPE, TextMessageModel.class);
        mMessageModelManager.registerModel(ImageMessageModel.ROOT_MIME_TYPE, ImageMessageModel.class);
    }

    public <T extends MessageModel> void registerModel(@NonNull String modelIdentifier, @NonNull Class<T> messageModelClass) {
        mMessageModelManager.registerModel(modelIdentifier, messageModelClass);
    }

    public boolean hasModel(@NonNull String modelIdentifier) {
        return mMessageModelManager.hasModel(modelIdentifier);
    }

    public void remove(@NonNull String modelIdentifier) {
        mMessageModelManager.remove(modelIdentifier);
    }

    public MessageModelManager getMessageModelManager() {
        return mMessageModelManager;
    }
}
