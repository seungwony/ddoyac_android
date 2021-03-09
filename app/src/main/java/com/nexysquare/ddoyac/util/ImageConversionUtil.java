package com.nexysquare.ddoyac.util;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
public class ImageConversionUtil {
    public static Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        //https://developer.android.com/reference/android/media/Image.html#getFormat()
        //https://developer.android.com/reference/android/graphics/ImageFormat#JPEG
        //https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
        if (imageProxy.getFormat() == ImageFormat.JPEG) {
            ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            return bitmap;
        } else if (imageProxy.getFormat() == ImageFormat.YUV_420_888) {
            ByteBuffer yBuffer = imageProxy.getPlanes()[0].getBuffer(); // Y
            ByteBuffer uBuffer = imageProxy.getPlanes()[1].getBuffer(); // U
            ByteBuffer vBuffer = imageProxy.getPlanes()[2].getBuffer(); // V

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            return bitmap;
        }
        return null;
    }

    public static ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1 * bitmap.getWidth() * bitmap.getHeight() * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        for (int y = 0; y < bitmap.getHeight(); ++y) {
            for (int x = 0; x < bitmap.getWidth(); ++x) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                //int r = (pixel >> 16) & 0xFF;
                //int g = (pixel >> 8) & 0xFF;
                //int b = pixel & 0xFF;
                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }
        return byteBuffer;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        byte[] byteArray = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];
        int byteArrayIndex = 0;
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                //int r = (pixel >> 16) & 0xFF;
                //int g = (pixel >> 8) & 0xFF;
                //int b = pixel & 0xFF;
                byteArray[byteArrayIndex++] = (byte)r;
                byteArray[byteArrayIndex++] = (byte)g;
                byteArray[byteArrayIndex++] = (byte)b;
            }
        }

        return byteArray;
    }

    //https://stackoverflow.com/questions/41773621/camera2-output-to-bitmap //https://github.com/EzequielAdrianM/Camera2Vision/blob/master/Camera2/app/src/main/java/com/example/ezequiel/camera2/MainActivity.java
    private byte[] convertYUV420888ToNV21(Image imgYUV420) {
// Converting YUV_420_888 data to YUV_420_SP (NV21).
        byte[] data;
        ByteBuffer buffer0 = imgYUV420.getPlanes()[0].getBuffer();
        ByteBuffer buffer2 = imgYUV420.getPlanes()[2].getBuffer();
        int buffer0_size = buffer0.remaining();
        int buffer2_size = buffer2.remaining();
        data = new byte[buffer0_size + buffer2_size];
        buffer0.get(data, 0, buffer0_size);
        buffer2.get(data, buffer0_size, buffer2_size);
        return data;
    }


    //https://stackoverflow.com/questions/41773621/camera2-output-to-bitmap
    public static Bitmap convertYUV420888ToNV21_bitmap(Image imgYUV420) {
// Converting YUV_420_888 data to YUV_420_SP (NV21).
        byte[] nv21;
        ByteBuffer buffer0 = imgYUV420.getPlanes()[0].getBuffer();
        ByteBuffer buffer2 = imgYUV420.getPlanes()[2].getBuffer();
        int buffer0_size = buffer0.remaining();
        int buffer2_size = buffer2.remaining();
        nv21 = new byte[buffer0_size + buffer2_size];
        buffer0.get(nv21, 0, buffer0_size);
        buffer2.get(nv21, buffer0_size, buffer2_size);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imgYUV420.getWidth(), imgYUV420.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return bitmap;
    }



    public static Bitmap convertYUV420888ToNV21_bitmap(Image imgYUV420, int orientation) {
// Converting YUV_420_888 data to YUV_420_SP (NV21).
        byte[] nv21;
        ByteBuffer buffer0 = imgYUV420.getPlanes()[0].getBuffer();
        ByteBuffer buffer2 = imgYUV420.getPlanes()[2].getBuffer();
        int buffer0_size = buffer0.remaining();
        int buffer2_size = buffer2.remaining();
        nv21 = new byte[buffer0_size + buffer2_size];
        buffer0.get(nv21, 0, buffer0_size);
        buffer2.get(nv21, buffer0_size, buffer2_size);

        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imgYUV420.getWidth(), imgYUV420.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return bitmap;
    }
}
