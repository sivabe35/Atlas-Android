package com.layer.ui.message;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.message.messagetypes.CellFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindingRegistry {

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
    protected final SparseArray<MessageCell> mCellTypesByViewType;
    protected final Map<CellFactory, Integer> mMyViewTypesByCell;
    protected final Map<CellFactory, Integer> mTheirViewTypesByCell;

    public BindingRegistry(LayerClient layerClient) {
        this(layerClient, 0, 1, -1);
    }

    public BindingRegistry(@NonNull LayerClient layerClient, final int headerViewType, final int footerViewType, final int unknownViewType) {
        mLayerClient = layerClient;
        mCellFactories = new ArrayList<>();
        mCellTypesByViewType = new SparseArray<>();
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
    }

    protected boolean isLegacyMessageType(Message message) {
        return true;
    }

    public int getViewType(Message message) {
        if (isLegacyMessageType(message)) {
            Identity authenticatedUser = mLayerClient.getAuthenticatedUser();
            boolean isMe = authenticatedUser != null && authenticatedUser.equals(message.getSender());
            for (CellFactory factory : mCellFactories) {
                if (!factory.isBindable(message)) continue;
                return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
            }
        } else {
            return VIEW_TYPE_CARD;
        }

        return VIEW_TYPE_UNKNOWN;
    }

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
}
