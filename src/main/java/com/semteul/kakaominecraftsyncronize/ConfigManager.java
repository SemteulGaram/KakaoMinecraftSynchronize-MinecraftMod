package com.semteul.kakaominecraftsyncronize;

import net.minecraftforge.common.config.Config;

public class ConfigManager {

    @Config(modid = KakaoMinecraftSynchronize.MODID)
    public static class KakaoMinecraftSynchronizeConfig {

        @Config.Comment("Server host")
        public static String serverOrigin = "http://example.com:3000";

        @Config.Comment("Update interval (milliseconds)")
        public static int interval = 500;
    }
}
