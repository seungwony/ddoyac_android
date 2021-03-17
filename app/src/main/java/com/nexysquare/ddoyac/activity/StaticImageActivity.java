package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.nexysquare.ddoyac.Constants;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.core.ExtractDescriptorTask;
import com.nexysquare.ddoyac.env.ImageUtils;
import com.nexysquare.ddoyac.textdetection.TextGraphic;
import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;
import com.nexysquare.ddoyac.tflite.Classifier;
import com.nexysquare.ddoyac.tflite.DetectorFactory;
import com.nexysquare.ddoyac.tflite.YoloV5Classifier;
import com.nexysquare.ddoyac.tracking.MultiBoxTracker;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.util.LabelHelper;
import com.nexysquare.ddoyac.view.ColorCircleDrawable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class StaticImageActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private YoloV5Classifier detector;
    public static void open(Context context, Uri imgUri) {
        Intent intent = new Intent(context, StaticImageActivity.class);
        intent.setData(imgUri);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    private ImageView static_img_preview;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private ArrayList<String> extract_colors = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    protected Handler inferenceHandler;
    private HandlerThread handlerThread;

    boolean isShapeClassificationProcessing = true;
    boolean isColorExtracting = true;

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.back_btn){
            onBackPressed();
        }else if(v.getId() == R.id.ocr_search_btn || v.getId() == R.id.text || v.getId() == R.id.search_btn ){
            SearchDrugActivity.open(StaticImageActivity.this, textView, detectedShape, foundKeyword, extract_colors);
        }else  if(v.getId() == R.id.img_search_btn){
            SearchDrugActivity.open(StaticImageActivity.this, textView, detectedShape, extract_colors, detectBitmap);
        }
    }

    private enum DetectorMode {
        TF_OD_API;
    }


    private TextView shape_info_txt, extract_color_info_txt, ocr_result_txt;
    private EditText textView;

    String foundKeyword;
    String detectedShape;

    private ImageView cropImg, extract_color_img;
    private Bitmap detectBitmap;


    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    private GraphicOverlay graphics_overlay, graphics_overlay_ocr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_image);
        graphics_overlay = findViewById(R.id.graphics_overlay);
        graphics_overlay_ocr = findViewById(R.id.graphics_overlay_ocr);
        static_img_preview = findViewById(R.id.static_img_preview);

        cropImg = findViewById(R.id.cropImg);
        ocr_result_txt = findViewById(R.id.ocr_result_txt);
        extract_color_img = findViewById(R.id.extract_color_img);
        shape_info_txt = findViewById(R.id.shape_info_txt);
        extract_color_info_txt = findViewById(R.id.extract_color_info_txt);
        textView = findViewById(R.id.text);

        Button ocr_search_btn = findViewById(R.id.ocr_search_btn);
        Button img_search_btn = findViewById(R.id.img_search_btn);
        ImageButton back_btn = findViewById(R.id.back_btn);
        ImageButton search_btn = findViewById(R.id.search_btn);
//        Bitmap pickImg = getIntent().getParcelableExtra("bitmap");


        ocr_search_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        textView.setOnClickListener(this);
        img_search_btn.setOnClickListener(this);

        try {
            detector = DetectorFactory.getDetector(getAssets(), Constants.MODEL_PATH);
        } catch (final IOException e) {
            e.printStackTrace();
            Log.e(e.getMessage(), "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        int cropSize = detector.getInputSize();

        Log.i(TAG, "cropsSize: "+cropSize); // 640, 320 ...


        Uri imgUri = getIntent().getData();

        if(imgUri!=null){
            setPreviewImage(imgUri);
        }

    }


    private void setPreviewImage(Uri selectedImage){
        try {
            InputStream imageStream = getContentResolver().openInputStream(
                    selectedImage);


            Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);


            static_img_preview.setImageBitmap(selectedImageBitmap);
//            sel_img.invalidate();


            process(selectedImageBitmap);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void process(Bitmap bitmap){

        int cropSize = detector.getInputSize();

//                boolean MAINTAIN_ASPECT = true;
//                Matrix  frameToCropTransform =
//                        ImageUtils.getTransformationMatrix(
//                                boxWidth, boxHeight,
//                                cropSize, cropSize,
//                                0, MAINTAIN_ASPECT);

//        int boxWidth = static_img_preview.getLayoutParams().width;
//        int boxHeight = static_img_preview.getLayoutParams().height;

        int boxWidth = bitmap.getWidth();
        int boxHeight = bitmap.getHeight();

//                Matrix  cropToFrameTransform = new Matrix();
//                frameToCropTransform.invert(cropToFrameTransform);

//                int targetWidth = imageView.getWidth();
//
//                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
//                int targetHeight = (int) (targetWidth * aspectRatio);
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, cropSize, cropSize, false);

//        int rotation = imageProxy.getImageInfo().getRotationDegrees();
//        int orientation = getScreenOrientation();

//                Log.d(TAG, "rotation : " + rotation + " orientation : " + orientation);
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        boxWidth, boxHeight,
                        cropSize, cropSize,
                        0, true);


        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);



            Bitmap finalBitmap = bitmap;
//            runInBackground(new Runnable() {
//                @Override
//                public void run() {


                    Log.i(TAG, "Running detection on static image ");
                    final long startTime = SystemClock.uptimeMillis();


                    final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);


                    Collections.sort(results, new Comparator<Classifier.Recognition>() {
                        @Override
                        public int compare(Classifier.Recognition o1, Classifier.Recognition o2) {
                            if(o1.getConfidence() > o2.getConfidence()){
                                return 1;
                            }else if(o1.getConfidence()< o2.getConfidence()){
                                return -1;
                            }

                            return 0;
                        }
                    });
                    //   Log.i(TAG, "results: " + results);
                    long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

//                        Log.e("CHECK", "run: " + results.size());

                    float minimumConfidence = Constants.MINIMUM_CONFIDENCE_TF_OD_API;
                    switch (MODE) {
                        case TF_OD_API:
                            minimumConfidence = Constants.MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                    }



                    final List<Classifier.Recognition> mappedRecognitions =
                            new LinkedList<Classifier.Recognition>();
                    String str_result = "";

                    Bitmap bm_last = null;
                    for (final Classifier.Recognition result : results) {
                        final RectF location = result.getLocation();
                        if (location != null && result.getConfidence() >= minimumConfidence) {



//                                location.
//                                canvas.drawRect(location, paint);

//                                cropToFrameTransform.mapRect(location);

                            cropToFrameTransform.mapRect(location);
                            result.setLocation(location);


                            boolean isPreview = true;
                            if(isPreview) {
                                int top = (int)location.top;
                                int left = (int)location.left;
                                int right = (int)location.right;
                                int bottom = (int)location.bottom;

                                int width = right - left;
                                int height = bottom - top;

                                if(width>0&& height>0 && finalBitmap!=null && finalBitmap.getHeight()>0){
                                    try{
                                        bm_last = Bitmap.createBitmap(finalBitmap, left, top, width, height);
                                    }catch (Exception ex){
                                        Log.e(TAG, ex.getMessage());
                                    }

                                }



//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            cropImg.setImageBitmap(_bm);
//                                        }
//                                    });


                            }

                            str_result = result.getTitle();

                            Log.d(TAG, "Label : "+str_result);

//                                ■, ●, ■■■, ETC

                            result.setTitle(LabelHelper.intuitionLabel(str_result));

                            mappedRecognitions.add(result);


                        }
                    }

                    MultiBoxTracker tracker = new MultiBoxTracker(graphics_overlay, getApplicationContext());

                    if(isShapeClassificationProcessing){
                        tracker.trackResults(mappedRecognitions, croppedBitmap);
                    }







//                        tracker.trackResults(mappedRecognitions, croppedBitmap);
//                        trackingOverlay.postInvalidate();

//                        tracker.trackResults(mappedRecognitions, currTimestamp, croppedBitmap);
                    Bitmap focusBitmap =null;
                    if(bm_last!=null){
                        focusBitmap = BitmapUtil.fixedSizeWithCenterCropHalf(bm_last);
                    }

                    Bitmap finalBm_last = bm_last;
                    String finalStr_result = str_result;

                    Bitmap finalFocusBitmap = focusBitmap;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {



                            graphics_overlay.clear();
                            graphics_overlay.add(tracker);
                            if(finalBm_last !=null){
//                                        if(isShapeClassificationProcessing) {
                                cropImg.setImageBitmap(finalBm_last);

                                detectBitmap = finalBm_last;
//                                        }
                            }







                            if(finalFocusBitmap !=null){

                                if(isColorExtracting){
                                    String colors = getMostCommonColour(finalFocusBitmap);
                                    String colors_info = String.format(getString(R.string.color_form), colors);

                                    extract_color_info_txt.setText(colors);
                                }


                            }
                            if(!finalStr_result.equals("")){
                                if(isShapeClassificationProcessing){
                                    String shape_info = LabelHelper.intuitionLabel(finalStr_result);
                                    shape_info_txt.setText(shape_info);
                                    detectedShape = finalStr_result;
                                }


                            }



                        }
                    });

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
//                }
//            });

        InputImage inputImage =
                InputImage.fromBitmap(bitmap, 0);
//                    InputImage image =
//                            InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        // Pass image to an ML Kit Vision API
        // ...
        recognizeText(inputImage);
    }

    public String getMostCommonColour(Bitmap bitmap) {


        try {
            Map map = ColorUtils.getInstance().imageColour(bitmap);
            List list = new LinkedList(map.entrySet());
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {

                    return ((Comparable) ((Map.Entry) (o1)).getValue())
                            .compareTo(((Map.Entry) (o2)).getValue());

                }

            });
            String colourTemp;

//        if(list.size()==0){
//            return "White";
//        }
            Map.Entry me = (Map.Entry) list.get(list.size() - 1);
            int[] rgb = ColorUtils.getRGBArr((Integer) me.getKey());

//            redTextView.setText("Red: "+ ColorUtils.To00Hex(rgb[0]));
//            greenTextView.setText("Green: "+ ColorUtils.To00Hex(rgb[1]));
//            blueTextView.setText("Blue: "+ ColorUtils.To00Hex(rgb[2]));
//            dominant_color_txt.setText("Dominant Color: #"+ ColorUtils.To00Hex(rgb[0]) + ColorUtils.To00Hex(rgb[1])  + ColorUtils.To00Hex(rgb[2]));

            //Find name of color
            String colorName = ColorUtils.getInstance().getNearestColorNameFromRgb(rgb[0], rgb[1], rgb [2]);

            ArrayList<String> Colors_kor = ColorUtils.getInstance().getListOfColorInKoreanFromEngList(colorName);

            int closestColorInt = ColorUtils.getInstance().getNearestColorIntFromRgb(rgb[0], rgb[1], rgb [2]);


            extract_color_img.setBackground(new ColorCircleDrawable(closestColorInt));
//            extract_color_img.setBackgroundColor(closestColorInt);
//            closest_color_view.setBackgroundColor(closestColorInt);

//            closest_color_txt.setText("Closest Named Color: " + ColorUtils.getInstance().getNearestColorHexFromRgb(rgb[0], rgb[1], rgb [2])) ;
//            result_color_txt.setText(colorName + "\n"+ Colors_kor.toString());

            String colors= ColorUtils.getInstance().convertedColorListToIdx(Colors_kor).toString();

//            color_info_txt.setText("Colors : "+ Colors_kor.toString() + "\n"+ ColorUtils.getInstance().convertedColorListToIdx(Colors_kor).toString());
//
//
            extract_colors.clear();
            extract_colors.addAll(Colors_kor);

            //Converting RGB color value to hex string
//            colourTemp= "#" + ColorUtils.To00Hex(rgb[0]) + ColorUtils.To00Hex(rgb[1]) +  ColorUtils.To00Hex(rgb[2]);
//            int colorInt = Color.parseColor(colourTemp);
//            dominant_color_view.setBackgroundColor(colorInt);

//            return To00Hex(rgb[0]) + To00Hex(rgb[1])  + To00Hex(rgb[2]);

            String[] color = colorName.split(",");
            if(color.length>0){
                return color[0];
            }
//            return Colors_kor.toString();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }


    private void recognizeText(InputImage image) {

        // [START get_detector_default]
        TextRecognizer recognizer = TextRecognition.getClient();
        // [END get_detector_default]


        // [START run_detector]
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {


                                graphics_overlay_ocr.clear();

                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_text]
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();


                                    if (boundingBox != null) {
                                        int top = boundingBox.top;
                                        int left = boundingBox.left;
                                        int right = boundingBox.right;
                                        int bottom = boundingBox.bottom;

                                        int width = right - left;
                                        int height = bottom - top;
                                        if (left > 0 && top > 0 && width > 0 && height > 0) {
//                                            final Bitmap _bm = Bitmap.createBitmap(finalBitmap, left, top, width, height);


                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
//                                                                                                   cropImg.setImageBitmap(_bm);
                                                    foundKeyword = text;

                                                    //                                               String colors = getMostCommonColour(_bm);
                                                    textView.setText(text);
                                                    ocr_result_txt.setText(text);

                                                }
                                            });
                                        }
                                    }

                                    for (Text.Line line : block.getLines()) {
                                        // ...
                                        for (Text.Element element : line.getElements()) {
                                            // ...


                                            // element.getBoundingBox().offsetTo(0, -10);

                                        }
                                    }
                                }

                                GraphicOverlay.Graphic textGraphic = new TextGraphic(graphics_overlay_ocr, visionText);
                                graphics_overlay_ocr.add(textGraphic);

                                // [END get_text]
                                // [END_EXCLUDE]

                            }

//                                firebaseVisionText.getTextBlocks()
                            //getting decoded text
//                                String text = firebaseVisionText.getText();
//                                //Setting the decoded text in the texttview
//
//                                //for getting blocks and line elements
//                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
//                                    String blockText = block.getText();
//
//                                    if(block.getBoundingBox()!=null){
//                                        int top = block.getBoundingBox().top;
//                                        int left = block.getBoundingBox().left;
//                                        int right = block.getBoundingBox().right;
//                                        int bottom = block.getBoundingBox().bottom;
//
//                                        if(left> 0 && top > 0 && right - left > 0 && bottom - top > 0){
//                                            final Bitmap _bm = Bitmap.createBitmap(finalBitmap1, left, top, right - left, bottom - top);
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    //                                               cropImg.setImageBitmap(_bm);
//                                                    foundKeyword= text;
//
//                                                    //                                               String colors = getMostCommonColour(_bm);
//                                                    textView.setText(text);
//
//                                                }
//                                            });
//                                        }
//                                    }
//
//                                    for (FirebaseVisionText.Line line : block.getLines()) {
//                                        String lineText = line.getText();
//                                        for (FirebaseVisionText.Element element : line.getElements()) {
//                                            String elementText = element.getText();
//
//                                            GraphicOverlay.Graphic textGraphic = new TextGraphic(graphics_overlay_ocr, element);
//                                            graphics_overlay_ocr.add(textGraphic);
//
//                                        }
//                                    }
//                                }
//                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });



        // [END run_detector]
    }
    @Override
    protected void onPause() {


        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}

