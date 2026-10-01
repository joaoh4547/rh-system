package com.rhsystem.interfaces.ui.pages.cache;

import java.util.Locale;

/**
 * Small helper to render byte counts in a human-readable form (B, KB, MB, GB).
 */
final class ByteFormat {

    private ByteFormat() {
    }

    static String humanReadable(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);
        return String.format(Locale.US, "%.2f %s", value, units[unit]);
    }
}
