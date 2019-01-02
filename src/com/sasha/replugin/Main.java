package com.sasha.replugin;

import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends RePlugin implements SimpleListener {

    public ILogger logger = LoggerBuilder.buildProperLogger("AntiAFK");
    public Config CFG = new Config("AntiAFK");
    private ScheduledExecutorService executorService;
    private Runnable spamTask = () -> {
        Random rand = new Random();
        this.getReMinecraft().minecraftClient.getSession().send(
                new ClientChatPacket(CFG.var_spamMessages.get(rand.nextInt(CFG.var_spamMessages.size() - 1)))
        );
    };
    private Runnable twistTask = () -> {
        Random rand = new Random();
        if (this.getReMinecraft().minecraftServer.getSessions().isEmpty()) {
            if (rand.nextBoolean()) {
                this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
            } else {
                float yaw = -90 + (90 - -90) * rand.nextFloat();
                float pitch = -90 + (90 - -90) * rand.nextFloat();
                this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerRotationPacket(true, yaw, pitch));
            }
        }
    };

    @Override
    public void onPluginInit() {
        executorService = Executors.newScheduledThreadPool(4);
    }

    @Override
    public void onPluginEnable() {
        if (this.executorService.isTerminated() || this.executorService.isShutdown()) {
            executorService = Executors.newScheduledThreadPool(4);
        }
        if (CFG.var_spamChat) {
            executorService.scheduleWithFixedDelay(spamTask, CFG.var_spamIntervalSeconds,
                    CFG.var_spamIntervalSeconds, TimeUnit.SECONDS);
        }
        if (CFG.var_antiAfk) {
            executorService.scheduleWithFixedDelay(twistTask, CFG.var_twistIntervalSeconds,
                    CFG.var_twistIntervalSeconds, TimeUnit.SECONDS);
        }
        logger.log("AntiAFK plugin enabled");
    }

    @Override
    public void onPluginDisable() {
        this.executorService.shutdownNow();
        logger.log("AntiAFK plugin disabled");
    }

    @Override
    public void onPluginShutdown() {
        this.executorService.shutdownNow();
        logger.log("AntiAFK plugin shutdown");
    }

    @Override
    public void registerCommands() {

    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }
}
class Config extends Configuration {
    @ConfigSetting public boolean var_spamChat = false;
    @ConfigSetting public boolean var_antiAfk = true;
    @ConfigSetting public int var_spamIntervalSeconds = 60;
    @ConfigSetting public int var_twistIntervalSeconds = 5;
    @ConfigSetting public ArrayList<String> var_spamMessages = new ArrayList<>();
    {
        var_spamMessages.add("Spam :D!");
        var_spamMessages.add("Spam D:!");
    }
    Config(String configName) {
        super(configName);
    }
}
