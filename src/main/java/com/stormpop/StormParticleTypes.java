package com.stormpop;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The mod's OWN particle types. Each one uses its own texture from
 * assets/storm_pop_particles/textures/particle, so texture packs cannot change them.
 * Inspired by impact flash-lite by flamesentinell.
 */
public final class StormParticleTypes {
    public static final SimpleParticleType FLAME = FabricParticleTypes.simple();
    public static final SimpleParticleType FIRE = FabricParticleTypes.simple();
    public static final SimpleParticleType STAR = FabricParticleTypes.simple();
    public static final SimpleParticleType DIAMOND = FabricParticleTypes.simple();
    public static final SimpleParticleType PETAL = FabricParticleTypes.simple();
    public static final SimpleParticleType RING = FabricParticleTypes.simple();
    public static final SimpleParticleType BOLT = FabricParticleTypes.simple();

    private StormParticleTypes() {}

    public static void register() {
        add("storm_flame", FLAME);
        add("storm_fire", FIRE);
        add("storm_star", STAR);
        add("storm_diamond", DIAMOND);
        add("storm_petal", PETAL);
        add("storm_ring", RING);
        add("storm_bolt", BOLT);
    }

    private static void add(String name, SimpleParticleType type) {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of("storm_pop_particles", name), type);
        // same behaviour as vanilla flame: glows, slows down, shrinks a little while it ages
        ParticleFactoryRegistry.getInstance().register(type, FlameParticle.Factory::new);
    }
}
