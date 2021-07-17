package com.laioffer.jupiter.external;


import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TwitchClient {
    private static final String TOKEN = "Bearer m2hkdihnbsozjybd6j7l5ajdxpca69";
    private static final String CLIENT_ID = "e4cky6mvs2rhmzp05f0ut7qxvd1ip4";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private String buildGameURL (String url, String gameName, int limit) {
        if (gameName == null) {
            return String.format(url, limit);
        } else {
            try {
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = httpResponse -> {
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new TwitchException("Failed to get response from twitch");
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get response body from twitch");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", TOKEN);
        request.setHeader("Client-Id", CLIENT_ID);
        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            throw new TwitchException("Failed to get response body from Twitch");
        }
    }
}
