package com.semteul.kakaominecraftsyncronize;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.minecraft.util.text.TextFormatting;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ServerHandler {
    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    private final KakaoMinecraftSynchronize ctx;
    private final Logger logger;
    private final String cUrl;
    private final String uUrl;
    private final String mUrl;
    private final int MAX_ERROR_COUNT = 5;
    private final int MAX_SEND_QUEUE = 4;

    private boolean connect = false;
    private int errorCount = 0;
    private LinkedList<String> sendQueue = new LinkedList<String>();

    ServerHandler (KakaoMinecraftSynchronize ctx) {
        this.logger = ctx.logger;
        this.ctx = ctx;
        this.cUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/c";
        this.uUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/u";
        this.mUrl = ConfigManager.KakaoMinecraftSynchronizeConfig.serverOrigin + "/m/m";

        SERVICE.execute(new InternalLooper(ConfigManager.KakaoMinecraftSynchronizeConfig.interval));
    }
    
    void handleUnexpectedError(Exception err) {
    	this.logger.error("========== UNEXPECTED ERROR OCCUR ==========");
    	this.logger.error(err);
    	this.errorCount++;
    	if (this.isMaxErrorCountReach()) {
    		this.logger.error("Unexpected error occur more then " 
    				+ this.MAX_ERROR_COUNT + " times. Service disabled.");
    	}
    }
    
    void handleMalformedURLException(Exception err) {
    	this.errorCount = this.MAX_ERROR_COUNT;
    	this.logger.error("Malformed URL detected. Please change url in config (ex: http://example.com:3000)");
    }

    int getErrorCount() {
        return this.errorCount;
    }
    
    boolean isMaxErrorCountReach() {
    	return this.errorCount == this.MAX_ERROR_COUNT;
    }

    boolean isReady() {
        return KakaoMinecraftSynchronize.hasServer() && this.connect && !this.isMaxErrorCountReach();
    }

    void sendMessage(String msg) {
        if (!this.isReady()) {
        	// Queue messages instead of sending request
        	this.sendQueue.add(msg);
        	if (this.sendQueue.size() > this.MAX_SEND_QUEUE) {
        		this.sendQueue.removeFirst();
        	}
        	return;
        }

        // send queued message first
        // TODO: Suspect concurrency issues
        if (this.sendQueue.size() != 0) this.sendQueuedMessages();
        
        // Create jsonObject
        JsonObject jsonObject = new JsonObject();
        JsonArray m = new JsonArray();
        m.add(msg);
        jsonObject.add("m", m);

        try {
        	// Make request
            URL url = new URL(this.mUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("POST");
            connection.connect();

            // Send message with encoding
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(jsonObject.toString());
            osw.close();

            // Response check
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                this.logger.warn("ServerHandler.sendMessage> get wrong responseCode: " + responseCode
                        + ", Reason:" +  connection.getResponseMessage());
            }
        } catch (MalformedURLException err) {
            this.handleMalformedURLException(err);
        } catch (IOException err) {
            if (err.getMessage().equals("Connection refused: connect")) {
            	this.connect = false;
            	return;
            }
            this.handleUnexpectedError(err);
        }
    }
    
    void sendQueuedMessages() {
    	if (this.sendQueue.size() == 0) return;
    	
    	// Create jsonObject
        JsonObject jsonObject = new JsonObject();
        JsonArray m = new JsonArray();
        while (!sendQueue.isEmpty()) m.add(sendQueue.pollFirst());
        jsonObject.add("m", m);

        try {
        	// Make request
            URL url = new URL(this.mUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("POST");
            connection.connect();

            // Send message with encoding
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            osw.write(jsonObject.toString());
            osw.close();

            // Response check
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                this.logger.warn("ServerHandler.sendMessage> get wrong responseCode: " + responseCode
                        + ", Reason:" +  connection.getResponseMessage());
            }
        } catch (MalformedURLException err) {
            this.handleMalformedURLException(err);
        } catch (IOException err) {
            if (err.getMessage().equals("Connection refused: connect")) {
            	this.connect = false;
            	return;
            }
            this.handleUnexpectedError(err);
        }
    }

    void doUpdate() {
        try {
        	// if last connection is connected
            if (this.connect) {
            	// send queued message first
            	// TODO: Suspect concurrency issues
            	if (this.sendQueue.size() != 0) this.sendQueuedMessages();
            	
            	// Make request
                URL url = new URL(this.uUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");

                // Response code check
                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    // Blank response
                    return;
                } else if (responseCode == 200) {
                    // Has update message
                	// Get content with encoding
                    InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }

                    // Parse json
                    JsonParser parser = new JsonParser();
                    JsonObject jsonObject = (JsonObject) parser.parse(sb.toString());
                    JsonArray m = jsonObject.getAsJsonArray("m");
                    int count = m.size();
                    for (int i = 0; i < count; i++) {
                        // TODO: change format
                        this.ctx.sendMessageAll(TextFormatting.YELLOW + m.get(i).getAsString());
                    }
                } else {
                	this.logger.warn("ServerHandler.doUpdate> get wrong responseCode: " + responseCode
                            + ", Reason:" +  connection.getResponseMessage());
                }
            // if last connection isn't connected
            } else {
            	// try connect request
                URL url = new URL(this.cUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                
                // Response code check
                if (connection.getResponseCode() == 200) {
                	this.logger.info("MiddleServer connected");
                    this.connect = true;
                }
            }
        } catch (MalformedURLException err) {
            this.handleMalformedURLException(err);
        } catch (IOException err) {
            if (err instanceof ConnectException) {
            	if (this.connect) {
            		this.logger.warn("disconnected from MiddleServer");
            		this.connect = false;
            	}
            	return;
            }
            this.handleUnexpectedError(err);
        } catch (JsonParseException err) {
        	this.logger.warn("MiddleServer response invalid JSON");
        	this.logger.warn(err);
        }
    }
}

class InternalLooper implements Runnable {
    private int interval;

     InternalLooper (int interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        while (true) {
            if (KakaoMinecraftSynchronize.serverHandler.isMaxErrorCountReach()) {
              return;
            }
            KakaoMinecraftSynchronize.serverHandler.doUpdate();
            try { Thread.sleep(this.interval); } catch(InterruptedException e) { return; }
        }
    }
}
