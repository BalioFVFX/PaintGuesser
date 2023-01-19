package com.paintguesser;

import androidx.annotation.Nullable;

import java.io.Closeable;

public class Utils {
    public static void closeCloseable(@Nullable final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
