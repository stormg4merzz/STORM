package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Better POP by STORMG4MERX - the pop effects.
 * Inspired by impact flash-lite by flamesentinell.
 *
 * Every style has its own particle shape with its own texture (see StormParticleTypes),
 * so texture packs cannot change how they look:
 *   BLUE = blue flames, LIME = star sparks, GOLD = crystal shards,
 *   PINK = cherry blossoms, RED = circle rings, WHITE = lightning, FIRE = real fire.
 *
 * A pop is split into FRAMES steps that run on consecutive ticks (animation).
 * HUD "Intensity" = how far it spreads (1 / 2.5 / 3.5 blocks),
 * HUD "Time" = how long particles live (1 / 2 / 3 seconds).
 */
public final class StormParticles {
    private static final Random RNG = new Random();
    private static final int FRAMES = 8;

    private static final ConcurrentLinkedQueue<Task> PENDING = new ConcurrentLinkedQueue<>();
    private static final Set<String> REPORTED = new HashSet<>();
    private static String currentStyle = "";
    private static long lastMs = 0;
    private static double lastX, lastY, lastZ;

    private static final class Task {
        int delay;
        final Runnable run;
        Task(int delay, Runnable run) { this.delay = delay; this.run = run; }
    }

    private StormParticles() {}

    /** Called every client tick (from StormClient). */
    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            PENDING.clear();
            return;
        }
        Iterator<Task> it = PENDING.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.delay-- <= 0) {
                it.remove();
                try {
                    t.run.run();
                } catch (RuntimeException e) {
                    // never let a particle problem crash the game; tell the player once
                    report("frame", e);
                }
            }
        }
    }

    /** y = middle of the target. */
    public static void spawn(StormStyle style, double x, double y, double z, double dirX, double dirZ) {
        long now = System.currentTimeMillis();
        // ignore the same pop arriving twice
        if (now - lastMs < 100 && Math.abs(x - lastX) < 0.5 && Math.abs(y - lastY) < 0.5 && Math.abs(z - lastZ) < 0.5) {
            return;
        }
        lastMs = now; lastX = x; lastY = y; lastZ = z;

        for (int f = 0; f < FRAMES; f++) {
            final int frame = f;
            PENDING.add(new Task(f, () -> frame(StormConfig.style, x, y, z, frame)));
        }
    }

    private static void frame(StormStyle style, double x, double y, double z, int f) {
        ParticleManager pm = MinecraftClient.getInstance().particleManager;
        double R = StormConfig.reach();                 // how far the effect spreads (blocks)
        double h = Math.max(0.9, R * 0.5);              // half height of the effect
        double p = (f + 1) / (double) FRAMES;           // progress 0..1
        currentStyle = style.displayName;

        switch (style) {
            case BLUE -> {
                // blue flames rising around the player + a few sparkles
                for (int i = 0; i < cnt(5, R); i++) {
                    double[] pt = discPoint(R);
                    spawnOne(pm, StormParticleTypes.BLUE_FLAME, x + pt[0], y - h + RNG.nextDouble() * h * 1.6, z + pt[1],
                            (RNG.nextDouble() - 0.5) * 0.03, 0.05 + RNG.nextDouble() * 0.08, (RNG.nextDouble() - 0.5) * 0.03,
                            2.5f + RNG.nextFloat() * 1.5f);
                }
                for (int i = 0; i < cnt(2, R); i++) {
                    double[] pt = discPoint(R);
                    spawnOne(pm, StormParticleTypes.BLUE_STAR, x + pt[0], y + (RNG.nextDouble() - 0.5) * 2 * h, z + pt[1],
                            0, 0.02, 0, 1.4f + RNG.nextFloat());
                }
            }
            case LIME -> {
                // star sparks flying out in a sphere
                for (int i = 0; i < cnt(8, R); i++) {
                    double[] d = randomDir();
                    double r = R * p * (0.8 + 0.2 * RNG.nextDouble());
                    spawnOne(pm, StormParticleTypes.LIME_STAR, x + d[0] * r, y + d[1] * r * (h / R), z + d[2] * r,
                            d[0] * 0.02, d[1] * 0.02, d[2] * 0.02,
                            1.6f + RNG.nextFloat() * 1.2f);
                }
            }
            case GOLD -> {
                // crystal shards shooting out all around
                for (int i = 0; i < cnt(7, R); i++) {
                    double a = RNG.nextDouble() * Math.PI * 2;
                    double yy = y + (RNG.nextDouble() - 0.5) * 2 * h * 0.8;
                    double dist = R * p * (0.6 + 0.4 * RNG.nextDouble());
                    spawnOne(pm, StormParticleTypes.GOLD_DIAMOND, x + Math.cos(a) * dist, yy, z + Math.sin(a) * dist,
                            Math.cos(a) * 0.04, 0, Math.sin(a) * 0.04,
                            1.8f + RNG.nextFloat() * 1.4f);
                }
            }
            case PINK -> {
                // cherry blossoms drifting down around the player
                for (int i = 0; i < cnt(6, R); i++) {
                    double[] pt = discPoint(R);
                    spawnOne(pm, StormParticleTypes.PINK_PETAL, x + pt[0], y + (RNG.nextDouble() - 0.2) * h * 1.4, z + pt[1],
                            (RNG.nextDouble() - 0.5) * 0.04, -0.02 - RNG.nextDouble() * 0.03, (RNG.nextDouble() - 0.5) * 0.04,
                            2.0f + RNG.nextFloat() * 1.5f);
                }
            }
            case RED -> {
                // big circle rings around the player, growing outward
                if (f % 2 == 0) {
                    double ringR = Math.max(0.5, R * p * 1.1);
                    for (int k = 0; k < 2; k++) {
                        spawnOne(pm, StormParticleTypes.RED_RING, x, y, z, 0, 0, 0,
                                (float) (ringR / 0.15 * (0.9 + 0.2 * k)));
                    }
                }
                for (int i = 0; i < cnt(3, R); i++) {
                    double[] d = randomDir();
                    double r = R * p;
                    spawnOne(pm, StormParticleTypes.RED_STAR, x + d[0] * r, y + d[1] * r * (h / R), z + d[2] * r,
                            0, 0, 0, 1.2f + RNG.nextFloat());
                }
            }
            case WHITE -> {
                // lightning bolts, new ones every 3rd step
                if (f % 3 == 0) {
                    int bolts = (int) Math.max(1, Math.round(R));
                    for (int b = 0; b < bolts; b++) {
                        double[] pt = discPoint(R);
                        for (int k = 0; k < 3; k++) {
                            spawnOne(pm, StormParticleTypes.WHITE_BOLT,
                                    x + pt[0] + (RNG.nextDouble() - 0.5) * 0.3, y + h * 1.2 - k * h * 0.9, z + pt[1] + (RNG.nextDouble() - 0.5) * 0.3,
                                    0, 0, 0, 4.0f + RNG.nextFloat() * 1.5f);
                        }
                    }
                }
                for (int i = 0; i < cnt(3, R); i++) {
                    double[] pt = discPoint(R);
                    spawnOne(pm, StormParticleTypes.WHITE_STAR, x + pt[0], y + (RNG.nextDouble() - 0.5) * 2 * h, z + pt[1],
                            0, 0, 0, 1.2f + RNG.nextFloat());
                }
            }
            case FIRE -> {
                // real fire: flame sprites rising from the ground to above the head
                for (int i = 0; i < cnt(9, R); i++) {
                    double[] pt = discPoint(R);
                    spawnOne(pm, StormParticleTypes.FIRE_FLAME, x + pt[0], y - h + RNG.nextDouble() * h * 1.4, z + pt[1],
                            (RNG.nextDouble() - 0.5) * 0.04, 0.07 + RNG.nextDouble() * 0.10, (RNG.nextDouble() - 0.5) * 0.04,
                            2.5f + RNG.nextFloat() * 1.5f);
                }
            }
        }
    }

    /**
     * Spawns one particle of our own type (colours come from its PNG, no tint),
     * sizes it and sets how long it lives (HUD "Time"). A failing particle never stops the others.
     */
    private static void spawnOne(ParticleManager pm, ParticleEffect type,
                                 double x, double y, double z, double vx, double vy, double vz,
                                 float scale) {
        try {
            Particle particle = pm.addParticle(type, x, y, z, vx, vy, vz);
            if (particle == null) {
                report("missing particle", new IllegalStateException("particle type not registered"));
                return;
            }
            particle.scale(scale);
            particle.setMaxAge(lifeTicks());
        } catch (RuntimeException e) {
            report(type.getClass().getSimpleName(), e);
        }
    }

    /** Tells you once in chat if a particle type fails, so it can be fixed. */
    private static void report(String what, Throwable t) {
        if (!REPORTED.add(currentStyle + " " + what)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[STORM] " + currentStyle + " / " + what + " failed: " + t), false);
        }
    }

    /** Particle lifetime in ticks (20 ticks = 1 second). */
    private static int lifeTicks() {
        return StormConfig.lifeSeconds * 20 - 2 + RNG.nextInt(5);
    }

    /** Base particle count scaled by the spread, so a wider effect gets more particles. */
    private static int cnt(int base, double R) {
        return (int) Math.max(1, Math.round(base * R));
    }

    /** Random point inside a flat circle of radius R. */
    private static double[] discPoint(double R) {
        double r = R * Math.sqrt(RNG.nextDouble());
        double a = RNG.nextDouble() * Math.PI * 2;
        return new double[] { Math.cos(a) * r, Math.sin(a) * r };
    }

    private static double[] randomDir() {
        double u = RNG.nextDouble() * 2 - 1;
        double a = RNG.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(1 - u * u);
        return new double[] { r * Math.cos(a), u, r * Math.sin(a) };
    }
}
