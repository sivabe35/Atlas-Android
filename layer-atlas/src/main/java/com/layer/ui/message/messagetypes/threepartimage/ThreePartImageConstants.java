package com.layer.ui.message.messagetypes.threepartimage;

public interface ThreePartImageConstants {
    int ORIENTATION_0 = 0;
    int ORIENTATION_180 = 1;
    int ORIENTATION_90 = 2;
    int ORIENTATION_270 = 3;

    String MIME_TYPE_PREVIEW = "image/jpeg+preview";
    String MIME_TYPE_INFO = "application/json+imageSize";

    int PART_INDEX_FULL = 0;
    int PART_INDEX_PREVIEW = 1;
    int PART_INDEX_INFO = 2;
}
