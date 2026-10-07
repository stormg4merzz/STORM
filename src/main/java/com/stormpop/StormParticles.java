package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Spawns the pop effects. Uses vanilla dust particles (any RGB colour), so no
 * custom particle classes are needed. Dust particles scale incoming velocity
 * down a lot, so the speeds below look large on purpose - tweak to taste.
 */
public final class StormParticles {
    private static final Random RNG = new Random();

    private StormParticles() {}

    /** dirX/dirZ = horizontal direction pointing from the attacker towards the target. */
    public static void spawn(StormStyle style, double x, double y, double z, double dirX, double dirZ) {
        ParticleManager pm = MinecraftClient.getInstance().particleManager;
        int n = StormConfig.intensity;

        switch (style) {
            case BLUE -> {
                // pulse: one flat ring + one tilted ring
                int count = 20 * n;
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(pm, style.color, 1.0f, x, y, z, Math.cos(a) * 4.0, 0, Math.sin(a) * 4.0);
                    dust(pm, style.color, 0.8f, x, y, z, Math.cos(a) * 3.0, Math.sin(a) * 3.0, 0);
                }
            }
            case LIME -> {
                // spark: random sphere burst
                for (int i = 0; i < 18 * n; i++) {
                    double[] d = randomDir();
                    double s = 2.0 + RNG.nextDouble() * 3.0;
                    dust(pm, style.color, 0.6f, x, y, z, d[0] * s, d[1] * s, d[2] * s);
                }
            }
            case GOLD -> {
                // streaks: short lines of particles shooting away from the attacker
                for (int i = 0; i < 6 * n; i++) {
                    double sx = dirX + (RNG.nextDouble() - 0.5) * 0.9;
                    double sz = dirZ + (RNG.nextDouble() - 0.5) * 0.9;
                    double sy = (RNG.nextDouble() - 0.3) * 0.6;
                    for (int k = 0; k < 6; k++) {
                        double off = k * 0.12;
                        dust(pm, style.color, 0.8f - k * 0.08f,
                                x + sx * off, y + sy * off, z + sz * off,
                                sx * 7.0, sy * 7.0, sz * 7.0);
                    }
                }
            }
            case PINK -> {
                // spiral rising around the target
                int count = 24 * n;
                for (int i = 0; i < count; i++) {
                    double a = i * 0.55;
                    double h = i * 0.045;
                    dust(pm, style.color, 0.8f,
                            x + Math.cos(a) * 0.6, y - 0.4 + h, z + Math.sin(a) * 0.6,
                            -Math.sin(a) * 1.5, 1.5, Math.cos(a) * 1.5);
                }
            }
            case TEAL -> {
                // double shockwave: two flat rings at different speeds
                int count = 16 * n;
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(pm, style.color, 0.9f, x, y - 0.3, z, Math.cos(a) * 2.5, 0, Math.sin(a) * 2.5);
                    dust(pm, style.color, 1.1f, x, y - 0.3, z, Math.cos(a) * 5.0, 0, Math.sin(a) * 5.0);
                }
            }
            case WHITE -> {
                // lightning bolt: zigzag from above down to the target
                int bolts = n;
                for (int b = 0; b < bolts; b++) {
                    double cx = x + (RNG.nextDouble() - 0.5) * 0.4;
                    double cz = z + (RNG.nextDouble() - 0.5) * 0.4;
                    for (int i = 0; i < 24; i++) {
                        double yy = y + 2.5 - i * 0.12;
                        cx += (RNG.nextDouble() - 0.5) * 0.25;
                        cz += (RNG.nextDouble() - 0.5) * 0.25;
                        pm.addParticle(new DustColorTransitionParticleEffect(0xFFFFFF, 0x66E0FF, 0.9f),
                                cx, yy, cz, 0, 0, 0);
                    }
                }
            }
            case RAINBOW -> {
                // multicolour burst, random hue per particle
                for (int i = 0; i < 20 * n; i++) {
                    double[] d = randomDir();
                    double s = 2.0 + RNG.nextDouble() * 3.0;
                    int rgb = MathHelper.hsvToRgb(RNG.nextFloat(), 0.9f, 1.0f);
                    dust(pm, rgb, 0.7f, x, y, z, d[0] * s, d[1] * s, d[2] * s);
                }
            }
        }
    }

    private static void dust(ParticleManager pm, int rgb, float scale,
                             double x, double y, double z, double vx, double vy, double vz) {
        pm.addParticle(new DustParticleEffect(rgb, scale), x, y, z, vx, vy, vz);
    }

    private static double[] randomDir() {
        double u = RNG.nextDouble() * 2 - 1;
        double a = RNG.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(1 - u * u);
        return new double[] { r * Math.cos(a), u, r * Math.sin(a) };
    }
}
