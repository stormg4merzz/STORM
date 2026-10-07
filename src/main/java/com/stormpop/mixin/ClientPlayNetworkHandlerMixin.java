package com.stormpop.mixin;

import com.stormpop.StormConfig;
import com.stormpop.StormParticles;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Status 35 = "totem of undying used". Plays the STORM pop effect on that entity. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityStatus", at = @At("HEAD"))
    private void storm$onTotemPop(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (!StormConfig.enabled || packet.getStatus() != 35 || this.world == null) return;
        Entity entity = packet.getEntity(this.world);
        if (entity == null) return;
        StormParticles.spawn(StormConfig.style,
                entity.getX(), entity.getY() + entity.getHeight() * 0.6, entity.getZ(), 0.0, 1.0);
    }
}
