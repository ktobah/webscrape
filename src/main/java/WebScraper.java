
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WebScraper {

    public WebScraper() throws IOException {

        final String USER_AGENT = "\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36\"";
        final String url = "https://isisn.nsfc.gov.cn/egrantindex/funcindex/prjsearch-list";

        Map cookies;
        HashMap<String, String> formData = new HashMap<>();

        //Connection conn = Jsoup.connect("https://isisn.nsfc.gov.cn/egrantindex/funcindex/prjsearch-list");
        Connection.Response conn = Jsoup.connect(url).userAgent(USER_AGENT).execute();
        //Document doc = conn.get();
        Document doc = conn.parse();
        cookies = conn.cookies();

        Element captcha = doc.selectFirst("#img_checkcode");

        Connection.Response fetchCaptcha = Jsoup //
                .connect(captcha.absUrl("src")) // Extract image absolute URL
                .cookies(cookies) // Grab cookies
                .ignoreContentType(true) // Needed for fetching image
                .execute();

        cookies.putAll(fetchCaptcha.cookies());

        // Load image from Jsoup response
        String captchaValue = DecodeCaptcha.decodeCaptcha(writeCaptchaToDisk(new ByteArrayInputStream(fetchCaptcha.bodyAsBytes())));
        System.out.println("Captcha: "+captchaValue);

        String resultData = "prjNo:,ctitle:,psnName:,orgName:,subjectCode:,f_subjectCode_hideId:,subjectCode_hideName:,keyWords:,checkcode:"+captchaValue+
                ",grantCode:339,subGrantCode:,helpGrantCode:,year:2018";

        formData.put("resultDate", resultData);
        formData.put("checkcode", captchaValue);

        Connection connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .timeout(0)
                .method(Connection.Method.POST);

        connection.data(formData);

        Connection.Response response = connection.execute();
        cookies.putAll(response.cookies());

        //This demonstrates that the server returns a different captcha even when cookies are used.
        Connection.Response fetchCaptcha1 = Jsoup //
                .connect("https://isisn.nsfc.gov.cn/egrantindex/validatecode.jpg") // Extract image absolute URL
                .cookies(cookies) // Grab cookies
                .ignoreContentType(true) // Needed for fetching image
                .execute();

        System.out.println("Captcha: "+DecodeCaptcha.decodeCaptcha(writeCaptchaToDisk(new ByteArrayInputStream(fetchCaptcha1.bodyAsBytes()))));

        /*//Send POST request
        Connection.Response homePage = Jsoup.connect(url)
                .cookies(cookies)
                .data(formData)
                .method(Connection.Method.POST)
                .userAgent(USER_AGENT)
                .execute();

        System.out.println(response.parse().html());*/
    }

    public static void main(String[] args) throws IOException {
        new WebScraper();
    }

    private static String writeCaptchaToDisk(ByteArrayInputStream image){

        BufferedImage imageTem;

        try {
            imageTem = ImageIO.read(image);
            ImageIO.write(imageTem, "png",new File("captcha.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "captcha.png";
    }
}
