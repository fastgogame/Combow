package io.fastgogame;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import io.fastgogame.SeleniumManager.AuthAction;

public class EventListener extends ListenerAdapter {

    private boolean loginEditing = false;
    private boolean passwordEditing = false;

    private String guildid;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        DatabaseService databaseService = new DatabaseService(event);
        SeleniumManager seleniumManager = new SeleniumManager(databaseService, event);
        String message = event.getMessage().getContentRaw();
        String commandPrefix = "!";

        if (message.startsWith(commandPrefix)) {
            String command = message.substring(commandPrefix.length());

            if (event.isFromGuild()) {
                guildid = event.getGuild().getId();
                switch (command) {
                    case "configure" -> configureServer(event, databaseService);
                    case "launch" -> seleniumManager.authorize(AuthAction.LAUNCH, guildid);
                }
            }
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            if (loginEditing) {
                editLogin(event, message, databaseService);
            } else if (passwordEditing) {
                editPassword(event, message, databaseService);
                seleniumManager.authorize(AuthAction.VALIDATE, guildid);
            }
        }
    }

    private void configureServer(MessageReceivedEvent event, DatabaseService databaseService) {
        if (!databaseService.hasGuild(guildid)) {
            databaseService.addGuild(guildid);
        }
        sendPrivateMessage(event, "Configuring server for " + event.getGuild().getName() + "\nSend your PloudOS login");
        loginEditing = true;
    }

    private void editLogin(MessageReceivedEvent event, String message, DatabaseService databaseService) {
        databaseService.updateUserLogin(guildid, message);
        sendPrivateMessage(event, "Your login has been updated " + message + "\nSend your PloudOS password");
        loginEditing = false;
        passwordEditing = true;
    }

    private void editPassword(MessageReceivedEvent event, String message, DatabaseService databaseService) {
        databaseService.updateUserPassword(guildid, message);
        sendPrivateMessage(event, "Your password has been updated "
                + message.charAt(0)
                + "*****"
                + message.charAt(message.length() - 1));
        passwordEditing = false;
    }

    private void sendPrivateMessage(MessageReceivedEvent event, String message) {
        PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
        privateChannel.sendMessage(message).queue();
    }
}
