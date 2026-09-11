package com.ni.external.multipleimageselect.adapters;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ni.external.multipleimageselect.models.Album;
import com.ni.SnatchItUp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Darshan on 4/14/2015.
 */
public class CustomAlbumSelectAdapter extends CustomGenericAdapter<Album> {
    public CustomAlbumSelectAdapter(Context context, ArrayList<Album> albums) {
        super(context, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_album_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_album_image);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view_album_name);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.textView.setText(arrayList.get(position).bucketName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Picasso.get()
                    .load(arrayList.get(position).bucketUri)
                    .placeholder(R.drawable.image_placeholder)
                    .resize(size, size)
                    .centerCrop()
                    .into(viewHolder.imageView);
        } else {
            Picasso.get()
                    .load(new File(arrayList.get(position).bucketPath))
                    .placeholder(R.drawable.image_placeholder)
                    .resize(size, size)
                    .centerCrop()
                    .into(viewHolder.imageView);
        }

        /*Glide.with(context)
                .load(arrayList.get(position).path)
                .placeholder(R.drawable.image_placeholder)
                .override(size,size)
                .centerCrop()
                .into(viewHolder.imageView);*/


        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
