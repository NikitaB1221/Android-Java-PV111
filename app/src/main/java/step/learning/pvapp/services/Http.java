package step.learning.pvapp.services;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Http {
    private static final byte[] buffer = new byte[4096];
    public static String  getString(String url){
        try( InputStream urlStream = new URL(url).openStream()) {
            return readStream(urlStream);
        }
        catch (IOException | android.os.NetworkOnMainThreadException ex) {
            Log.e("Http.getString - Ex:", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    public static String readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        return new String(byteBuilder.toByteArray(), StandardCharsets.UTF_8);
    }
}
