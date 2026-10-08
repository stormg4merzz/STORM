package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Big STORM pop effects. Uses vanilla dust particles at their maximum size
 * and spreads them over about one player height (1.8 blocks).
 */
public final class StormParticles {
    private static final Random RNG = new Random();
    private static final float BIG = 4.0f;   // maximum size dust allows
    private static final double H = 0.9;     // half of a player's height

    private StormParticles() {}

    /** y = middle of the target. dirX/dirZ = horizontal direction away from attacker. */
    public static void spawn(StormStyle style, double x, double y, double z, double dirX, double dirZ) {
        ParticleManager pm = MinecraftClient.getInstance().particleManager;
        int n = StormConfig.intensity;

        switch (style) {
            case BLUE -> {
                // three big pulse rings: feet, middle, head
                int count = 24 * n;
                for (double lvl : new double[] { -H, 0, H }) {
                    for (int i = 0; i < count; i++) {
                        double a = Math.PI * 2 * i / count;
                        dust(pm, style.color, x, y + lvl, z, Math.cos(a) * 7.0, 0, Math.sin(a) * 7.0);
                    }
                }
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(pm, style.color, x, y, z, Math.cos(a) * 6.0, Math.sin(a) * 6.0, 0);
                }
            }
            case LIME -> {
                for (int i = 0; i < 40 * n; i++) {
                    double[] d = randomDir();
                    double s = 4.0 + RNG.nextDouble() * 5.0;
                    dust(pm, style.color, x, y + (RNG.nextDouble() - 0.5) * 2 * H, z,
                            d[0] * s, d[1] * s, d[2] * s);
                }
            }
            case GOLD -> {
                for (int i = 0; i < 10 * n; i++) {
                    double sx = dirX + (RNG.nextDouble() - 0.5) * 1.0;
                    double sz = dirZ + (RNG.nextDouble() - 0.5) * 1.0;
                    double sy = (RNG.nextDouble() - 0.5) * 0.5;
                    double startY = y + (RNG.nextDouble() - 0.5) * 2 * H;
                    for (int k = 0; k < 8; k++) {
                        double off = k * 0.22;
                        dust(pm, style.color, x + sx * off, startY + sy * off, z + sz * off,
                                sx * 10.0, sy * 10.0, sz * 10.0);
                    }
                }
            }
            case PINK -> {
                int count = 40 * n;
                for (int i = 0; i < count; i++) {
                    double a = i * 0.45;
                    double h = -H + (2 * H) * i / count;
                    dust(pm, style.color, x + Math.cos(a) * 0.9, y + h, z + Math.sin(a) * 0.9,
                            -Math.sin(a) * 2.5, 1.5, Math.cos(a) * 2.5);
                }
            }
            case TEAL -> {
                int count = 20 * n;
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(pm, style.color, x, y - H + 0.2, z, Math.cos(a) * 5.0, 0, Math.sin(a) * 5.0);
                    dust(pm, style.color, x, y - H + 0.2, z, Math.cos(a) * 9.0, 0, Math.sin(a) * 9.0);
                }
            }
            case WHITE -> {
                for (int b = 0; b < n; b++) {
                    double cx = x + (RNG.nextDouble() - 0.5) * 0.4;
                    double cz = z + (RNG.nextDouble() - 0.5) * 0.4;
                    for (int i = 0; i < 32; i++) {
                        double yy = y + 2.2 - i * 0.1;
                        cx += (RNG.nextDouble() - 0.5) * 0.3;
                        cz += (RNG.nextDouble() - 0.5) * 0.3;
                        pm.addParticle(new DustColorTransitionParticleEffect(0xFFFFFF, 0x66E0FF, BIG),
                                cx, yy, cz, 0, 0, 0);
                    }
                }
            }
            case RAINBOW -> {
                for (int i = 0; i < 40 * n; i++) {
                    double[] d = randomDir();
                    double s = 4.0 + RNG.nextDouble() * 5.0;
                    int rgb = MathHelper.hsvToRgb(RNG.nextFloat(), 0.9f, 1.0f);
                    dust(pm, rgb, x, y + (RNG.nextDouble() - 0.5) * 2 * H, z,
                            d[0] * s, d[1] * s, d[2] * s);
                }
            }
        }
    }

    private static void dust(ParticleManager pm, int rgb,
                             double x, double y, double z, double vx, double vy, double vz) {
        pm.addParticle(new DustParticleEffect(rgb, BIG), x, y, z, vx, vy, vz);
    }

    private static double[] randomDir() {
        double u = RNG.nextDouble() * 2 - 1;
        double a = RNG.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(1 - u * u);
        return new double[] { r * Math.cos(a), u, r * Math.sin(a) };
    }
}
