package com.virtualcloset.app.ui;

import javafx.scene.paint.Color;

/**
 * ──────────────────────────────────────────────────────────────────
 *  THEME — edit ALL app colors in one place here.
 *  Switch between LIGHT and DARK by calling Theme.toggle().
 * ──────────────────────────────────────────────────────────────────
 */
public final class Theme {

    // ── Light palette ──────────────────────────────────────────────
    private static final String L_BG            = "#f6f4ff"; // window background
    private static final String L_CARD_BG       = "#ffffff"; // panel / card background
    private static final String L_CARD_BORDER   = "#ddd7f5"; // panel / card border
    private static final String L_ACCENT        = "#7a5cff"; // selected item ring, buttons
    private static final String L_ACCENT_BG     = "#efe8ff"; // selected item card fill
    private static final String L_ITEM_BG       = "#faf9ff"; // unselected item card fill
    private static final String L_CANVAS_BG     = "#ffffff"; // outfit canvas background
    private static final String L_TEXT_PRIMARY   = "#1a1a2e"; // headings
    private static final String L_TEXT_SECONDARY = "#5f5a73"; // subtitles / meta
    private static final String L_WARN          = "#cc6600"; // ⚠ re-export badge

    // ── Dark palette ───────────────────────────────────────────────
    private static final String D_BG            = "#12111a"; // window background
    private static final String D_CARD_BG       = "#1e1c2b"; // panel / card background
    private static final String D_CARD_BORDER   = "#3a3550"; // panel / card border
    private static final String D_ACCENT        = "#9d80ff"; // selected item ring, buttons
    private static final String D_ACCENT_BG     = "#2d2550"; // selected item card fill
    private static final String D_ITEM_BG       = "#27243a"; // unselected item card fill
    private static final String D_CANVAS_BG     = "#1a1826"; // outfit canvas background
    private static final String D_TEXT_PRIMARY   = "#f0eeff"; // headings
    private static final String D_TEXT_SECONDARY = "#a89ec5"; // subtitles / meta
    private static final String D_WARN          = "#ffaa44"; // ⚠ re-export badge

    // ── Active flag ────────────────────────────────────────────────
    private static boolean dark = false;

    private Theme() {}

    public static boolean isDark() { return dark; }

    public static void toggle() { dark = !dark; }

    // ── Color accessors ────────────────────────────────────────────
    public static Color bg()            { return c(dark ? D_BG            : L_BG); }
    public static Color cardBg()        { return c(dark ? D_CARD_BG       : L_CARD_BG); }
    public static Color cardBorder()    { return c(dark ? D_CARD_BORDER   : L_CARD_BORDER); }
    public static Color accent()        { return c(dark ? D_ACCENT        : L_ACCENT); }
    public static Color accentBg()      { return c(dark ? D_ACCENT_BG     : L_ACCENT_BG); }
    public static Color itemBg()        { return c(dark ? D_ITEM_BG       : L_ITEM_BG); }
    public static Color canvasBg()      { return c(dark ? D_CANVAS_BG     : L_CANVAS_BG); }
    public static Color textPrimary()   { return c(dark ? D_TEXT_PRIMARY   : L_TEXT_PRIMARY); }
    public static Color textSecondary() { return c(dark ? D_TEXT_SECONDARY : L_TEXT_SECONDARY); }
    public static Color warn()          { return c(dark ? D_WARN          : L_WARN); }

    // ── Inline-style helpers (for setStyle calls) ──────────────────
    public static String cardBgStyle()   { return "-fx-background-color:" + hex(cardBg()) + ";"; }
    public static String accentBgStyle() { return "-fx-background-color:" + hex(accentBg()) + ";"; }
    public static String itemBgStyle()   { return "-fx-background-color:" + hex(itemBg()) + ";"; }
    public static String textStyle()     { return "-fx-text-fill:" + hex(textPrimary()) + ";"; }
    public static String warnHex()       { return hex(warn()); }

    private static Color c(String hex) { return Color.web(hex); }
    public static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}
