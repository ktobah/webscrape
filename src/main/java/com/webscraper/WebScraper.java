package com.webscraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WebScraper {

    private static WebDriver driver;
    private static Screenshot  screenshot;
    private static String captchaValue;
    private static WebElement element;
    private static int numberOfPages;

    public WebScraper() throws IOException, InterruptedException {

        // Initialization of the Selenium driver
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://isisn.nsfc.gov.cn/egrantindex/funcindex/prjsearch-list");
        CSVUtils csvUtils = new CSVUtils();

        // Retrieve the value of the captcha
        captchaValue = findCaptcha();

        Actions actions = new Actions(driver);

        Select select = new Select(driver.findElement(By.id("f_grantCode")));
        //I tested with just one example "重大研究计划".
        select.selectByVisibleText("重大研究计划");

        // Fill in the captcha value on the web page
        actions.click(driver.findElement(By.id("f_checkcode")))
                .sendKeys(captchaValue)
                .build()
                .perform();

        // This will put the process on hold so a human can check if the captcha is decoded correctly or correct it if needed.
        Thread.sleep(10000);

        // Click search to submit the form
        driver.findElement(By.id("searchBt")).click();

        // Retrieve the headers of the table
        String tableHeader = "<table>" + driver.findElement(By.className("ui-jqgrid-htable")).getAttribute("innerHTML") + "<table>";
        csvUtils.tableToCSV(tableHeader);

        // Get the number of pages returned as results
        numberOfPages = (Integer.valueOf(driver.findElement(By.id("sp_1_TopBarMnt")).getText()));

        // Iterate over all the result pages and retrieve the data
        for (int i=0; i<numberOfPages;i++){
            if(i == numberOfPages-1){
                String tableRows = "<table>" + driver.findElement(By.id("dataGrid")).getAttribute("innerHTML") + "<table>";
                CSVUtils.tableToCSV(tableRows);
            }else{
                String tableRows = "<table>" + driver.findElement(By.id("dataGrid")).getAttribute("innerHTML") + "<table>";
                CSVUtils.tableToCSV(tableRows);
                // Stop the process for 3 seconds to make sure the captcha has fully loaded
                Thread.sleep(2000);

                captchaValue = findCaptcha();
                actions.click(driver.findElement(By.id("checkCode")))
                        .sendKeys(captchaValue)
                        .build()
                        .perform();

                // Stop the process for 10 second to allow a human to inspect the decoded captcha
                Thread.sleep(10000);

                // Click "next" to load the next page
                driver.findElement(By.id("next_t_TopBarMnt")).click();
            }

        }

        // Write the scraped data to a CSV file "data.csv"
        CSVUtils.writeCSV();
    }

    // This method take a screenshot of the captcha then decode it using com.webscraper.DecodeCaptcha
    private static String findCaptcha() throws IOException {
        //Find the captcha element
        element = driver.findElement(By.id("img_checkcode"));

        // Get a screenshot of the element
        // The dpr here is very important to get the correct screenshot. My display is 4K, therefore I used "2"
        // You might need to check your dpr and change it.
        // Hint: to get dpr in Chrome or Firefox, open developer tools, type in the console "window.devicePixelRatio"
        screenshot = new AShot().shootingStrategy(ShootingStrategies.scaling(2)).takeScreenshot(driver, element);

        //save the captcha to disk
        ImageIO.write(screenshot.getImage(), "PNG", new File("captcha.png"));

        //Decode captcha
        return DecodeCaptcha.decodeCaptcha("captcha.png");
    }

    public static void main(String[] args) throws Exception {
        new WebScraper();
    }
}