package com.semteul.kakaominecraftsyncronize;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.CharSet;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ServerHandler {
    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    private final KakaoMinecraftSynchronize ctx;
    private final Logger logger;
    private final String cUrl;
    private final String uUrl;
    private final String mUrl;

    private boolean connect = false;
    private int errorCount = 0;
    private boolean wasWrongResponse = false;

    ServerHandler (KakaoMinecraftSynchronize ctx) {
        this.logger = ctx.logger;
        this.ctx = ctx;
        this.cUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/c";
        this.uUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/u";
        this.mUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/m";

        SERVICE.execute(new InternalLooper(this.ctx, ConfigManager.KakaoMinecraftSynchronizeConfig.interval));
    }

    int getErrorCount() {
        return this.errorCount;
    }

    boolean isConnect () {
        return this.connect;
    }

    void sendMessage (String msg) {
        if (!this.connect) return;

        JsonObject jsonObject = new JsonObject();
        JsonArray m = new JsonArray();
        m.add(msg);
        jsonObject.add("m", m);

        try {
            URL url = new URL(this.mUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("POST");
            connection.connect();

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(jsonObject.toString());
            osw.close();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                this.logger.warn("ServerHandler.sendMessage> get wrong responseCode: " + responseCode
                        + ", Reason:" +  connection.getResponseMessage());
            }
        }catch (MalformedURLException err) {
            this.logger.error("ServerHandler.sendMessage> send error");
            this.logger.error(err);
        } catch (IOException err) {
            this.logger.error("ServerHandler.sendMessage> send error");
            this.logger.error(err);
        }
    }

    void doUpdate () {
        try {
            if (this.connect) {
                URL url = new URL(this.uUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    // Blank response
                    return;
                } else if (responseCode == 200) {
                    // Has update message
                    this.logger.error("DEBUG TYPE: " + connection.getContent().getClass().getName());

                    /*
                    url = new URL(desiredUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // just want to do an HTTP GET here
      connection.setRequestMethod("GET");

      // uncomment this if you want to write output to this url
      //connection.setDoOutput(true);

      // give it 15 seconds to respond
      connection.setReadTimeout(15*1000);
      connection.connect();

      // read the output from the server
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      stringBuilder = new StringBuilder();

      String line = null;
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line + "\n");
      }
      return stringBuilder.toString();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        stringBuilder.append(line + "\n");
                    }
                    String result = stringBuilder.toString();
                    reader.close();

                    JsonParser parser = new com.google.gson.JsonParser();
            parser.parse()
                     */
                } else {
                    if (!this.wasWrongResponse) {
                        this.wasWrongResponse = true;
                        this.logger.error("DEBUG SERVER WRONG");
                    }
                }
                this.wasWrongResponse = false;
            } else {
                URL url = new URL(this.cUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();

                this.connect = true;
            }
        } catch (Exception err) {
            this.errorCount++;
            this.ctx.logger.error("ServerHandler.doUpdate> Unexpected error occur");
            this.ctx.logger.error(err);
            // TODO: alert to user
        }
    }
}

class InternalLooper implements Runnable {
    private KakaoMinecraftSynchronize ctx;
    private int interval;

     InternalLooper (KakaoMinecraftSynchronize ctx, int interval) {
        this.ctx = ctx;
        this.interval = interval;
    }

    @Override
    public void run () {
        while (true) {
            if (ctx.serverHandler.getErrorCount() >= 5) {
                ctx.logger.info("Update fail more then 5 times. Update disabled.");
              return;
            }
            ctx.serverHandler.doUpdate();
            try { Thread.sleep(this.interval); } catch(InterruptedException e) { return; }
        }
    }
}
