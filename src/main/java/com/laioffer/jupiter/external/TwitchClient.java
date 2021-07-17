package com.laioffer.jupiter.external;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
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
import java.util.*;

public class TwitchClient {
    private static final String TOKEN = "Bearer m2hkdihnbsozjybd6j7l5ajdxpca69";
    private static final String CLIENT_ID = "e4cky6mvs2rhmzp05f0ut7qxvd1ip4";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/%s";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    private String buildGameURL (String url, String gameName, int limit) {
        if (gameName == null) {
            return String.format(url, limit);
        } else {
            try {
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new TwitchException("URL conversion failed");
            }
            return String.format(url, gameName);
        }
    }

    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
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

    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            throw new TwitchException("Failed to parse json data from Twitch");
        }
    }

    private List<Item> getItemList(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            throw new TwitchException("Failed to parse json data from Twitch");
        }
    }

    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        return getGameList((searchTwitch(buildGameURL(TOP_GAME_URL, null, limit))));
    }

    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }

    public List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            try {
                item.setType(ItemType.STREAM);
                String boardcastName = URLEncoder.encode(item.getBroadcasterName(), "UTF-8");
                item.setUrl(String.format(TWITCH_BASE_URL, boardcastName));
            } catch (UnsupportedEncodingException e) {
                throw new TwitchException("URL conversion failed");
            }
        }
        return streams;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }
}

