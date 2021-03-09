package com.nexysquare.ddoyac.textdetection;

//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.RectF;
//
//import com.google.firebase.ml.vision.text.FirebaseVisionText;
//import com.google.mlkit.vision.text.Text;
//import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;
//
//public class TextGraphic extends GraphicOverlay.Graphic {
//
//    private static final int TEXT_COLOR = Color.WHITE;
//    private static final float TEXT_SIZE = 54.0f;
//    private static final float STROKE_WIDTH = 4.0f;
//
//    private final Paint rectPaint;
//    private final Paint textPaint;
//    private final Text.Element text;
//
//    public TextGraphic(GraphicOverlay overlay, Text.Element text) {
//        super(overlay);
//
//        this.text = text;
//
//        rectPaint = new Paint();
//        rectPaint.setColor(TEXT_COLOR);
//        rectPaint.setStyle(Paint.Style.STROKE);
//        rectPaint.setStrokeWidth(STROKE_WIDTH);
//
//        textPaint = new Paint();
//        textPaint.setColor(TEXT_COLOR);
//        textPaint.setTextSize(TEXT_SIZE);
//        // Redraw the overlay, as this graphic has been added.
//        postInvalidate();
//    }
//
//    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
//    @Override
//    public void draw(Canvas canvas) {
//        if (text == null) {
//            throw new IllegalStateException("Attempting to draw a null text.");
//        }
//
//        // Draws the bounding box around the TextBlock.
//        RectF rect = new RectF(text.getBoundingBox());
//        rect.left = translateX(rect.left);
//        rect.top = translateY(rect.top);
//        rect.right = translateX(rect.right);
//        rect.bottom = translateY(rect.bottom);
//        canvas.drawRect(rect, rectPaint);
//
//        // Renders the text at the bottom of the box.
//        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);
//    }
//}


import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;

import java.util.Arrays;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "TextGraphic";

    private static final int TEXT_COLOR = Color.WHITE;
    private static final int MARKER_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Paint labelPaint;
    private final Text text;

    public TextGraphic(GraphicOverlay overlay, Text text) {
        super(overlay);

        this.text = text;

        rectPaint = new Paint();
        rectPaint.setColor(MARKER_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        labelPaint = new Paint();
        labelPaint.setColor(MARKER_COLOR);
        labelPaint.setStyle(Paint.Style.FILL);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "Text is: " + text.getText());
        for (TextBlock textBlock : text.getTextBlocks()) {
            // Renders the text at the bottom of the box.
            Log.d(TAG, "TextBlock text is: " + textBlock.getText());
            Log.d(TAG, "TextBlock boundingbox is: " + textBlock.getBoundingBox());
            Log.d(TAG, "TextBlock cornerpoint is: " + Arrays.toString(textBlock.getCornerPoints()));
            for (Line line : textBlock.getLines()) {
                Log.d(TAG, "Line text is: " + line.getText());
                Log.d(TAG, "Line boundingbox is: " + line.getBoundingBox());
                Log.d(TAG, "Line cornerpoint is: " + Arrays.toString(line.getCornerPoints()));
                // Draws the bounding box around the TextBlock.
                RectF rect = new RectF(line.getBoundingBox());
                // If the image is flipped, the left will be translated to right, and the right to left.
                float x0 = translateX(rect.left);
                float x1 = translateX(rect.right);
                rect.left = min(x0, x1);
                rect.right = max(x0, x1);
                rect.top = translateY(rect.top);
                rect.bottom = translateY(rect.bottom);
                canvas.drawRect(rect, rectPaint);


                //        RectF rect = new RectF(text.getBoundingBox());
//        rect.left = translateX(rect.left);
//        rect.top = translateY(rect.top);
//        rect.right = translateX(rect.right);
//        rect.bottom = translateY(rect.bottom);
//        canvas.drawRect(rect, rectPaint);
//
//        // Renders the text at the bottom of the box.
        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);
//                float lineHeight = TEXT_SIZE + 2 * STROKE_WIDTH;
//                float textWidth = textPaint.measureText(line.getText());
//                canvas.drawRect(
//                        rect.left - STROKE_WIDTH,
//                        rect.top - lineHeight,
//                        rect.left + textWidth + 2 * STROKE_WIDTH,
//                        rect.top,
//                        labelPaint);
//                // Renders the text at the bottom of the box.
//                canvas.drawText(line.getText(), rect.left, rect.top - STROKE_WIDTH, textPaint);
//
//                for (Element element : line.getElements()) {
//                    Log.d(TAG, "Element text is: " + element.getText());
//                    Log.d(TAG, "Element boundingbox is: " + element.getBoundingBox());
//                    Log.d(TAG, "Element cornerpoint is: " + Arrays.toString(element.getCornerPoints()));
//                    Log.d(TAG, "Element language is: " + element.getRecognizedLanguage());
//                }
            }
        }
    }
}