package com.stormpop;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class StormConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("storm_pop_particles.properties");

    public static volatile boolean enabled = true;
    public static volatile StormStyle style = StormStyle.BLUE;
    /** 1 = light, 2 = normal, 3 = heavy */
    public static volatile int intensity = 2;
    /** how long particles stay: 1, 2 or 3 seconds */
    public static volatile int lifeSeconds = 1;

    private StormConfig() {}

    /** Spread of the effect in blocks: Low = 1, Normal = 2.5, High = 3.5. */
    public static double reach() {
        return switch (intensity) { case 1 -> 1.0; case 3 -> 3.5; default -> 2.5; };
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Properties p = new Properties();
            p.load(r);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            intensity = Math.max(1, Math.min(3, Integer.parseInt(p.getProperty("intensity", "2"))));
            lifeSeconds = Math.max(1, Math.min(3, Integer.parseInt(p.getProperty("life", "1"))));
            try {
                style = StormStyle.valueOf(p.getProperty("style", "BLUE").trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                style = StormStyle.BLUE;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[STORM] could not read config: " + e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            Properties p = new Properties();
            p.setProperty("enabled", Boolean.toString(enabled));
            p.setProperty("style", style.name());
            p.setProperty("intensity", Integer.toString(intensity));
            p.setProperty("life", Integer.toString(lifeSeconds));
            p.store(w, "STORM's pop particles - Inspired by impact flash-lite by flamesentinell");
        } catch (IOException e) {
            System.err.println("[STORM] could not save config: " + e.getMessage());
        }
    }
}
