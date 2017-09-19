package com.layer.ui.identity;

import com.layer.sdk.messaging.Identity;

public interface IdentityFormatter {
    /**
     * Implement to provide appropriately formatted intials for an Identity
     *
     * @see IdentityFormatterImpl for a sample implementation
     */
    String getInitials(Identity identity);

    String getFirstName(Identity identity);

    String getLastName(Identity identity);

    String getDisplayName(Identity identity);

    String getMetaData(Identity identity);

    String getUnknownNameString();
}
