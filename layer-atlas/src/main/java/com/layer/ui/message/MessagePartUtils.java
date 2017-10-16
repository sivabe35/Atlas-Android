package com.layer.ui.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagePartUtils {
    private static final Pattern PARAMETER_ROLE = Pattern.compile("\\s*role\\s*=\\s*.+\\s*;?");
    private static final Pattern PARAMETER_IS_ROOT = Pattern.compile(".*;\\s*role\\s*=\\s*root\\s*;?");

    @Nullable
    public static String getMimeType(MessagePart messagePart) {
        String mimeType = messagePart.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) return null;

        if (!mimeType.contains(";")) return mimeType;

        return mimeType.split(";")[0].trim();
    }

    @Nullable
    public static Map<String, String> getMimeTypeArguments(MessagePart messagePart) {
        String mimeType = messagePart.getMimeType();
        if (mimeType == null || mimeType.isEmpty() || !mimeType.contains(";")) return null;

        String[] split = mimeType.split(";");
        if (split.length < 2) return null;

        Map<String, String> arguments = new HashMap<>();
        for (int i = 1; i < split.length; i++) {
            String[] subsplit = split[i].split("=");
            arguments.put(subsplit[0].trim(), subsplit[1].trim());
        }

        return arguments;
    }

    @Nullable
    public static String getRole(@NonNull MessagePart messagePart) {
        String mimeType = messagePart.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) return null;

        Matcher matcher = PARAMETER_ROLE.matcher(mimeType);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static boolean isRoleRoot(@NonNull MessagePart messagePart) {
        String mimeType = messagePart.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) return false;

        return PARAMETER_IS_ROOT.matcher(mimeType).find();
    }

    public static String getRootMimeType(Message message) {
        for (MessagePart part : message.getMessageParts()) {
            if (isRoleRoot(part)) {
                return getMimeType(part);
            }
        }

        return null;
    }
}
