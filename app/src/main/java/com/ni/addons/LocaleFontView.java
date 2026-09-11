package com.ni.addons;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import com.ni.helper.LocaleManager;
import com.ni.SnatchItUp.R;


public class LocaleFontView extends AppCompatTextView {

    public LocaleFontView(Context context) {
        this(context, null);
    }

    public LocaleFontView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocaleFontView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode()) return;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FontText);
        if (ta != null) {
            String fontAsset = ta.getString(R.styleable.FontText_typefaceAsset);

            if (!TextUtils.isEmpty(fontAsset)) {
                Typeface tf = FontManager.getInstance(LocaleManager.getLocale(context.getResources())).getFont(fontAsset);
                int style = Typeface.NORMAL;
                float size = getTextSize();

                if (getTypeface() != null)
                    style = getTypeface().getStyle();

                if (tf != null)
                    setTypeface(tf, style);
                else
                    Log.e("FontText", String.format("Could not create a font from asset: %s", fontAsset));
            }
        }
    }
}