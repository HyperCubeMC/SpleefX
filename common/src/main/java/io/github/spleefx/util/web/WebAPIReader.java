/*
package io.github.spleefx.util.web;

import com.google.gson.reflect.TypeToken;
import io.github.spleefx.data.OfflinePlayerFactory;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.moltenjson.utils.Gsons;

import java.io.IOException;
import java.util.List;

*/
/**
 * A utility class for reading off the web API.
 *//*

public class WebAPIReader {

    private static final String VERSION = "http://spleefx.herokuapp.com/api/version";

    public static void readVersions() {
        Request request = new Builder().url(VERSION).build();
        try (Response response = OfflinePlayerFactory.CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Cannot connect to the web API!");
            }
            String responseBody = response.body().string();
            List<String> list = Gsons.DEFAULT.fromJson(responseBody, new TypeToken<List<String>>() {
            }.getType());
            System.out.println(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
*/
