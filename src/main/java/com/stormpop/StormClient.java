package com.stormpop;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Better POP by STORMG4MERX
 * Inspired by impact flash-lite by flamesentinell.
 *
 * The effect itself is triggered from mixin/ClientPlayNetworkHandlerMixin
 * (totem pop only).
 */
public class StormClient implements ClientModInitializer {
    private static boolean openHudNextTick = false;

    @Override
    public void onInitializeClient() {
        StormConfig.load();

        // /storm  ->  opens the HUD (opened on the next tick, because chat closes itself after a command)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("storm").executes(ctx -> {
                    openHudNextTick = true;
                    return 1;
                })));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            StormParticles.tick();
            if (openHudNextTick) {
                openHudNextTick = false;
                client.setScreen(new StormHudScreen(null));
            }
        });
    }
}
