package io.fastgogame;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.SQLException;
import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;


public class EventListener extends ListenerAdapter {

    private boolean loginEditing = false;
    private boolean passwordEditing = false;

    private String guildid;
    private String login;
    private String password;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        if (message.equals("!configure") && event.isFromGuild()) {
            try {
                handleConfigureCommand(event);
            } catch (SQLException e) {
                event.getChannel().sendMessage("Database connection failed").queue();
            }
        } else if (event.isFromType(ChannelType.PRIVATE) && loginEditing) {
            try {
                handleLoginEditing(event, message);
            } catch (SQLException e) {
                sendPrivateMessage(event, "Database connection failed");
            }
        } else if (event.isFromType(ChannelType.PRIVATE) && passwordEditing) {
            try {
                handlePasswordEditing(event, message);
            } catch (SQLException e) {
                sendPrivateMessage(event, "Database connection failed");
            }
        } else if (message.equals("!startserver") && event.isFromGuild()) {
            try {
                handleStartServerCommand(event);
            } catch (SQLException e) {
                event.getChannel().sendMessage("Database connection failed").queue();
            }
        }
    }

    private void handleConfigureCommand(MessageReceivedEvent event) throws SQLException {
        guildid = event.getGuild().getId();
        if (!DatabaseConnector.isGuildExists(guildid)) {
            DatabaseConnector.addGuild(guildid);
            sendPrivateMessage(event, "Welcome! " + event.getGuild().getName() + " has been linked successfully");
        } else {
            sendPrivateMessage(event, "Hi! Configuring server for " + event.getGuild().getName());
        }
        sendPrivateMessage(event, "Send your PloudOS login");
        loginEditing = true;
    }

    private void handleLoginEditing(MessageReceivedEvent event, String message) throws SQLException {
        DatabaseConnector.updateUserLogin(guildid, message);
        sendPrivateMessage(event, "Login has been successfully updated");
        sendPrivateMessage(event, "Send your PloudOS password");
        loginEditing = false;
        passwordEditing = true;
    }

    private void handlePasswordEditing(MessageReceivedEvent event, String message) throws SQLException {
        DatabaseConnector.updateUserPassword(guildid, message);
        sendPrivateMessage(event, "Password has been successfully updated");
        passwordEditing = false;
        //sendPrivateMessage(event, checkLoginDetails(guildid));
    }

    private void handleStartServerCommand(MessageReceivedEvent event) throws SQLException {
        guildid = event.getGuild().getId();
        if (DatabaseConnector.isGuildExists(guildid) && isLoginDetailsFilled()) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            //  /opt/google/chrome/google-chrome
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--remote-debugging-port=9222");
            WebDriver driver = new ChromeDriver(chromeOptions);
            String loginInfo = "Bot service is unavailable";
            try {
                loginInfo = checkLoginDetails(guildid, driver);
                event.getChannel().sendMessage(startServer(driver)).queue();
            } catch (NoSuchSessionException e) {
                event.getChannel().sendMessage(loginInfo).queue();
            }
            driver.quit();
        } else {
            event.getChannel().sendMessage("Login or password fields are not configured. Type \n!configure").queue();
        }
    }

    public String startServer(WebDriver driver){
        driver.get("https://ploudos.com/manage/0/");
        //Accept cookies
        driver.findElement(By.className("css-k8o10q")).click();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement startButton = wait.until(elementToBeClickable(By.className("btn-success")));
            startButton.click();
            return "Server is starting";
        } catch (TimeoutException e) {
            return "Error: Start button did not load within 30 seconds";
        }
    }

    public void sendPrivateMessage(MessageReceivedEvent event, String privateMessage) {
        PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
        privateChannel.sendMessage(privateMessage).queue();
    }

    public boolean isLoginDetailsFilled() throws SQLException {
        String[] loginData = DatabaseConnector.getLoginData(guildid);
        if (loginData != null) {
            login = loginData[0];
            password = loginData[1];
            return true;
        }
        return false;
    }

    public String checkLoginDetails(String guildid, WebDriver driver) {
        driver.get("https://ploudos.com/login/");
        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));

        usernameField.sendKeys(login);
        passwordField.sendKeys(password);
        loginButton.click();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement wrong = wait.until(elementToBeClickable(By.className("alert-danger")));
            if (wrong != null) {
                driver.quit();
            }
        } catch (TimeoutException e) {
            return "Login successful";
        }
        return "Incorrect login or password.Type \n!configure";
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild()
                .getDefaultChannel()
                .asStandardGuildMessageChannel()
                .sendMessage("Hello!\nType !configure to set your login and password on PloudOS.\nType !startserver to start your server.")
                .queue();
    }
}