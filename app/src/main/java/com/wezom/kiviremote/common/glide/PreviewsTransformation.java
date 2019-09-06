package com.wezom.kiviremote.common.glide;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class PreviewsTransformation extends BitmapTransformation {

    private int mRadius;
    private int oldApiMargins = 0;

    private static String STRING_CHARSET_NAME = "UTF-8";
    private static final String ID = "com.wezom.kiviremote.common.glide.PreviewsTrasnform";
    private static Charset CHARSET = Charset.forName(STRING_CHARSET_NAME);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    public PreviewsTransformation(int mRadius) {
        this.mRadius = mRadius;
    }

    public PreviewsTransformation(int mRadius, int oldApiMargins) {
        this.mRadius = mRadius;
        this.oldApiMargins = oldApiMargins;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        outWidth = outWidth - oldApiMargins;
        outHeight = outHeight - oldApiMargins;
        return TransformationUtils.roundedCorners(pool, TransformationUtils.fitCenter(pool, toTransform, outWidth,outHeight), mRadius);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PreviewsTransformation;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}