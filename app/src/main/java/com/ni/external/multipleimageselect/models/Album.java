package com.ni.external.multipleimageselect.models;

import android.net.Uri;

/**
 * Created by Darshan on 4/14/2015.
 */
public class Album {
    public Long bucketId;
    public String bucketName;
    public String bucketPath;
    public Uri bucketUri;

    public Album(Long bucketId, String name, String cover, Uri coverUri) {
        this.bucketId = bucketId;
        this.bucketName = name;
        this.bucketPath = cover;
        this.bucketUri = coverUri;
    }
}
