package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Better POP by STORMG4MERX - the pop effects.
 * Inspired by impact flash-lite by flamesentinell.
 *
 * Every pop is split into FRAMES small steps that run on consecutive ticks, so
 * rings expand, spirals turn and fire rises (animation). The HUD "Intensity"
 * sets how far the effect spreads (1 / 2.5 / 3.5 blocks) and the HUD "Time"
 * sets how long each particle lives (1 / 2 / 3 seconds).
 */
public final class StormParticles {
    private static final Random RNG = new Random();
    private static final float BIG = 4.0f;   // biggest size dust allows
    private static final int FRAMES = 8;     // animation steps per pop

    private static final ConcurrentLinkedQueue<Task> PENDING = new ConcurrentLinkedQueue<>();
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
                } catch (RuntimeException ignored) {
                    // never let a particle problem crash the game
                }
            }
        }
    }

    /** y = middle of the target. dirX/dirZ = unused for most styles. */
    public static void spawn(StormStyle style, double x, double y, double z, double dirX, double dirZ) {
        long now = System.currentTimeMillis();
        // ignore the same pop arriving twice
        if (now - lastMs < 100 && Math.abs(x - lastX) < 0.5 && Math.abs(y - lastY) < 0.5 && Math.abs(z - lastZ) < 0.5) {
            return;
        }
        lastMs = now; lastX = x; lastY = y; lastZ = z;

        for (int f = 0; f < FRAMES; f++) {
            final int frame = f;
            PENDING.add(new Task(f, () -> frame(style, x, y, z, frame)));
        }
    }

    private static void frame(StormStyle style, double x, double y, double z, int f) {
        ParticleManager pm = MinecraftClient.getInstance().particleManager;
        double R = StormConfig.reach();                 // how far the effect spreads (blocks)
        double h = Math.max(0.9, R * 0.5);              // half height of the effect
        double p = (f + 1) / (double) FRAMES;           // progress 0..1

        switch (style) {
            case BLUE -> {
                flames(pm, ParticleTypes.SOUL_FIRE_FLAME, x, y, z, R, h, cnt(6, R));
                double r = R * p;
                int pts = ringPoints(r);
                for (double lvl : new double[] { -h * 0.8, 0, h * 0.8 }) {
                    for (int i = 0; i < pts; i++) {
                        double a = Math.PI * 2 * i / pts;
                        dust(pm, style.color, 0x66C8FF, x + Math.cos(a) * r, y + lvl, z + Math.sin(a) * r);
                    }
                }
            }
            case LIME -> {
                // sparks flying out in a sphere
                for (int i = 0; i < cnt(12, R); i++) {
                    double[] d = randomDir();
                    double r = R * p * (0.8 + 0.2 * RNG.nextDouble());
                    dust(pm, style.color, 0x1E8A00,
                            x + d[0] * r, y + d[1] * r * (h / R), z + d[2] * r);
                }
            }
            case GOLD -> {
                // streaks shooting out all around, each with a short tail
                for (int i = 0; i < cnt(8, R); i++) {
                    double a = RNG.nextDouble() * Math.PI * 2;
                    double yy = y + (RNG.nextDouble() - 0.5) * 2 * h * 0.8;
                    double dist = R * p * (0.6 + 0.4 * RNG.nextDouble());
                    for (int k = 0; k < 4; k++) {
                        double d = dist * (1.0 - k * 0.12);
                        dust(pm, style.color, 0xFFF0A0, x + Math.cos(a) * d, yy, z + Math.sin(a) * d);
                    }
                }
            }
            case PINK -> {
                // two spiral arms that turn and rise
                int n = cnt(10, R);
                for (int arm = 0; arm < 2; arm++) {
                    for (int j = 0; j < n; j++) {
                        double a = f * 0.6 + j * 0.5 + arm * Math.PI;
                        double rad = R * (j + 1) / n;
                        double yy = y - h + 2 * h * (j + 1) / n;
                        dust(pm, style.color, 0xFF8CC8, x + Math.cos(a) * rad, yy, z + Math.sin(a) * rad);
                    }
                }
            }
            case TEAL -> {
                // shockwave rings along the ground
                for (double rr : new double[] { R * p, R * p * 0.6 }) {
                    int pts = ringPoints(rr);
                    for (int i = 0; i < pts; i++) {
                        double a = Math.PI * 2 * i / pts;
                        dust(pm, style.color, 0x00A58F, x + Math.cos(a) * rr, y - h + 0.15, z + Math.sin(a) * rr);
                    }
                }
                for (int i = 0; i < cnt(4, R); i++) {
                    double[] pt = discPoint(R);
                    dust(pm, style.color, 0x00A58F, x + pt[0], y + (RNG.nextDouble() - 0.5) * 2 * h, z + pt[1]);
                }
            }
            case WHITE -> {
                // lightning bolts, new ones every 3rd step
                if (f % 3 == 0) {
                    int bolts = (int) Math.max(1, Math.round(R));
                    for (int b = 0; b < bolts; b++) {
                        double[] pt = discPoint(R);
                        double cx = x + pt[0];
                        double cz = z + pt[1];
                        double top = y + h * 2.2;
                        for (double yy = top; yy > y - h; yy -= 0.1) {
                            cx += (RNG.nextDouble() - 0.5) * 0.3;
                            cz += (RNG.nextDouble() - 0.5) * 0.3;
                            dust(pm, 0xFFFFFF, 0x66E0FF, cx, yy, cz);
                        }
                    }
                }
                for (int i = 0; i < cnt(5, R); i++) {
                    double[] pt = discPoint(R);
                    dust(pm, 0xFFFFFF, 0x66E0FF, x + pt[0], y + (RNG.nextDouble() - 0.5) * 2 * h, z + pt[1]);
                }
            }
            case FIRE -> {
                // real fire: flame sprites rising, hot colours, a few embers
                flames(pm, ParticleTypes.FLAME, x, y, z, R, h, cnt(10, R));
                for (int i = 0; i < cnt(8, R); i++) {
                    double[] pt = discPoint(R);
                    dust(pm, 0xFFD21F, 0xFF2A00, x + pt[0], y + (RNG.nextDouble() - 0.5) * 2 * h, z + pt[1]);
                }
                if (f % 2 == 0) {
                    for (int i = 0; i < cnt(2, R); i++) {
                        double[] pt = discPoint(R);
                        add(pm, ParticleTypes.LAVA, x + pt[0], y + (RNG.nextDouble() - 0.3) * h, z + pt[1], 0, 0, 0);
                    }
                }
            }
        }
    }

    /** Flame-shaped particles rising from the ground to head height inside the effect area. */
    private static void flames(ParticleManager pm, ParticleEffect type,
                               double x, double y, double z, double R, double h, int count) {
        for (int i = 0; i < count; i++) {
            double[] pt = discPoint(R);
            double py = y - h + RNG.nextDouble() * h * 1.4;
            Particle fp = add(pm, type, x + pt[0], py, z + pt[1],
                    (RNG.nextDouble() - 0.5) * 0.04, 0.07 + RNG.nextDouble() * 0.10, (RNG.nextDouble() - 0.5) * 0.04);
            if (fp != null) {
                fp.scale(2.5f + RNG.nextFloat() * 1.5f);
            }
        }
    }

    /** Coloured dust square that fades from colour a to colour b. */
    private static void dust(ParticleManager pm, int a, int b, double x, double y, double z) {
        if (a == b) {
            add(pm, new DustParticleEffect(a, BIG), x, y, z, 0, 0, 0);
        } else {
            add(pm, new DustColorTransitionParticleEffect(a, b, BIG), x, y, z, 0, 0, 0);
        }
    }

    /** Adds a particle and sets how long it lives (HUD "Time"). */
    private static Particle add(ParticleManager pm, ParticleEffect effect,
                                double x, double y, double z, double vx, double vy, double vz) {
        Particle particle = pm.addParticle(effect, x, y, z, vx, vy, vz);
        if (particle != null) {
            particle.setMaxAge(lifeTicks());
        }
        return particle;
    }

    /** Particle lifetime in ticks (20 ticks = 1 second). */
    private static int lifeTicks() {
        return StormConfig.lifeSeconds * 20 - 2 + RNG.nextInt(5);
    }

    /** Base particle count scaled by the spread, so a wider effect gets more particles. */
    private static int cnt(int base, double R) {
        return (int) Math.max(1, Math.round(base * R));
    }

    private static int ringPoints(double r) {
        return Math.max(8, (int) Math.round(Math.PI * 2 * r * 3));
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
