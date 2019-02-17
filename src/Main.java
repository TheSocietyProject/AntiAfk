
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;
import com.sasha.reminecraft.util.TextMessageColoured;
import com.sasha.simplecmdsys.SimpleCommand;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends RePlugin implements SimpleListener {

    public static Main INSTANCE;

    public Main(){
        Main.INSTANCE = this;
    }

    public ILogger logger = LoggerBuilder.buildProperLogger("AntiAFK");
    public Config CFG = new Config();
    private ScheduledExecutorService executorService;


    private Runnable twistTask = () -> {
        if (this.getReMinecraft().minecraftClient == null)
            return;

        if(!this.getReMinecraft().minecraftClient.getSession().isConnected())
            return;

        if(!isInGame())
            return;

        if(!CFG.var_afkWhileConnected && this.getReMinecraft().areChildrenConnected())
            return;

        // if it came here the antiafk can be handled

        Random rand = new Random();
        if (rand.nextBoolean()) {
            this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
        } else {
            float yaw = -90 + (90 - -90) * rand.nextFloat();
            float pitch = -90 + (90 - -90) * rand.nextFloat();
            this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerRotationPacket(true, yaw, pitch));
        }

    };

    @Override
    public void onPluginInit() {
        executorService = Executors.newScheduledThreadPool(1);

        if (CFG.var_antiAfk) {
            executorService.scheduleWithFixedDelay(twistTask, CFG.var_twistIntervalSeconds,
                    CFG.var_twistIntervalSeconds, TimeUnit.SECONDS);
        }

        registerCommands();
    }

    @Override
    public void onPluginEnable() {
        logger.log("AntiAFK plugin enabled");
    }

    @Override
    public void onPluginDisable() {
    }

    @Override
    public void onPluginShutdown() {
        this.executorService.shutdownNow();
    }

    @Override
    public void registerCommands() {
        try{
            ReMinecraft.INGAME_CMD_PROCESSOR.register(AfkCommand.class);
            logger.log("REGISTER COMMAND NOW");
        }catch(Exception e){
            logger.log(e.toString());
        }
    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }

    public boolean isInGame() {
        MinecraftProtocol pckprot = (MinecraftProtocol) this.getReMinecraft().minecraftClient.getSession().getPacketProtocol();
        return pckprot.getSubProtocol() == SubProtocol.GAME;
    }

}
class Config extends Configuration {

    @ConfigSetting public boolean var_afkWhileConnected = false;

    @ConfigSetting public boolean var_antiAfk = true;

    @ConfigSetting public int var_twistIntervalSeconds = 5;

    Config() {
        super("AntiAfk");
    }
}


class AfkCommand extends SimpleCommand{

    ILogger logger = LoggerBuilder.buildProperLogger("AFKCOMMAND");

    public AfkCommand(){
        super("afk");
        logger.log("constructor in AfkCommand");
    }

    @Override
    public void onCommand(){
        logger.log("wow at least onCommmand works");
        boolean beAfk = !Main.INSTANCE.CFG.var_afkWhileConnected;
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from("&7changed beeing afk to " + beAfk)));

        Main.INSTANCE.CFG.var_afkWhileConnected = beAfk;

    }
}