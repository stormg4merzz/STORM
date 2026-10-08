package com.stormpop;

/** The 7 STORM particle styles. Every name contains STORM. */
public enum StormStyle {
    BLUE("STORM Blue", 0x1E5BFF),    // pulse rings + blue soul fire
    LIME("STORM Lime", 0x7CFF2B),    // spark burst
    GOLD("STORM Gold", 0xFFC400),    // streaks
    PINK("STORM Pink", 0xFF2E93),    // spiral
    TEAL("STORM Teal", 0x00E5C3),    // shockwave rings
    WHITE("STORM White", 0xFFFFFF),  // lightning
    FIRE("STORM Fire", 0xFF6A00);    // real fire

    public final String displayName;
    public final int color;

    StormStyle(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }
}
