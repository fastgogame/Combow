import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotHost extends ListenerAdapter{
    public static void main(String[] args) throws Exception {
        String token = "MTEzNTY1NjQyOTU5Njk3NTEwNg.G2pbyN.u6OdCf5lZPtZkjaBDIrhKQp-7gymsLsHPrWZmU";
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new BotHost())
                .build();
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!startserver")) {
            // Ваш код для запуска сервера Minecraft на хостинге
            event.getChannel().sendMessage("Server started!").queue();
        }
    }
}
