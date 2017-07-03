package com.layer.ui.avatar;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.util.imagecache.BitmapWrapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Avatar interface exposes the interaction between the AvatarView and AvatarViewModel Avatar.
 * @see Avatar.ViewModel is implemented by
 * @see AvatarViewModel and
 * @see Avatar.View is implemented by
 * @see AvatarView
 **/

public interface Avatar {

    interface ViewModel {

        //Loop through the participants and calls setClusterSizes and revalidateView() on the View
        void update();

        //Set the participants in the ViewModel and then call update on the ViewModel
        void setParticipants(Identity[] participants);

        void setParticipants(Set<Identity> participants);

        Set<Identity> getParticipants();

        //Get the number of Avatar to be drawn in the View onDraw
        int getInitialSize();

        Set<Map.Entry<Identity, String>> getEntrySet();

        //BitmapWrapper is an interface that wraps the bitmap
        BitmapWrapper getImageTarget(Identity key);

        //setClusterSizes is called in the View onLayout which in turns call the corresponding method on the View
        void setClusterSizes();

         //loadImage Work with Image Caching Library to provide Bitmap to the View
        void loadImage(String url, String tag, int width, int height, BitmapWrapper bitmapWrapper, Object... args);

        //Set the view on the ViewViewModel
        void setView(Avatar.View avatar);

        //Set custom AvatarInitial on the ViewModel to allow client plug in their custom Initials
        void setAvatarInitials(AvatarInitials avatarInitials);

        void setMaximumAvatar(int maximumAvatar);
    }

    interface View {

        boolean setClusterSizes(Map<Identity, String> initials, List<BitmapWrapper> pendingLoads);

        void revalidateView();
    }
}
