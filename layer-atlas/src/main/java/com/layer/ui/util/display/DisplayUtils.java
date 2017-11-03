package com.layer.ui.util.display;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DisplayUtils {
    private static DisplayMetrics sDisplayMetrics = Resources.getSystem().getDisplayMetrics();

    public static int dpToPx(int dp) {
        return (int) (dp * sDisplayMetrics.density);
    }

    public static int pxToDp(int px) {
        return (int) (px / sDisplayMetrics.density);
    }
}
