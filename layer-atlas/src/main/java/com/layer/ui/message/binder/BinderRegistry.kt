package com.layer.ui.message.binder

import android.support.annotation.NonNull
import com.layer.sdk.LayerClient
import com.layer.sdk.messaging.Identity
import com.layer.sdk.messaging.Message
import com.layer.ui.message.MessageCell
import com.layer.ui.message.messagetypes.CellFactory

class BinderRegistry(@NonNull client: LayerClient) {

    private val layerClient = client
    private var authenticatedUser: Identity? = client.authenticatedUser

    /**
     * Number of permissible CellFactory view type cells, including my cell and their cell per type
     */
    private val numberOfLegacyViewTypes = 1000

    public val viewTypeUnkown = Int.MIN_VALUE
    public val viewTypeHeader = viewTypeUnkown + 1
    public val viewTypeFooter = Int.MAX_VALUE

    public val viewTypeLegacyStart: Int by lazy { Math.max(viewTypeHeader, viewTypeFooter) + 1 }
    public val viewTypeLegacyEnd: Int by lazy { viewTypeLegacyStart + numberOfLegacyViewTypes }
    public val viewTypeCard: Int by lazy { viewTypeLegacyEnd + 1 }

    private lateinit var cellTypesByViewType: LinkedHashMap<Int, MessageCell>

    // Legacy Message binding with CellFactory and MessageCells
    public var cellFactories: MutableList<CellFactory<*, *>>? = null
        /**
         * Registers one or more CellFactories for the MessagesAdapter to manage.  CellFactories
         * know which Messages they can render, and handle View caching, creation, and mBinding.
         *
         * @param factories Cells to register.
         */
        set(@NonNull factories) {
            if (factories!!.size * 2 > viewTypeLegacyEnd) throw IllegalArgumentException("Too many cell factories. " +
                    "Cannot support more than " + numberOfLegacyViewTypes / 2)


            cellTypesByViewType = LinkedHashMap()

            var startIndex = viewTypeLegacyStart

            factories.forEach { cellFactory ->
                cellTypesByViewType.put(startIndex++, MessageCell(true, cellFactory))
                cellTypesByViewType.put(startIndex++, MessageCell(false, cellFactory))
            }
        }

    public fun isLegacyMessageType(message: Message): Boolean {
        return message.messageParts.none({ it.isRoleRoot() })
    }

    public fun getMessageCellForViewType(viewType: Int): MessageCell? {
        return cellTypesByViewType.get(viewType)
    }

    public fun getViewType(message: Message): Int {
        return if (isLegacyMessageType(message)) {
            cellTypesByViewType.filter { (_, messageCell) -> messageCell.mCellFactory.isBindable(message) && messageCell.mMe == message.sender?.equals(authenticatedUser) }
                    .entries
                    .firstOrNull()
                    ?.key ?: viewTypeUnkown

        } else {
            viewTypeCard
        }
    }

    public fun getCellFactory(message: Message): CellFactory<*, *>? {
        return cellFactories?.firstOrNull { it.isBindable(message) }
    }

    public fun cacheContent(message: Message) {
        if (isLegacyMessageType(message)) {
            getCellFactory(message)?.getParsedContent(layerClient, message)
        }
    }

    public fun notifyScrollStateChange(newState: Int) {
        cellFactories?.forEach { it.onScrollStateChanged(newState) }
    }

}