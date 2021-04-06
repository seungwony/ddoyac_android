package com.nexysquare.ddoyac.tracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.nexysquare.ddoyac.env.BorderedText;
import com.nexysquare.ddoyac.env.ImageUtils;
import com.nexysquare.ddoyac.env.Logger;
import com.nexysquare.ddoyac.tflite.Classifier;
import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;


public class MultiBoxTracker extends GraphicOverlay.Graphic{
    private static final float TEXT_SIZE_DIP = 18;
    private static final float MIN_SIZE = 16.0f;
    private static final int[] COLORS = {
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.WHITE,
            Color.parseColor("#55FF55"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FF8888"),
            Color.parseColor("#AAAAFF"),
            Color.parseColor("#FFFFAA"),
            Color.parseColor("#55AAAA"),
            Color.parseColor("#AA33AA"),
            Color.parseColor("#0D0068")
    };
    final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
    private final Logger logger = new Logger();
    private final Queue<Integer> availableColors = new LinkedList<Integer>();
    private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();
    private final Paint boxPaint = new Paint();
    private final float textSizePx;
    private final BorderedText borderedText;
    private Matrix frameToCanvasMatrix;
    private int frameWidth;
    private int frameHeight;
    private int sensorOrientation;

    public MultiBoxTracker(GraphicOverlay overlay, final Context context) {
        super(overlay);
        for (final int color : COLORS) {
            availableColors.add(color);
        }

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(10.0f);
        boxPaint.setStrokeCap(Paint.Cap.ROUND);
        boxPaint.setStrokeJoin(Paint.Join.ROUND);
        boxPaint.setStrokeMiter(100);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
    }

//    @Override
//    public void draw(Canvas canvas) {
//        final boolean rotated = sensorOrientation % 180 == 90;
//        final float multiplier =
//                Math.min(
//                        canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
//                        canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
//        frameToCanvasMatrix =
//                ImageUtils.getTransformationMatrix(
//                        frameWidth,
//                        frameHeight,
//                        (int) (multiplier * (rotated ? frameHeight : frameWidth)),
//                        (int) (multiplier * (rotated ? frameWidth : frameHeight)),
//                        sensorOrientation,
//                        false);
//        for (final TrackedRecognition recognition : trackedObjects) {
//            final RectF trackedPos = new RectF(recognition.location);
//
//            getFrameToCanvasMatrix().mapRect(trackedPos);
//            boxPaint.setColor(recognition.color);
//
//            float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
//
//            int width = canvas.getWidth();
//            int height = canvas.getHeight() / 2;
//            Bitmap resize_bitmap = Bitmap.createScaledBitmap(recognition.croppedBitmap, width, height, false);
//
////      canvas.drawBitmap(resize_bitmap, 0, 0, null);
//            canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint); // 박스 추가
//
//            final String labelString =
//                    !TextUtils.isEmpty(recognition.title)
//                            ? String.format("%s %.2f", recognition.title, (100 * recognition.detectionConfidence))
//                            : String.format("%.2f", (100 * recognition.detectionConfidence));
//            //            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.top,
//            // labelString);
//            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.top, labelString + "%", boxPaint); // 박스위에 클래스명과 정확도 표시
//        }
//    }

    public synchronized void setFrameConfiguration(
            final int width, final int height, final int sensorOrientation) {
        frameWidth = width;
        frameHeight = height;
        this.sensorOrientation = sensorOrientation;
    }

    public synchronized void drawDebug(final Canvas canvas) {
        final Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60.0f);

        final Paint boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);

        for (final Pair<Float, RectF> detection : screenRects) {
            final RectF rect = detection.second;
            canvas.drawRect(rect, boxPaint);
            canvas.drawText("" + detection.first, rect.left, rect.top, textPaint);
            borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + detection.first);
        }
    }
    public synchronized void trackResults(final List<Classifier.Recognition> results, Bitmap cropCopyBitmap) {
        logger.i("Processing %d results ", results.size());
        processResults(results, cropCopyBitmap);
    }
    public synchronized void trackResults(final List<Classifier.Recognition> results, final long timestamp, Bitmap cropCopyBitmap) {
        logger.i("Processing %d results from %d", results.size(), timestamp);
        processResults(results, cropCopyBitmap);
    }

    private Matrix getFrameToCanvasMatrix() {
        return frameToCanvasMatrix;
    }

    @Override
    public  void draw(final Canvas canvas) {
        final boolean rotated = sensorOrientation % 180 == 90;
        final float multiplier =
                Math.min(
                        canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                        canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
        frameToCanvasMatrix =
                ImageUtils.getTransformationMatrix(
                        frameWidth,
                        frameHeight,
                        (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                        (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                        sensorOrientation,
                        false);
        for (final TrackedRecognition recognition : trackedObjects) {
            final RectF trackedPos = new RectF(recognition.location);

            getFrameToCanvasMatrix().mapRect(trackedPos);
            boxPaint.setColor(recognition.color);

            float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;

            int width = canvas.getWidth();
            int height = canvas.getHeight() / 2;
            Bitmap resize_bitmap = Bitmap.createScaledBitmap(recognition.croppedBitmap, width, height, false);

//      canvas.drawBitmap(resize_bitmap, 0, 0, null);
            canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint); // 박스 추가

            final String labelString =
                    !TextUtils.isEmpty(recognition.title)
                            ? String.format("%s %.2f", recognition.title, (100 * recognition.detectionConfidence))
                            : String.format("%.2f", (100 * recognition.detectionConfidence));
            //            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.top,
            // labelString);
            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.bottom, labelString + "%", boxPaint); // 박스위에 클래스명과 정확도 표시
        }
    }

    private void processResults(final List<Classifier.Recognition> results, Bitmap cropCopyBitmap) {
        final List<Pair<Float, Classifier.Recognition>> rectsToTrack = new LinkedList<Pair<Float, Classifier.Recognition>>();

        screenRects.clear();
        final Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());

        for (final Classifier.Recognition result : results) {  // 디텍된 객체들 하나씩 돌면서 화면에 뿌릴건지 말건지 판단
            if (result.getLocation() == null) {
                continue;
            }
            final RectF detectionFrameRect = new RectF(result.getLocation());

            final RectF detectionScreenRect = new RectF();
            rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);

            logger.v("Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect);

            screenRects.add(new Pair<Float, RectF>(result.getConfidence(), detectionScreenRect));

            if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {  // 크기 작으면 스킵
                logger.w("Degenerate rectangle! " + detectionFrameRect);
                continue;
            }

            rectsToTrack.add(new Pair<Float, Classifier.Recognition>(result.getConfidence(), result)); // 화면에 뿌릴 네모박스 추가
        }

        trackedObjects.clear();
        if (rectsToTrack.isEmpty()) {
            logger.v("Nothing to track, aborting.");
            return;
        }

        for (final Pair<Float, Classifier.Recognition> potential : rectsToTrack) {
            final TrackedRecognition trackedRecognition = new TrackedRecognition();
            trackedRecognition.detectionConfidence = potential.first;
            trackedRecognition.location = new RectF(potential.second.getLocation());
            trackedRecognition.title = potential.second.getTitle();
//      trackedRecognition.color = COLORS[trackedObjects.size() % COLORS.length];
            trackedRecognition.color = COLORS[potential.second.getDetectedClass() % COLORS.length];
            trackedRecognition.croppedBitmap = cropCopyBitmap;
            trackedObjects.add(trackedRecognition);

//      if (trackedObjects.size() >= COLORS.length) {
//        break;
//      }
        }
    }

    private static class TrackedRecognition {
        RectF location;
        float detectionConfidence;
        int color;
        String title;
        Bitmap croppedBitmap;
    }
}