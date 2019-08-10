package com.semteul.kakaominecraftsyncronize;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
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
public class KakaoMinecraftSynchronize {
    static final String MODID = "kakaominecraftsynchronize";
    static final String NAME = "KakaoMinecraftSynchronize";
    static final String VERSION = "1.0.1";

    private static final String ORIGINAL_SERVER_ORIGIN = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin;

    private static MinecraftServer server = null;
    
    static ServerHandler serverHandler = null;

    Logger logger = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.logger = event.getModLog();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        server = event.getServer();
    }
    
    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
    	if (ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin.equals(ORIGINAL_SERVER_ORIGIN)) {
            this.logger.error("Default config detected. Please edit config and restart server");
            return;
        }
    	this.logger.info("ServerHandler starting...");
    	serverHandler = new ServerHandler(this);
    	serverHandler.sendMessage(server.getMOTD() + " server has been started!");
    }
    
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    	if (serverHandler != null) serverHandler.sendMessage(server.getMOTD() + " server has been stopped!");
    }

    @Mod.EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void onChatMessage(ServerChatEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(event.getUsername()+ ": " + event.getMessage());
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(
                    event.player.getDisplayNameString() + " join Server");
        }

        @SubscribeEvent
        public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
            if (serverHandler != null) serverHandler.sendMessage(
                    event.player.getDisplayNameString() + " leave Server");
        }

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
            if (serverHandler != null) serverHandler.sendMessage(
                    event.getEntityLiving().getCombatTracker().getDeathMessage().getUnformattedText());
        }
    }
    
    static boolean hasServer() {
    	return server != null;
    }

    void sendMessageAll(String text) {
        if (!serverHandler.isReady()) return;
        ITextComponent msg = new TextComponentString(text);
        // Send message to console
        server.sendMessage(msg);
        
        PlayerList pList = server.getPlayerList();
        String[] names = pList.getOnlinePlayerNames();
        for (String name : names) {
            EntityPlayerMP ePlayer = pList.getPlayerByUsername(name);
            // Send message to player
            if (ePlayer != null) ePlayer.sendMessage(msg);
        }
    }
}
