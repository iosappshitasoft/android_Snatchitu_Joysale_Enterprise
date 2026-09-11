package com.ni.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.HomeItemResponse;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Shared helpers for admin discount UI on listing cards and detail.
 */
public final class DiscountUtils {

    private DiscountUtils() {
    }

    public static boolean isDarkTheme(Context context) {
        return (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Light theme: white section, black text, light input.
     * Dark theme: black section, white text, dark input.
     */
    public static void applyPromoCodeTheme(Context context,
                                           View section,
                                           View inputContainer,
                                           TextView title,
                                           TextView valueView,
                                           EditText editText) {
        if (context == null) {
            return;
        }
        boolean isDark = isDarkTheme(context);
        int sectionBg = isDark ? Color.BLACK : Color.WHITE;
        int textColor = isDark ? Color.WHITE : Color.BLACK;
        int hintColor = Color.parseColor("#888888");
        int inputBg = isDark ? Color.parseColor("#2B2B2B") : Color.parseColor("#F2F2F2");
        int stroke = isDark ? Color.parseColor("#2B2B2B") : Color.parseColor("#E0E0E0");

        if (section != null) {
            section.setBackgroundColor(sectionBg);
        }
        if (title != null) {
            title.setTextColor(textColor);
        }
        if (valueView != null) {
            valueView.setTextColor(textColor);
        }
        if (editText != null) {
            editText.setTextColor(textColor);
            editText.setHintTextColor(hintColor);
        }
        if (inputContainer != null) {
            float density = context.getResources().getDisplayMetrics().density;
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(inputBg);
            bg.setCornerRadius(12f * density);
            bg.setStroke(Math.max(1, Math.round(density)), stroke);
            inputContainer.setBackground(bg);
        }
    }

    public static void bindListingDiscount(Context context,
                                           HomeItemResponse.Item item,
                                           TextView priceView,
                                           TextView originalPriceView,
                                           TextView discountBadgeView) {
        if (item == null) {
            return;
        }

        boolean hasDiscount = item.hasDiscount()
                && item.getPrice() != null
                && !item.getPrice().equals("0");

        if (item.getPrice() != null && item.getPrice().equals("0")) {
            if (priceView != null) {
                priceView.setText(context.getString(R.string.giving_away));
                priceView.setTextColor(context.getResources().getColor(R.color.primary));
            }
            if (originalPriceView != null) {
                originalPriceView.setVisibility(View.GONE);
            }
            if (discountBadgeView != null) {
                discountBadgeView.setVisibility(View.GONE);
            }
            return;
        }

        if (hasDiscount) {
            try {
                String percentValue = item.getEffectiveDiscountPercent();
                double original = Double.parseDouble(item.getPrice());
                double percent = Double.parseDouble(percentValue);
                double discounted = original - (original * percent / 100d);
                if (priceView != null) {
                    priceView.setText(formatDiscountPrice(item, discounted));
                    priceView.setTextColor(context.getResources().getColor(R.color.primaryText));
                }
                if (originalPriceView != null) {
                    originalPriceView.setVisibility(View.VISIBLE);
                    originalPriceView.setText(item.getFormattedPrice());
                    originalPriceView.setPaintFlags(originalPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (discountBadgeView != null) {
                    discountBadgeView.setVisibility(View.VISIBLE);
                    discountBadgeView.setText(context.getString(
                            R.string.discount_off_format,
                            cleanPercent(percentValue)));
                    applyCornerRibbon(context, discountBadgeView, 28, 14);
                }
            } catch (NumberFormatException e) {
                if (priceView != null) {
                    priceView.setText(item.getFormattedPrice());
                    priceView.setTextColor(context.getResources().getColor(R.color.primaryText));
                }
                if (originalPriceView != null) {
                    originalPriceView.setVisibility(View.GONE);
                }
                if (discountBadgeView != null) {
                    discountBadgeView.setVisibility(View.GONE);
                }
            }
        } else {
            if (priceView != null) {
                priceView.setText(item.getFormattedPrice());
                priceView.setTextColor(context.getResources().getColor(R.color.primaryText));
            }
            if (originalPriceView != null) {
                originalPriceView.setVisibility(View.GONE);
            }
            if (discountBadgeView != null) {
                discountBadgeView.setVisibility(View.GONE);
            }
        }
    }

    public static void bindAffiliateLabel(Context context,
                                          HomeItemResponse.Item item,
                                          TextView affiliateLabel,
                                          View priceLay) {
        if (affiliateLabel == null) {
            return;
        }
        if (item != null
                && !"".equals(item.getPromoCode())
                && !"".equals(item.getDiscountPercentage())) {
            affiliateLabel.setVisibility(View.VISIBLE);
            affiliateLabel.setText(context.getString(R.string.discounted));
            affiliateLabel.setTextColor(context.getResources().getColor(R.color.primary));
            applyPriceTopMargin(context, priceLay, 2);
        } else {
            affiliateLabel.setVisibility(View.GONE);
            applyPriceTopMargin(context, priceLay, 5);
        }
    }

    private static void applyPriceTopMargin(Context context, View priceLay, int marginDp) {
        if (priceLay == null || !(priceLay.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams priceParams =
                (ViewGroup.MarginLayoutParams) priceLay.getLayoutParams();
        int marginPx = JoysaleApplication.dpToPx(context, marginDp);
        if (priceParams.topMargin != marginPx) {
            priceParams.topMargin = marginPx;
            priceLay.setLayoutParams(priceParams);
        }
    }

    /**
     * Places the diagonal discount sash on the physical top-left corner
     * in every language, matching the iOS Arabic layout.
     */
    public static void applyCornerRibbon(Context context, View ribbon, int overhangDp, int topDp) {
        if (context == null || ribbon == null || ribbon.getLayoutParams() == null) {
            return;
        }
        int overhang = JoysaleApplication.dpToPx(context, overhangDp);
        int top = JoysaleApplication.dpToPx(context, topDp);
        ribbon.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ribbon.setTextDirection(View.TEXT_DIRECTION_LTR);
        ViewGroup.LayoutParams raw = ribbon.getLayoutParams();
        if (raw instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) raw;
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.topMargin = top;
            lp.bottomMargin = 0;
            lp.setMarginStart(Integer.MIN_VALUE);
            lp.setMarginEnd(Integer.MIN_VALUE);
            lp.leftMargin = -overhang;
            lp.rightMargin = 0;
            ribbon.setRotation(-45f);
            ribbon.setLayoutParams(lp);
        } else if (raw instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) raw;
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            lp.topMargin = top;
            lp.bottomMargin = 0;
            lp.setMarginStart(Integer.MIN_VALUE);
            lp.setMarginEnd(Integer.MIN_VALUE);
            lp.leftMargin = -overhang;
            lp.rightMargin = 0;
            ribbon.setRotation(-45f);
            ribbon.setLayoutParams(lp);
        }
    }

    public static String cleanPercent(String discountPercent) {
        if (discountPercent == null) {
            return "";
        }
        if (discountPercent.contains(".")) {
            return discountPercent.replaceAll("\\.?0+$", "");
        }
        return discountPercent;
    }

    public static String formatDiscountPrice(HomeItemResponse.Item item, double discountedPrice) {
        NumberFormat priceFormat = NumberFormat.getInstance(Locale.US);
        priceFormat.setGroupingUsed(false);
        if (Math.abs(discountedPrice - Math.rint(discountedPrice)) < 0.0001d) {
            priceFormat.setMaximumFractionDigits(0);
            priceFormat.setMinimumFractionDigits(0);
        } else {
            priceFormat.setMaximumFractionDigits(2);
            priceFormat.setMinimumFractionDigits(0);
        }
        String discountedNumber = priceFormat.format(discountedPrice);
        String formattedOriginal = item.getFormattedPrice();
        if (formattedOriginal != null && !formattedOriginal.trim().isEmpty()) {
            return formattedOriginal.replaceFirst("[0-9][0-9,\\.]*\\s*$", discountedNumber);
        }
        String symbol = "$";
        if (item.getCurrencyCode() != null && item.getCurrencyCode().contains("-")) {
            symbol = item.getCurrencyCode().split("-")[0];
        } else if (item.getCurrencySymbol() != null && !item.getCurrencySymbol().isEmpty()) {
            symbol = item.getCurrencySymbol();
        }
        return symbol + " " + discountedNumber;
    }
}
