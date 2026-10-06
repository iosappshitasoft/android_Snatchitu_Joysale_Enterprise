package com.ni.utils;

import android.widget.ImageView;

import com.ni.model.HomeItemResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves listing photo URLs for both regular ads and affiliate products.
 * Prefer {@code item_url_main_original}  — site 350 thumbs are
 * often missing for some products while the S3 original still loads.
 * Affiliate items may send a filename in {@code item_image} (or a plain URL string).
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
        // Prefer original (web/iOS). Fall back to 350 when original is missing.
        return toAbsoluteUrl(firstNonEmpty(
                photo.getItemUrlMainOriginal(),
                photo.getItemUrlMain350(),
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
        // Resize large S3 originals for the grid. onlyScaleDown keeps 350px fallbacks
        // from failing when the cell is larger than the bitmap.
        RequestCreator request = picasso.load(resolved)
                .resize(sizePx, sizePx)
                .onlyScaleDown()
                .centerCrop()
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
