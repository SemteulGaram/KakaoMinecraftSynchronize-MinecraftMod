package com.semteul.kakaominecraftsyncronize;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final Properties defaultValues = new Properties();
    private String fileName;

    public static boolean debug = false;
    public static String relayServerAddress = "http://example.com:7110";
    public static int updateInterval = 500;

    Config(String fileName) {
        Logger.Log("Config instance - " + fileName);
        this.fileName = fileName;
        read();
    }

    public void read() {
        Properties properties = new Properties(defaultValues);

        try {
            FileReader configReader = new FileReader(fileName);
            properties.load(configReader);
            configReader.close();
        } catch (FileNotFoundException ignored) {
            // If the config does not exist, generate the default one.
            Logger.Log("Generating the config file at: " + fileName);
            save();
            return;
        } catch (IOException e) {
            Logger.Log("Failed to read the config file: " + fileName);
            e.printStackTrace();
        }

        relayServerAddress = properties.getProperty(Constants.CONFIG_RELAY_SERVER_ADDRESS);
        updateInterval = parseIntOrDefault(properties.getProperty(Constants.CONFIG_UPDATE_INTERVAL), 500);
        debug = parseIntOrDefault(properties.getProperty(Constants.CONFIG_DEBUG), 0) != 0;
    }

    public void save() {
        try {
            File config = new File(fileName);
            boolean existed = config.exists();
            File parentDir = config.getParentFile();
            if (!parentDir.exists())
                parentDir.mkdirs();

            FileWriter configWriter = new FileWriter(config);

            writeString(configWriter, Constants.CONFIG_RELAY_SERVER_ADDRESS, relayServerAddress);
            writeInt(configWriter, Constants.CONFIG_UPDATE_INTERVAL, updateInterval);
            writeBoolean(configWriter, Constants.CONFIG_DEBUG, debug);

            configWriter.close();

            if (!existed)
                Logger.Log("Created the config file.");
        } catch (IOException e) {
            Logger.Log("Failed to write the config file: " + fileName);
            e.printStackTrace();
        }
    }

    private static void writeString(FileWriter configWriter, String name, String value) throws IOException {
        configWriter.write(name + '=' + value + '\n');
    }

    private static void writeBoolean(FileWriter configWriter, String name, boolean value) throws IOException {
        writeString(configWriter, name, value ? "1" : "0");
    }

    private static void writeInt(FileWriter configWriter, String name, int value) throws IOException {
        writeString(configWriter, name, String.valueOf(value));
    }

    private int parseIntOrDefault(String toParse, int defaultValue) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
