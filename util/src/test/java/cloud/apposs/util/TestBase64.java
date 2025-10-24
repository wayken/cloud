package cloud.apposs.util;

import org.junit.Test;

import java.io.IOException;

public class TestBase64 {
    @Test
    public void testBase64Encode() {
        String value = "Base64StringValu";
        String message = Base64.base64Encode(value);
        System.out.println(message);
        System.out.println(Base64.base64Decode(message));
    }

    @Test
    public void testBase64EncodeFile() throws IOException {
        String message = Base64.encodeFromFile("C://11.png");
        System.out.println(message);
    }
}
