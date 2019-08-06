package com.semteul.kakaominecraftsyncronize;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = KakaoMinecraftSynchronize.MODID, name = KakaoMinecraftSynchronize.NAME, version = KakaoMinecraftSynchronize.VERSION)
public class KakaoMinecraftSynchronize
{
    static final String MODID = "kakaominecraftsynchronize";
    static final String NAME = "KakaoMinecraftSynchronize";
    static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
