package com.semteul.kakaominecraftsyncronize;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import java.io.File;
import java.nio.file.Paths;

@Mod(Constants.MOD_ID)
public class KakaoMinecraftSynchronize {
    public static MinecraftServer server = null;
    
    public static ServerHandler serverHandler = null;

    public KakaoMinecraftSynchronize() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            Logger.Log("Disabled because not running on the dedicated server.");
            return;
        }
        MinecraftForge.EVENT_BUS.register(this);
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        new Config(Paths.get(FMLPaths.CONFIGDIR.get().toString(), Constants.MOD_ID + ".cfg").toString());
        server = event.getServer();
        serverHandler = new ServerHandler(this);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
    	if (Config.relayServerAddress.equals(Constants.CONFIG_RELAY_SERVER_ADDRESS)) {
            Logger.Log("Default config detected. Please edit config and restart server");
            return;
        }
        Logger.Log("ServerHandler starting...");
    	serverHandler = new ServerHandler(this);
    	serverHandler.sendMessage(server.getMOTD() + " server has been started!");
    }

    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent event) {
    	if (serverHandler != null) serverHandler.sendMessage(server.getMOTD() + " server has been stopped!");
    }



    static boolean hasServer() {
    	return server != null;
    }

    void sendMessageAll(String text) {
        if (!serverHandler.isReady()) return;
        ITextComponent msg = new StringTextComponent(text);
        // Send message to console
        server.sendMessage(msg, Util.DUMMY_UUID);

        PlayerList pList = server.getPlayerList();
        String[] names = pList.getOnlinePlayerNames();
        for (String name : names) {
            PlayerEntity ePlayer = pList.getPlayerByUsername(name);
            // Send message to player
            if (ePlayer != null) ePlayer.sendMessage(msg, Util.DUMMY_UUID);
        }
    }
}
