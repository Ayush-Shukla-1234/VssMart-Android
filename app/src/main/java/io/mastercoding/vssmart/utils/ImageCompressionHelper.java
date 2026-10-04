package io.mastercoding.vssmart.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * High-performance client-side Image Compression utility for marketplace item uploads.
 * Downscales images proportionally (max 1080px) and compresses to JPEG at 75% quality.
 */
public class ImageCompressionHelper {

    private static final String TAG = "CompressionHelper";
    private static final int MAX_DIMENSION = 1080;
    private static final int COMPRESSION_QUALITY = 75;

    public static byte[] compressImage(Context context, Uri imageUri) {
        return compressImage(context, imageUri, MAX_DIMENSION, MAX_DIMENSION, COMPRESSION_QUALITY);
    }

    public static byte[] compressImage(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        if (context == null || imageUri == null) {
            Log.e(TAG, "Context or imageUri is null");
            return null;
        }

        try {
            // Step 1: Decode image dimensions only
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            if (input == null) return null;

            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, boundsOptions);
            input.close();

            int origWidth = boundsOptions.outWidth;
            int origHeight = boundsOptions.outHeight;

            if (origWidth <= 0 || origHeight <= 0) {
                Log.e(TAG, "Invalid image dimensions: " + origWidth + "x" + origHeight);
                return null;
            }

            // Step 2: Compute inSampleSize to prevent OOM
            int inSampleSize = 1;
            while ((origWidth / (inSampleSize * 2)) >= maxWidth || (origHeight / (inSampleSize * 2)) >= maxHeight) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
            sampleOptions.inJustDecodeBounds = false;
            sampleOptions.inSampleSize = inSampleSize;

            InputStream sampleInput = context.getContentResolver().openInputStream(imageUri);
            if (sampleInput == null) return null;

            Bitmap sampledBitmap = BitmapFactory.decodeStream(sampleInput, null, sampleOptions);
            sampleInput.close();

            if (sampledBitmap == null) return null;

            // Step 3: Exact proportional downscaling so max dimension <= 1080px
            int width = sampledBitmap.getWidth();
            int height = sampledBitmap.getHeight();
            float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);

            Bitmap scaledBitmap;
            if (scale < 1.0f) {
                int targetWidth = Math.max(1, Math.round(width * scale));
                int targetHeight = Math.max(1, Math.round(height * scale));
                scaledBitmap = Bitmap.createScaledBitmap(sampledBitmap, targetWidth, targetHeight, true);
                if (scaledBitmap != sampledBitmap) {
                    sampledBitmap.recycle();
                }
            } else {
                scaledBitmap = sampledBitmap;
            }

            // Step 4: Compress to JPEG byte array at target quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            scaledBitmap.recycle();

            byte[] compressedBytes = baos.toByteArray();
            Log.d(TAG, "Image compressed successfully: " + (compressedBytes.length / 1024) + " KB");
            return compressedBytes;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }
}
