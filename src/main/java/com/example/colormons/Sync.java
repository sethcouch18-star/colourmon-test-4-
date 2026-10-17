package com.example.colormons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

/** Sends palettes to the server (which relays them) and receives other players' palettes. */
public class Sync {

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(ColormonsNet.ID, ColormonsNet.CODEC,
                (payload, context) -> context.enqueueWork(() -> apply(payload)));
    }

    /** Push my edit so everyone sees it. No-ops in single player. */
    public static void broadcast(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        MonColors mc2 = ColorStore.of(id);
        if (mc2.isEmpty()) return;
        try {
            PacketDistributor.sendToServer(ColormonsNet.from(id.toString(), mc2));
        } catch (Throwable ignored) {}
    }

    private static void apply(ColormonsNet payload) {
        try {
            UUID id = UUID.fromString(payload.uuid());
            ColorStore.putRemote(id, payload.toColors());
            TintCache.invalidate(id);
        } catch (Exception ignored) {}
    }
}
