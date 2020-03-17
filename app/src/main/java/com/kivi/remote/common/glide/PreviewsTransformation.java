package com.kivi.remote.common.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import androidx.annotation.NonNull;

import static com.kivi.remote.common.Constants.SMALL_BITMAP;

public class PreviewsTransformation extends BitmapTransformation {

    private int mRadius;
    private int oldApiMargins = 0;

    private static String STRING_CHARSET_NAME = "UTF-8";
    private static final String ID = "com.kivi.remote.common.glide.PreviewsTrasnform";
    private static Charset CHARSET = Charset.forName(STRING_CHARSET_NAME);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    public PreviewsTransformation(int mRadius) {
        this.mRadius = mRadius;
    }


    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() >= SMALL_BITMAP) {
            return TransformationUtils.roundedCorners(pool, TransformationUtils.centerCrop(pool, toTransform, outWidth,outHeight), mRadius);
        } else {
            return TransformationUtils.fitCenter(pool, toTransform, outWidth,outHeight);
        }
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