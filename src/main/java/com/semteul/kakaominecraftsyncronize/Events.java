package com.semteul.kakaominecraftsyncronize;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.semteul.kakaominecraftsyncronize.KakaoMinecraftSynchronize.serverHandler;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus =  Mod.EventBusSubscriber.Bus.FORGE)
public class Events {
    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        if (serverHandler != null) serverHandler.sendMessage(event.getUsername()+ ": " + event.getMessage());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (serverHandler != null) serverHandler.sendMessage(
            event.getPlayer().getDisplayName().getString() + " join Server");
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (serverHandler != null) serverHandler.sendMessage(
            event.getPlayer().getDisplayName().getString() + " leave Server");
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) return;
        if (serverHandler != null) serverHandler.sendMessage(
            event.getSource().getDeathMessage(event.getEntityLiving()).getUnformattedComponentText());
    }

    @SubscribeEvent
    public void advancement(AdvancementEvent event) {
        if (serverHandler != null && event.getAdvancement() != null
                && event.getAdvancement().getDisplay() != null
                && event.getAdvancement().getDisplay().shouldAnnounceToChat())
            serverHandler.sendMessage(event.getPlayer().getDisplayName().getString() + " advencement: " + event
                    .getAdvancement()
                    .getDisplay()
                    .getTitle()
                    .getString());

    }
}
