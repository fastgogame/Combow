package listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class EventListener extends ListenerAdapter {

    WebDriver driver = new ChromeDriver();
    public void startServer(){
        driver.get("https://ploudos.com/manage/0/");
        //css-k8o10q
        WebElement acceptCookies = driver.findElement(By.className("css-k8o10q"));
        acceptCookies.click();
        WebElement startButton = driver.findElement(By.className("btn-success"));
        startButton.click();
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.equals("!startserver")) {
            driver.get("https://ploudos.com/");
            Cookie authCheck = driver.manage().getCookieNamed("PLOUDOS_SESSION_1");
            if (authCheck != null) {
                startServer();
            } else {
                driver.get("https://ploudos.com/login/");
                WebElement usernameField = driver.findElement(By.name("username"));
                WebElement passwordField = driver.findElement(By.name("password"));
                WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));

                usernameField.sendKeys("fastgogame");
                passwordField.sendKeys("MmAqebQTrL");
                loginButton.click();

                startServer();
            }
            event.getChannel().sendMessage("Server started!").queue();
            driver.quit();
        }
    }
}
