package com.webscraper;

import org.bytedeco.javacpp.BytePointer;

import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.TessBaseAPI;

public class DecodeCaptcha {

    public static String decodeCaptcha(String imageCaptcha) {
        BytePointer outText;

        TessBaseAPI api = new TessBaseAPI();
        api.SetVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        api.Init("/usr/local/Cellar/tesseract/4.0.0/share/tessdata", "eng");


        // Open input image with leptonica library
        PIX image =  pixRead(imageCaptcha);
        api.SetImage(image);

        // Get OCR result
        outText = api.GetUTF8Text();

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);

        return outText.getString();
    }
}