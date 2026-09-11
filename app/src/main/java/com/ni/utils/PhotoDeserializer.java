package com.ni.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ni.model.HomeItemResponse;

import java.lang.reflect.Type;

/**
 * Affiliate APIs may return {@code photos} as URL strings instead of photo objects.
 */
public class PhotoDeserializer implements JsonDeserializer<HomeItemResponse.Photo> {

    @Override
    public HomeItemResponse.Photo deserialize(JsonElement json, Type typeOfT,
                                              JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return new HomeItemResponse().new Photo();
        }
        if (json.isJsonPrimitive()) {
            return PhotoUtils.photoFromUrl(json.getAsString());
        }
        if (!json.isJsonObject()) {
            return new HomeItemResponse().new Photo();
        }

        JsonObject obj = json.getAsJsonObject();
        HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
        photo.setItemUrlMainOriginal(firstString(obj, "item_url_main_original", "original"));
        photo.setItemUrlMain350(firstString(obj, "item_url_main_350", "item_url_main_80", "thumb"));
        photo.setItemImage(firstString(obj, "item_image", "url", "src"));
        photo.setImage(asString(obj, "image"));
        photo.setPath(asString(obj, "path"));
        photo.setHeight(asString(obj, "height"));
        photo.setWidth(asString(obj, "width"));
        photo.setType(asString(obj, "type"));
        return photo;
    }

    private String firstString(JsonObject obj, String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = asString(obj, key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private String asString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return null;
    }
}
