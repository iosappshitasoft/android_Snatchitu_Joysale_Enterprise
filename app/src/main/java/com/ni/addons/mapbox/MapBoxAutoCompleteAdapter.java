package com.ni.addons.mapbox;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ni.SnatchItUp.R;
import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;

public class MapBoxAutoCompleteAdapter extends ArrayAdapter {

    private static final String TAG = MapBoxAutoCompleteAdapter.class.getSimpleName();
    private MapBoxAutoCompleteInterface mListener;
    public ArrayList<SearchSuggestion> resultLists = new ArrayList<>();
    private Context context;
    private String countryCode, from;
    // Create a new Places client instance.
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private int itemLayout;

    public MapBoxAutoCompleteAdapter(Context context, int itemLayout, String from, MapBoxAutoCompleteInterface mListener) {

        super(context, itemLayout);
        this.context = context;
        this.itemLayout = itemLayout;
        this.mListener = mListener;
        this.from = from;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }
        TextView txtSuggestion = view.findViewById(R.id.txtSuggestion);

        SearchSuggestion item = getItem(position);
        String address = getFormattedAddress(item);
        txtSuggestion.setText(address);
        txtSuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onPlaceClick(resultLists.get(position), position, address);
                }
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return resultLists.size();
    }

    @Override
    public SearchSuggestion getItem(int index) {
        return resultLists.get(index);
    }

    public void getAutoComplete(String input) {/*

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(LocationActivity.BOUNDS);
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request;
        if (from.equals(Constants.TAG_PROFILE)) {
            request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(bounds)
                    //Addon Single Country
//                    .setCountry(countryCode)
                    .setTypeFilter(TypeFilter.REGIONS)
                    .setSessionToken(token)
                    .setQuery("" + input)
                    .build();
        } else {
            request = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(bounds)
                    //Addon Single Country
//                    .setCountry(countryCode)
                    .setSessionToken(token)
                    .setQuery("" + input)
                    .build();
        }
        resultLists = new ArrayList<>();
        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                resultLists.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getPrimaryText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD), null));
            }
            notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getMessage());
            }
        });
    */}

    public interface MapBoxAutoCompleteInterface {
        void onPlaceClick(SearchSuggestion placeAutocomplete, int position, String location);
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setResultLists(ArrayList<SearchSuggestion> resultLists) {
        this.resultLists = resultLists;
    }

    public static String getFormattedAddress(SearchSuggestion item) {
        StringBuilder builder = new StringBuilder();
        builder.append(item.getName());
        builder.append(", ");
        if (item.getAddress() != null && item.getAddress().getLocality() != null) {
            builder.append(item.getAddress().getLocality());
            builder.append(", ");
        }

        if (item.getAddress() != null && item.getAddress().getDistrict() != null) {
            if (item.getAddress() != null && item.getAddress().getPlace() != null) {
                if (item.getAddress().getPlace().equalsIgnoreCase(item.getAddress().getDistrict())) {
                    builder.append(item.getAddress().getPlace());
                    builder.append(", ");
                } else {
                    builder.append(item.getAddress().getPlace());
                    builder.append(", ");
                    builder.append(item.getAddress().getDistrict());
                    builder.append(", ");
                }
            } else {
                if (!item.getName().equalsIgnoreCase(item.getAddress().getDistrict())) {
                    builder.append(item.getAddress().getDistrict());
                    builder.append(", ");
                }
            }
        }

        if (item.getAddress() != null && item.getAddress().getRegion() != null) {
            builder.append(item.getAddress().getRegion());
            builder.append(", ");
        }
        if (item.getAddress() != null && item.getAddress().getCountry() != null) {
            builder.append(item.getAddress().getCountry());
        }
        return builder.toString();
    }
}
