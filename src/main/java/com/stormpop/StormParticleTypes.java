package com.stormpop;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The mod's OWN particle types. Each one has its own coloured texture in
 * assets/storm_pop_particles/textures/particle, so texture packs cannot change them
 * (except a pack that deliberately replaces these files, like your own custom pack).
 * The mod does NOT tint the particles: whatever colours the PNG has is what you see.
 * Inspired by impact flash-lite by flamesentinell.
 */
public final class StormParticleTypes {
    public static final SimpleParticleType BLUE_FLAME = FabricParticleTypes.simple();
    public static final SimpleParticleType BLUE_STAR = FabricParticleTypes.simple();
    public static final SimpleParticleType LIME_STAR = FabricParticleTypes.simple();
    public static final SimpleParticleType GOLD_DIAMOND = FabricParticleTypes.simple();
    public static final SimpleParticleType PINK_PETAL = FabricParticleTypes.simple();
    public static final SimpleParticleType RED_RING = FabricParticleTypes.simple();
    public static final SimpleParticleType RED_STAR = FabricParticleTypes.simple();
    public static final SimpleParticleType WHITE_BOLT = FabricParticleTypes.simple();
    public static final SimpleParticleType WHITE_STAR = FabricParticleTypes.simple();
    public static final SimpleParticleType FIRE_FLAME = FabricParticleTypes.simple();

    private StormParticleTypes() {}

    public static void register() {
        add("storm_blue_flame", BLUE_FLAME);
        add("storm_blue_star", BLUE_STAR);
        add("storm_lime_star", LIME_STAR);
        add("storm_gold_diamond", GOLD_DIAMOND);
        add("storm_pink_petal", PINK_PETAL);
        add("storm_red_ring", RED_RING);
        add("storm_red_star", RED_STAR);
        add("storm_white_bolt", WHITE_BOLT);
        add("storm_white_star", WHITE_STAR);
        add("storm_fire_flame", FIRE_FLAME);
    }

    private static void add(String name, SimpleParticleType type) {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of("storm_pop_particles", name), type);
        // same behaviour as vanilla flame: glows, slows down, shrinks a little while it ages
        ParticleFactoryRegistry.getInstance().register(type, FlameParticle.Factory::new);
    }
}
