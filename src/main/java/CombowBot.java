import io.github.cdimascio.dotenv.Dotenv;
import listeners.EventListener;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import javax.security.auth.login.LoginException;

public class CombowBot extends ListenerAdapter{

    private final Dotenv config;
    private final ShardManager shardManager;

    public CombowBot() throws LoginException{
        config = Dotenv.configure().load();
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setActivity(Activity.watching("Death Note"));
        shardManager = builder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        shardManager.addEventListener(new EventListener());
    }

    public ShardManager getShardManager(){
        return shardManager;
    }

    public static void main(String[] args) {
        try {
            CombowBot bot = new CombowBot();
        } catch (LoginException e) {
            System.out.println("Invalid bot token");
        }
    }
}
