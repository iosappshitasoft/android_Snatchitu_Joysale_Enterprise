package com.ni.external;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.ni.SnatchItUp.R;

import java.lang.reflect.Field;

/**
 * Created by hitasoft on 12/2/16.
 */
public class CustomEditText extends AppCompatEditText {

    private Context context;
    private AttributeSet attrs;
    private int defStyle;

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        this.setTypeface(font);
        applyNightModeCursor();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyNightModeCursor();
    }

    /**
     * The framework caret drawable is solid black and is not tinted on API 29+.
     * Force the night-qualified white cursor so it stays visible on dark backgrounds.
     */
    private void applyNightModeCursor() {
        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (uiMode != Configuration.UI_MODE_NIGHT_YES) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTextCursorDrawable(R.drawable.cursor);
            return;
        }
        try {
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            field.setInt(this, R.drawable.cursor);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        super.setTypeface(tf, style);
    }

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typeface.createFromAsset(getContext().getAssets(), "font_regular.ttf");
        super.setTypeface(tf);
    }
}