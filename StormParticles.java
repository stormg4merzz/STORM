package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Big, short-lived STORM pop effects: real flame-shaped fire particles tinted
 * in the style colour, plus coloured dust rings/bursts around them.
 */
public final class StormParticles {
    private static final Random RNG = new Random();
    private static final float BIG = 4.0f;   // maximum size dust allows
    private static final double H = 0.9;     // half of a player's height

    /** Particle lifetime in ticks from the HUD "Time" button (20 ticks = 1 second). */
    private static int lifeTicks() {
        return StormConfig.lifeSeconds * 20 - 2 + RNG.nextInt(5);
    }

    /** Scales a base particle count by the HUD intensity: Low = 1/3, Normal = 2/3, High = 3.5/3. */
    private static int amount(int base) {
        double mult = switch (StormConfig.intensity) { case 1 -> 1.0 / 3; case 3 -> 3.5 / 3; default -> 2.0 / 3; };
        return Math.max(1, (int) Math.round(base * mult));
    }

    private StormParticles() {}

    /** y = middle of the target. dirX/dirZ = horizontal direction away from attacker. */
    public static void spawn(StormStyle style, double x, double y, double z, double dirX, double dirZ) {
        ParticleManager pm = MinecraftClient.getInstance().particleManager;

        fire(pm, style, x, y, z);

        switch (style) {
            case BLUE -> {
                int count = amount(20);
                for (double lvl : new double[] { -H, 0, H }) {
                    for (int i = 0; i < count; i++) {
                        double a = Math.PI * 2 * i / count;
                        dust(pm, style.color, x, y + lvl, z, Math.cos(a) * 7.0, 0, Math.sin(a) * 7.0);
                    }
                }
            }
            case LIME -> {
                for (int i = 0; i < amount(30); i++) {
                    double[] d = randomDir();
                    double s = 4.0 + RNG.nextDouble() * 5.0;
                    dust(pm, style.color, x, y + (RNG.nextDouble() - 0.5) * 2 * H, z,
                            d[0] * s, d[1] * s, d[2] * s);
                }
            }
            case GOLD -> {
                for (int i = 0; i < amount(8); i++) {
                    double sx = dirX + (RNG.nextDouble() - 0.5) * 1.0;
                    double sz = dirZ + (RNG.nextDouble() - 0.5) * 1.0;
                    double sy = (RNG.nextDouble() - 0.5) * 0.5;
                    double startY = y + (RNG.nextDouble() - 0.5) * 2 * H;
                    for (int k = 0; k < 6; k++) {
                        double off = k * 0.22;
                        dust(pm, style.color, x + sx * off, startY + sy * off, z + sz * off,
                                sx * 10.0, sy * 10.0, sz * 10.0);
                    }
                }
            }
            case PINK -> {
                int count = amount(30);
                for (int i = 0; i < count; i++) {
                    double a = i * 0.45;
                    double h = -H + (2 * H) * i / count;
                    dust(pm, style.color, x + Math.cos(a) * 0.9, y + h, z + Math.sin(a) * 0.9,
                            -Math.sin(a) * 2.5, 1.5, Math.cos(a) * 2.5);
                }
            }
            case TEAL -> {
                int count = amount(18);
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(pm, style.color, x, y - H + 0.2, z, Math.cos(a) * 5.0, 0, Math.sin(a) * 5.0);
                    dust(pm, style.color, x, y - H + 0.2, z, Math.cos(a) * 9.0, 0, Math.sin(a) * 9.0);
                }
            }
            case WHITE -> {
                for (int b = 0; b < amount(1); b++) {
                    double cx = x + (RNG.nextDouble() - 0.5) * 0.4;
                    double cz = z + (RNG.nextDouble() - 0.5) * 0.4;
                    for (int i = 0; i < 32; i++) {
                        double yy = y + 2.2 - i * 0.1;
                        cx += (RNG.nextDouble() - 0.5) * 0.3;
                        cz += (RNG.nextDouble() - 0.5) * 0.3;
                        add(pm, new DustColorTransitionParticleEffect(0xFFFFFF, 0x66E0FF, BIG),
                                cx, yy, cz, 0, 0, 0);
                    }
                }
            }
            case RAINBOW -> {
                for (int i = 0; i < amount(30); i++) {
                    double[] d = randomDir();
                    double s = 4.0 + RNG.nextDouble() * 5.0;
                    int rgb = MathHelper.hsvToRgb(RNG.nextFloat(), 0.9f, 1.0f);
                    dust(pm, rgb, x, y + (RNG.nextDouble() - 0.5) * 2 * H, z,
                            d[0] * s, d[1] * s, d[2] * s);
                }
            }
        }
    }

    /** Real flame-shaped particles over the whole body, tinted in the style colour. */
    private static void fire(ParticleManager pm, StormStyle style, double x, double y, double z) {
        // warm styles use the normal flame sprite, cool styles use the soul-fire sprite
        boolean warm = style == StormStyle.GOLD || style == StormStyle.PINK;
        ParticleEffect type = warm ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;

        for (int i = 0; i < amount(40); i++) {
            double px = x + (RNG.nextDouble() - 0.5) * 0.9;
            double pz = z + (RNG.nextDouble() - 0.5) * 0.9;
            double py = y + (RNG.nextDouble() - 0.5) * 2 * H;
            Particle p = pm.addParticle(type, px, py, pz,
                    (RNG.nextDouble() - 0.5) * 0.06, 0.08 + RNG.nextDouble() * 0.12, (RNG.nextDouble() - 0.5) * 0.06);
            if (p == null) continue;

            int rgb = style == StormStyle.RAINBOW
                    ? MathHelper.hsvToRgb(RNG.nextFloat(), 0.9f, 1.0f)
                    : style.color;
            if (p instanceof BillboardParticle bp) {
                bp.setColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
            }
            p.scale(2.5f + RNG.nextFloat() * 1.5f);
            p.setMaxAge(lifeTicks());
        }
    }

    private static void dust(ParticleManager pm, int rgb,
                             double x, double y, double z, double vx, double vy, double vz) {
        add(pm, new DustParticleEffect(rgb, BIG), x, y, z, vx, vy, vz);
    }

    /** Adds a particle and makes it disappear quickly. */
    private static void add(ParticleManager pm, ParticleEffect effect,
                            double x, double y, double z, double vx, double vy, double vz) {
        Particle p = pm.addParticle(effect, x, y, z, vx, vy, vz);
        if (p != null) {
            p.setMaxAge(lifeTicks());
        }
    }

    private static double[] randomDir() {
        double u = RNG.nextDouble() * 2 - 1;
        double a = RNG.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(1 - u * u);
        return new double[] { r * Math.cos(a), u, r * Math.sin(a) };
    }
}
