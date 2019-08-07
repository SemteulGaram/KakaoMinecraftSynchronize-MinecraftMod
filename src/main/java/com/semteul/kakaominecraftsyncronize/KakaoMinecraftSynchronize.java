package com.semteul.kakaominecraftsyncronize;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = KakaoMinecraftSynchronize.MODID,
        name = KakaoMinecraftSynchronize.NAME,
        version = KakaoMinecraftSynchronize.VERSION,
        serverSideOnly = true,
        acceptableRemoteVersions = "*"
)
public class KakaoMinecraftSynchronize
{
    static final String MODID = "kakaominecraftsynchronize";
    static final String NAME = "KakaoMinecraftSynchronize";
    static final String VERSION = "1.0";

    private static final String ORIGINAL_SERVER_ORIGIN = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin;

    Logger logger;
    static ServerHandler serverHandler = null;

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit (FMLPreInitializationEvent event) {
        this.logger = event.getModLog();
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void serverStarting (FMLServerStartedEvent event) {
        if (ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin.equals(ORIGINAL_SERVER_ORIGIN)) {
            this.logger.error("Default config detected. Please edit config and restart server");
            return;
        }
        this.logger.info("ServerHandler starting...");
        serverHandler = new ServerHandler(this);
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SuppressWarnings("unused")
        @SubscribeEvent
        public static void onChatMessage (ServerChatEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(event.getUsername()+ ": " + event.getMessage());
        }

        @SuppressWarnings("unused")
        @SubscribeEvent
        public static void onPlayerJoin (PlayerEvent.PlayerLoggedInEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(
                    event.player.getDisplayNameString() + " join Server");
        }

        @SuppressWarnings("unused")
        @SubscribeEvent
        public static void onPlayerDisconnect (PlayerEvent.PlayerLoggedOutEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(
                    event.player.getDisplayNameString() + " leave Server");
        }

        @SuppressWarnings("unused")
        @SubscribeEvent
        public static void onPlayerDeath (LivingDeathEvent event) {
            if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
            if (serverHandler != null) serverHandler.sendMessage(
                    event.getEntityLiving().getCombatTracker().getDeathMessage().getUnformattedText());
        }
    }

}
