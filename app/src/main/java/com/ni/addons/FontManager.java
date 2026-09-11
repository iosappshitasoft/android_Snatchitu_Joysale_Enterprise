package com.ni.addons;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.ni.SnatchItUp.JoysaleApplication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class FontManager {
    private static FontManager INSTANCE;

    private AssetManager mAssetManager;

    private Map<String, Typeface> mFonts;

    private final Locale mLocale;

    private FontManager(AssetManager assetManager, Locale locale) {
        mAssetManager = assetManager;
        mFonts = new HashMap<>();
        mLocale = locale;
    }

    public static void init(AssetManager assetManager, Locale locale) {
        INSTANCE = new FontManager(assetManager, locale);
    }

    public static FontManager getInstance(Locale locale) {
        if (INSTANCE == null) {
            AssetManager assetManager = JoysaleApplication.getApplicationContext2().getAssets();
            init(assetManager, locale);
            //Timber.d("Locale: %s", locale.getLanguage());
        }
        return INSTANCE;
    }

    public Typeface getFont(@NonNull String asset) {
        if (mFonts.containsKey(asset))
            return mFonts.get(asset);

        Typeface font = null;
        final String fontFilePrefix = mLocale.getLanguage().equals("ar") ?
                "fonts/ar/" : "fonts/en/";
        try {
            font = Typeface.createFromAsset(mAssetManager, fontFilePrefix + asset);
            mFonts.put(asset, font);
        } catch (Exception e) {

        }

        if (font == null) {
            try {
                String fixedAsset = fixAssetFilename(asset);
                //Timber.d("Font loaded: fixed: %s", fixedAsset);
                font = Typeface.createFromAsset(mAssetManager, fontFilePrefix + fixedAsset);
                mFonts.put(asset, font);
                mFonts.put(fixedAsset, font);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Timber.d("Font loaded: %s", fontFilePrefix + asset);
        return font;
    }

    @Nullable
    public static Typeface getFontForLocale(@NonNull AssetManager assetManager, @NonNull String language) {
        if (TextUtils.isEmpty(language)) return null;
        final String fontFilePrefix = language.equals("ar") ?
                "fonts/ar/" : "fonts/en/";
        try {
            return Typeface.createFromAsset(assetManager, fontFilePrefix + "font_regular.ttf");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String fixAssetFilename(String asset) {
        if (TextUtils.isEmpty(asset)) return asset;

        if ((!asset.endsWith(".ttf")) && (!asset.endsWith(".ttc"))) {
            asset = String.format("%s.ttf", asset);
        }
        return asset;
    }

    public static boolean destroy() {
        if (INSTANCE != null) {
            INSTANCE = null;
            return true;
        }
        return false;
    }
}