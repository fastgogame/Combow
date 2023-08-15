package io.fastgogame;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SeleniumManager {

    private final DatabaseService databaseService;
    private final MessageReceivedEvent event;

    public SeleniumManager(DatabaseService databaseService, MessageReceivedEvent event) {
        this.databaseService = databaseService;
        this.event = event;
    }

    @NotNull
    private ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");
        return chromeOptions;
    }

    @Nullable
    private WebDriver runChromeInstance() {
        ChromeOptions chromeOptions = getChromeOptions();
        try {
            WebDriver driver = new ChromeDriver(chromeOptions);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (WebDriverException e) {
            return null;
        }
    }

    public enum AuthAction {
        VALIDATE, LAUNCH
    }

    public void authorize(AuthAction authAction, String guildid) {
        WebDriver driver = runChromeInstance();
        String login = databaseService.getLoginData(guildid)[0];
        String password = databaseService.getLoginData(guildid)[1];
        if (driver != null && login != null && password != null) {
            try {
                driver.get("https://ploudos.com/login/");

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("btn-primary")));
                WebElement usernameField = driver.findElement(By.name("username"));
                WebElement passwordField = driver.findElement(By.name("password"));

                usernameField.sendKeys(login);
                passwordField.sendKeys(password);
                loginButton.click();

                switch (authAction) {
                    case VALIDATE -> {
                        if (!isLoginValid(driver)) {
                            event.getAuthor().openPrivateChannel().complete().sendMessage("Invalid login or password").queue();
                        }
                    }
                    case LAUNCH -> {
                        if (isLoginValid(driver)) {
                            launchServer(driver);
                        }
                    }
                }
            } catch (TimeoutException e) {
                event.getChannel().sendMessage("Timeout exceeded").queue();
            } finally {
                driver.quit();
            }
        }
    }

    private boolean isLoginValid(WebDriver driver) {
        Cookie cookie = driver.manage().getCookieNamed("PLOUDOS_SESSION_1");
        return cookie != null;
    }

    private void launchServer(WebDriver driver) {
        driver.get("https://ploudos.com/manage/0/");
        WebDriverWait waitCookies = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement acceptCookies = waitCookies.until(ExpectedConditions.elementToBeClickable(By.className("css-k8o10q")));
        acceptCookies.click();

        WebDriverWait waitStart = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement startButton = waitStart.until(ExpectedConditions.elementToBeClickable(By.className("btn-success")));
        if (startButton.isDisplayed()) {
            startButton.click();
        }
    }
}
