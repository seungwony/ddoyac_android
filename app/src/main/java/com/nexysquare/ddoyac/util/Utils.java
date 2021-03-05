package com.nexysquare.ddoyac.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.Normalizer;

public class Utils {
    public static boolean showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.showSoftInput(view, 0);
        }
        return false;
    }

    public static boolean hideInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return false;
    }
    public static String normalizeNfc(String unNormalMailBoxName) {
        if (!Normalizer.isNormalized(unNormalMailBoxName, Normalizer.Form.NFC)) {
            return Normalizer.normalize(unNormalMailBoxName, Normalizer.Form.NFC);
        }
        return unNormalMailBoxName;
    }
}
