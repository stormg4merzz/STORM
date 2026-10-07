package com.stormpop;

/** The 7 STORM particle styles. Every name contains STORM. */
public enum StormStyle {
    BLUE("STORM Blue", 0x1E5BFF),       // pulse ring
    LIME("STORM Lime", 0x7CFF2B),       // spark burst
    GOLD("STORM Gold", 0xFFC400),       // streaks
    PINK("STORM Pink", 0xFF2E93),       // spiral
    TEAL("STORM Teal", 0x00E5C3),       // double shockwave
    WHITE("STORM White", 0xFFFFFF),     // lightning bolt
    RAINBOW("STORM Rainbow", 0xFF00FF); // multicolour burst

    public final String displayName;
    public final int color;

    StormStyle(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }
}
