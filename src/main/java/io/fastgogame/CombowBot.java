package io.fastgogame;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import javax.security.auth.login.LoginException;

public class CombowBot extends ListenerAdapter{

    private final ShardManager shardManager;

    public CombowBot() throws LoginException{
        String token = Dotenv.configure().load().get("TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setActivity(Activity.playing("Minecraft"));
        shardManager = builder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        shardManager.addEventListener(new EventListener());
    }

    public static void main(String[] args) {
        try {
            CombowBot bot = new CombowBot();
        } catch (LoginException e) {
            System.err.println("Invalid bot token");
            System.exit(1);
        }
    }
}
