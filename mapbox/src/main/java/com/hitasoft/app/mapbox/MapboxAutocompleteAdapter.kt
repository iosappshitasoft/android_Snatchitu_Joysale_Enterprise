package com.ni.mapbox

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.mapbox.R
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.mapbox.search.result.SearchSuggestion

class MapboxAutocompleteAdapter internal constructor(
    context: Context?,
    itemLayout: Int,
    from: String,
    mListener: MapBoxAutoCompleteInterface?
) : ArrayAdapter<SearchSuggestion>(context!!, itemLayout) {

    public var mListener: MapBoxAutoCompleteInterface? = mListener
    private var resultLists: ArrayList<SearchSuggestion> = ArrayList<SearchSuggestion>()
    public var mContext: Context? = context
    private var countryCode: String? = null
    public var from: String? = from
    var tempLocation: String? = null

    // Create a new Places client instance.
    private val STYLE_BOLD: CharacterStyle = StyleSpan(Typeface.BOLD)
    public var itemLayout = itemLayout

    /*    fun MapBoxAutoCompleteAdapter(
           context: Context?,
           itemLayout: Int,
           from: String,
           mListener: MapBoxAutoCompleteInterface?
       ){
            //super.(context, itemLayout)

           this.context = context
           this.itemLayout = itemLayout
           this.mListener = mListener
           this.from = from
       }
   */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(parent.context)
                .inflate(itemLayout, parent, false)
        }
        val txtSuggestion = view!!.findViewById<TextView>(R.id.txtSuggestion)
        val item: SearchSuggestion = getItem(position)
        val address = getFormattedAddress(item)
        txtSuggestion.text = address
        txtSuggestion.setOnClickListener {
            if (mListener != null) {
                mListener!!.onPlaceClick(resultLists[position], position, address)
            }
        }
        return view
    }

    override fun getCount(): Int {
        return resultLists.size
    }

    override fun getItem(index: Int): SearchSuggestion {
        return resultLists[index]
    }

    fun getAutoComplete(input: String?) { /*

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
    */
    }

    interface MapBoxAutoCompleteInterface {
        fun onPlaceClick(placeAutocomplete: SearchSuggestion?, position: Int, location: String?)
    }

    fun setCountryCode(countryCode: String?) {
        this.countryCode = countryCode
    }

    fun setResultLists(resultLists: ArrayList<SearchSuggestion>) {
        this.resultLists = resultLists
    }

    fun getFormattedAddress(item: SearchSuggestion): String {
        val builder = StringBuilder()
        builder.append(item.name)
        builder.append(", ")
        if (item.address != null && item.address!!.locality != null) {
            builder.append(item.address!!.locality)
            builder.append(", ")
        }
        if (item.address != null && item.address!!.district != null) {
            if (item.address != null && item.address!!.place != null) {
                if (item.address!!.place
                        .equals(item.address!!.district)
                ) {
                    builder.append(item.address!!.place)
                    builder.append(", ")
                } else {
                    builder.append(item.address!!.place)
                    builder.append(", ")
                    builder.append(item.address!!.district)
                    builder.append(", ")
                }
            } else {
                if (!item.name.equals(item.address!!.district)) {
                    builder.append(item.address!!.district)
                    builder.append(", ")
                }
            }
        }
        if (item.address != null && item.address!!.region != null) {
            builder.append(item.address!!.region)
            builder.append(", ")
        }
        if (item.address != null && item.address!!.country != null) {
            builder.append(item.address!!.country)
        }
        return builder.toString()
    }
}

class AutocompleteCallback : DiffUtil.ItemCallback<SearchSuggestion>() {
    companion object {
        val TAG = AutocompleteCallback::class.simpleName
    }

    override fun areItemsTheSame(
        oldItem: SearchSuggestion,
        newItem: SearchSuggestion
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SearchSuggestion,
        newItem: SearchSuggestion
    ): Boolean {
        return oldItem == newItem
    }
}
