import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.util.TextMessageColoured;
import com.sasha.simplecmdsys.SimpleCommand;

public class AfkCommand extends SimpleCommand {


    public AfkCommand(){
        super("afk");
    }

    @Override
    public void onCommand(){

        boolean beAfk = !Main.INSTANCE.CFG.var_afkWhileConnected;
        ReMinecraft.INSTANCE.sendToChildren(new ServerChatPacket(TextMessageColoured.from("&7changed being afk to " + beAfk)));

        Main.INSTANCE.CFG.var_afkWhileConnected = beAfk;

    }
}