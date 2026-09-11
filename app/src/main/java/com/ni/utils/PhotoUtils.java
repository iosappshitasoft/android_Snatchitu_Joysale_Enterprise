package com.ni.utils;

import android.widget.ImageView;

import com.ni.model.HomeItemResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves listing photo URLs for both regular ads and affiliate products.
 * Affiliate items often send a filename in {@code item_image} (or a plain URL string)
 * instead of {@code item_url_main_original}.
 */
public final class PhotoUtils {

    private static final String S3_PRODUCTS_BASE = "https://snatchitup.s3.amazonaws.com/Products/";

    private PhotoUtils() {
    }

    public static String getFirstDisplayUrl(HomeItemResponse.Item item) {
        return getFirstUrl(item, false);
    }

    public static String getFirstListThumbUrl(HomeItemResponse.Item item) {
        return getFirstUrl(item, true);
    }

    private static String getFirstUrl(HomeItemResponse.Item item, boolean preferThumb) {
        if (item == null || item.getPhotos() == null || item.getPhotos().isEmpty()) {
            return null;
        }
        for (HomeItemResponse.Photo photo : item.getPhotos()) {
            String url = preferThumb ? getListThumbUrl(photo) : getDisplayUrl(photo);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static String getDisplayUrl(HomeItemResponse.Photo photo) {
        return getGalleryUrl(photo);
    }

    public static String getListThumbUrl(HomeItemResponse.Photo photo) {
        if (photo == null) {
            return null;
        }
        // Prefer the site 350 URL. S3 "original" links are often 403.
        return toAbsoluteUrl(firstNonEmpty(
                photo.getItemUrlMain350(),
                photo.getItemUrlMainOriginal(),
                photo.getItemImage(),
                photo.getImage(),
                photo.getPath()));
    }

    public static String getGalleryUrl(HomeItemResponse.Photo photo) {
        return getListThumbUrl(photo);
    }

    public static void loadListImage(ImageView target, String url, int sizePx, int placeholderRes) {
        loadListImage(target, url, sizePx, placeholderRes, null);
    }

    public static void loadListImage(ImageView target, String url, int sizePx, int placeholderRes, Object requestTag) {
        if (target == null) {
            return;
        }
        String resolved = url == null ? "" : url.trim();
        Picasso picasso = Picasso.get();
        picasso.cancelRequest(target);
        target.setTag(com.ni.SnatchItUp.R.id.list_image_url_tag, resolved);
        if (resolved.isEmpty()) {
            target.setImageResource(placeholderRes);
            return;
        }
        // Do not use resize + centerCrop + onlyScaleDown: 350px thumbs are smaller
        // than the grid cell, and Picasso treats that as a failed load (error placeholder).
        RequestCreator request = picasso.load(resolved)
                .placeholder(placeholderRes)
                .error(placeholderRes);
        if (requestTag != null) {
            request.tag(requestTag);
        }
        request.into(target);
    }

    public static void applyAffiliatePhotos(HomeItemResponse.Item item,
                                            List<HomeItemResponse.Photo> photos) {
        if (item == null || photos == null || photos.isEmpty()) {
            return;
        }
        List<HomeItemResponse.Photo> photoList = new ArrayList<>();
        for (HomeItemResponse.Photo photo : photos) {
            if (getGalleryUrl(photo) != null) {
                photoList.add(photo);
            }
        }
        if (!photoList.isEmpty()) {
            item.setPhotos(photoList);
        }
    }

    public static void applyAffiliatePhotoUrls(HomeItemResponse.Item item, List<String> photoUrls) {
        if (item == null || photoUrls == null || photoUrls.isEmpty()) {
            return;
        }
        List<HomeItemResponse.Photo> photoList = new ArrayList<>();
        for (String photoUrl : photoUrls) {
            if (usableUrl(photoUrl) == null) {
                continue;
            }
            photoList.add(photoFromUrl(photoUrl));
        }
        if (!photoList.isEmpty()) {
            item.setPhotos(photoList);
        }
    }

    public static String toAbsoluteUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || !isUsableRaw(value)) {
            return null;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        if (value.startsWith("/")) {
            String site = Constants.SITE_URL;
            if (site != null && site.endsWith("/")) {
                return site.substring(0, site.length() - 1) + value;
            }
            return (site != null ? site : "") + value;
        }
        return S3_PRODUCTS_BASE + value;
    }

    static HomeItemResponse.Photo photoFromUrl(String url) {
        HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
        String trimmed = url != null ? url.trim() : "";
        photo.setItemUrlMainOriginal(trimmed);
        photo.setItemUrlMain350(trimmed);
        photo.setItemImage(trimmed);
        return photo;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String usable = usableUrl(value);
            if (usable != null) {
                return usable;
            }
        }
        return null;
    }

    private static String usableUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (!isUsableRaw(value)) {
            return null;
        }
        return value;
    }

    private static boolean isUsableRaw(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase();
        return !"null".equals(lower) && !"undefined".equals(lower) && !"false".equals(lower);
    }
}
