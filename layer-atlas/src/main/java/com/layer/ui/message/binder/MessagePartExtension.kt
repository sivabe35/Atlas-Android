@file:JvmName("MessagePartUtilities")

package com.layer.ui.message.binder

import com.layer.sdk.messaging.MessagePart

private val parameterRoleRegex = "role=.+;?".toRegex()
private val parameterIsRootRegex = ".*;role=root;?".toRegex()

fun MessagePart.role(): String? {
    return parameterRoleRegex.find(mimeType)?.value
}

fun MessagePart.isRoot(): Boolean {
    return parameterIsRootRegex.matches(mimeType)
}