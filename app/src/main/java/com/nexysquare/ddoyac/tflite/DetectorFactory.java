package com.nexysquare.ddoyac.tflite;

import android.content.res.AssetManager;

import java.io.IOException;

public class DetectorFactory {
    public static YoloV5Classifier getDetector(
            final AssetManager assetManager,
            final String modelFilename)
            throws IOException {
        String labelFilename = null;
        boolean isQuantized = false;
        int inputSize = 0;


        if (modelFilename.equals("drugscan320s-fp16.tflite")) {
            labelFilename = "file:///android_asset/drugscan.txt";
            isQuantized = false;
            inputSize = 320;

        }
        else if (modelFilename.equals("drugscan224s-fp16.tflite")) {
            labelFilename = "file:///android_asset/drugscan.txt";
            isQuantized = false;
            inputSize = 224;
        }
        else if (modelFilename.equals("drugscan224m-fp16.tflite")) {
            labelFilename = "file:///android_asset/drugscan.txt";
            isQuantized = false;
            inputSize = 224;
        }


        return YoloV5Classifier.create(assetManager, modelFilename, labelFilename, isQuantized,
                inputSize);
    }

}
