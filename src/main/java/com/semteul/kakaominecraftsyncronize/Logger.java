package com.semteul.kakaominecraftsyncronize;

public class Logger {
    public static void Log(String text) {
        System.out.println("[KakaoMinecraftSynchronize] " + text);
    }

    public static void DebugLog(String text) {
        if (Config.debug)
            System.out.println("[KakaoMinecraftSynchronize] " + text);
    }
}