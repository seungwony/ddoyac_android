package com.nexysquare.ddoyac.textdetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;

public class TextGraphic extends GraphicOverlay.Graphic {

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionText.Element text;

    public TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
        super(overlay);

        this.text = text;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        if (text == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        // Renders the text at the bottom of the box.
        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);
    }
}