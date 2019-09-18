package com.edmobilelabs.tts.Utils;

import android.view.ViewGroup;
import android.widget.ToggleButton;

/**
 * Created by Rounak Khandeparkar on 2019-09-18.
 */
public final class ViewUtils {

    private ViewUtils() {
    }

    public static void toggleButtonCheckedStatus(ViewGroup parent, boolean isChecked) {
        if (parent == null) {
            return;
        }


        for (int i = 0; i < parent.getChildCount(); i++) {
            ToggleButton tb = (ToggleButton) parent.getChildAt(i);
            tb.setChecked(isChecked);
        }
    }

    public static void toggleButtonCheckedStatusExcept(ViewGroup parent, int viewIdToIgnore, boolean isChecked) {
        if (parent == null) {
            return;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            ToggleButton tb = (ToggleButton) parent.getChildAt(i);
            if (tb.getId() != viewIdToIgnore) {
                tb.setChecked(isChecked);
            }
        }
    }
}
