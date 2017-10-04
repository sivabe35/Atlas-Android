package com.layer.ui.message.binder

import com.layer.sdk.messaging.MessagePart

private val argumentSeparator = ";"

private val parameterSeparator = "="
private val parameterRole = "role"
private val parameterValueRoot = "root"

fun MessagePart.getRole(): String? {

    if (mimeType.contains(parameterRole)) {
        for (part: String in mimeType.split(argumentSeparator)) {
            if (part.startsWith(parameterRole)) {
                return part.split(parameterSeparator)[1]
            }
        }
    }

    return null
}

fun MessagePart.isRoleRoot(): Boolean {
    return getRole()?.equals(parameterValueRoot) ?: false
}