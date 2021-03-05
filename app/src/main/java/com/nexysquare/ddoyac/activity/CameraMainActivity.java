package com.nexysquare.ddoyac.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.view.TextureViewMeteringPointFactory;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.nexysquare.ddoyac.env.ImageUtils;
import com.nexysquare.ddoyac.tflite.DetectorFactory;
import com.nexysquare.ddoyac.tflite.YoloV5Classifier;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.ChipHelper;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.Constants;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.textdetection.TextGraphic;
import com.nexysquare.ddoyac.textdetection.others.GraphicOverlay;
import com.nexysquare.ddoyac.tflite.Classifier;
import com.nexysquare.ddoyac.tracking.MultiBoxTracker;
import com.nexysquare.ddoyac.util.LabelHelper;
import com.nexysquare.ddoyac.view.ColorCircleDrawable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;
import me.toptas.fancyshowcase.listener.OnViewInflateListener;

public class CameraMainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private final static String TAG  = "CameraXOCRActivity";
    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private ChipGroup filter_group;
    EditText textView;
    PreviewView mCameraView;
    SurfaceHolder holder;
    SurfaceView surfaceView;
    ProcessCameraProvider mCameraProvider;
    Canvas canvas;
    Paint paint;
    int cameraHeight, cameraWidth, xOffset, yOffset, boxWidth, boxHeight;
    private boolean inferencing = false;
    private enum DetectorMode {
        TF_OD_API;
    }

    private enum SearchMethod{
        OCR,
        IMAGE
    }

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private Handler handler = new Handler(Looper.getMainLooper());

    protected Handler inferenceHandler;
    private HandlerThread handlerThread;
    private ImageView cropImg, extract_color_img;

    String foundKeyword;
    String detectedShape;

    public CameraControl cControl;
    public CameraInfo cInfo;

    private SearchMethod searchMethod = SearchMethod.OCR;

//    private MultiBoxTracker tracker;
//
//    OverlayView trackingOverlay;

    private YoloV5Classifier detector;

//    protected Handler handler;
//    private HandlerThread handlerThread;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private GraphicOverlay graphics_overlay;
    private GraphicOverlay graphics_overlay_ocr;
    private ArrayList<String> extract_colors = new ArrayList<>();
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    private String MODEL_PATH = "drugscan224s-fp16.tflite";

    private TextView shape_info_txt, extract_color_info_txt, search_type_text;

    private Button search_ocr_btn, search_img_btn;

    FancyShowCaseView searchFancyShowcaseView;
    private View search_expanded_box;
    private Bitmap detectBitmap;

    private View extra_menu;
    /**
     * Responsible for converting the rotation degrees from CameraX into the one compatible with Firebase ML
     */

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }
    public static void open(Context context) {
        Intent intent = new Intent(context, CameraMainActivity.class);

        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax_main);


        boolean initUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("initUser", true);
        if(initUser){
            PermissionIntroActivity.open(getApplicationContext());
            finish();
            return;
        }

        ImageButton filter_clear_btn = findViewById(R.id.filter_clear_btn);

        extra_menu = findViewById(R.id.extra_menu);
        View extra_menu_bg = extra_menu.findViewById(R.id.extra_menu_bg);
        View keyword_search_btn = extra_menu.findViewById(R.id.keyword_search_btn);
        View app_info_btn = extra_menu.findViewById(R.id.app_info_btn);
        View feedback_btn = extra_menu.findViewById(R.id.feedback_btn);
        textView = findViewById(R.id.text);
        filter_group = findViewById(R.id.filter_group);
        graphics_overlay = findViewById(R.id.graphics_overlay);
        graphics_overlay_ocr = findViewById(R.id.graphics_overlay_ocr);

        shape_info_txt = findViewById(R.id.shape_info_txt);
        extract_color_info_txt = findViewById(R.id.extract_color_info_txt);

        ImageButton search_btn = findViewById(R.id.search_btn);
        ImageButton capture_btn = findViewById(R.id.capture_btn);

        search_type_text = findViewById(R.id.search_type_text);
        search_expanded_box = findViewById(R.id.search_expanded_box);
        Button guide_btn = findViewById(R.id.guide_btn);
        search_ocr_btn = findViewById(R.id.search_ocr_btn);
        search_img_btn = findViewById(R.id.search_img_btn);

        ImageButton more_btn = findViewById(R.id.more_btn);
//        search_ocr_btn.setTag("ocr");
//        search_img_btn.setTag("img");
        //Start Camera
//        startCamera();

        extra_menu.setVisibility(View.GONE);
        keyword_search_btn.setOnClickListener(this);
        app_info_btn.setOnClickListener(this);
        feedback_btn.setOnClickListener(this);
        extra_menu.setOnClickListener(this);
        //Create the bounding box
        cropImg = findViewById(R.id.cropImg);
        extract_color_img = findViewById(R.id.extract_color_img);
        surfaceView = findViewById(R.id.overlay);
        surfaceView.setZOrderOnTop(true);
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);


//        View search_cardview = findViewById(R.id.search_cardview);
        guide_btn.setOnClickListener(this);
        search_ocr_btn.setOnClickListener(this);
        search_img_btn.setOnClickListener(this);
        capture_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        textView.setOnClickListener(this);
        filter_clear_btn.setOnClickListener(this);
        more_btn.setOnClickListener(this);
//        search_cardview.setOnClickListener(this);


        search_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SearchDrugActivity.open(CameraMainActivity.this, textView);
                return false;
            }
        });

        try {
            detector = DetectorFactory.getDetector(getAssets(), MODEL_PATH);
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

        detector.useNNAPI();

        detector.setNumThreads(2);

//        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
//
//        frameToCropTransform =
//                ImageUtils.getTransformationMatrix(
//                        previewWidth, previewHeight,
//                        cropSize, cropSize,
//                        sensorOrientation, MAINTAIN_ASPECT);
//
//        cropToFrameTransform = new Matrix();
//        frameToCropTransform.invert(cropToFrameTransform);

//        tracker = new MultiBoxTracker(getApplicationContext());
//        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
//        trackingOverlay.postInvalidate();
//        trackingOverlay.addCallback(
//                new OverlayView.DrawCallback() {
//                    @Override
//                    public void drawCallback(final Canvas canvas) {
//
//                        tracker.draw(canvas);
//
////                        if (isDebug()) {
////                            tracker.drawDebug(canvas);
////                        }
//                    }
//                });
        testChip();




        boolean hasShownShowcase = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_shown_showcase", false);
        if(!hasShownShowcase){

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    showShowcaseview();
                }
            }, 300);


        }
    }

//    oncreate


    private void showShowcaseview(){
        FancyShowCaseQueue queue = new FancyShowCaseQueue();


        FancyShowCaseView ocrFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(search_ocr_btn)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .title(getString(R.string.showcase_ocr))
                .roundRectRadius(90)
                .customView(R.layout.view_showcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        View root_view = view.findViewById(R.id.root_view);

                        root_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                queue.getCurrent().hide();
                            }
                        });
                        TextView showcase_text = view.findViewById(R.id.showcase_text);
                        showcase_text.setText(getString(R.string.showcase_ocr));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        Glide.with(getApplicationContext()).load(R.drawable.showcase1).into(showcase_img);
                    }
                })

                .build();


        FancyShowCaseView searchFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(search_expanded_box)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .title(getString(R.string.showcase_keyword))
                .customView(R.layout.view_showcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        View root_view = view.findViewById(R.id.root_view);
                        root_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                queue.getCurrent().hide();
                            }
                        });
                        TextView showcase_text = view.findViewById(R.id.showcase_text);
                        showcase_text.setText(getString(R.string.showcase_keyword));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        Glide.with(getApplicationContext()).load(R.drawable.showcase2).into(showcase_img);
                    }

                })
                .build();

        FancyShowCaseView imageFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(search_img_btn)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .title(getString(R.string.showcase_image))
                .customView(R.layout.view_showcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        View root_view = view.findViewById(R.id.root_view);
                        root_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                queue.getCurrent().hide();
                            }
                        });
                        TextView showcase_text = view.findViewById(R.id.showcase_text);
                        showcase_text.setText(getString(R.string.showcase_image));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        Glide.with(getApplicationContext()).load(R.drawable.showcase3).into(showcase_img);
                    }
                })
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String s) {
//                        Log.d(TAG, "dismiss "+ s );
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("has_shown_showcase", true).apply();
                    }

                    @Override
                    public void onSkipped(String s) {

                    }
                })
                .build();




        queue.add(ocrFancyShowcaseView);
        queue.add(searchFancyShowcaseView);
        queue.add(imageFancyShowcaseView);

        queue.show();
    }


    private void showShowcaseOCRButton(){
        boolean has_shown_showcase_ocr_btn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_shown_showcase_ocr_btn", false);
        if(has_shown_showcase_ocr_btn) return;
        ImageButton capture_btn = findViewById(R.id.capture_btn);
        searchFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(capture_btn)
                .title(getString(R.string.showcase_ocr_button))
                .customView(R.layout.view_showcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
                        View root_view = view.findViewById(R.id.root_view);
//                        root_view.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ((FancyShowCaseView) v).hide();
//
//                            }
//                        });
                        TextView showcase_text = view.findViewById(R.id.showcase_text);
                        showcase_text.setText(getString(R.string.showcase_ocr_button));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        showcase_img.setVisibility(View.GONE);
//                        Glide.with(getApplicationContext()).load(R.drawable.showcase2).into(showcase_img);
                        Button close_forever_btn = view.findViewById(R.id.close_forever_btn);
                        close_forever_btn.setVisibility(View.VISIBLE);
                        close_forever_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("has_shown_showcase_ocr_btn", true).apply();
//                                FancyShowCaseView fshowcase = (FancyShowCaseView)view;
//                                fshowcase.hide();
                                searchFancyShowcaseView.hide();
                            }
                        });
                    }

                })
                .build();

        searchFancyShowcaseView.show();

    }

    private void showShowcaseImageButton(){
        boolean has_shown_showcase_img_btn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_shown_showcase_img_btn", false);
        if(has_shown_showcase_img_btn) return;
        ImageButton capture_btn = findViewById(R.id.capture_btn);
        searchFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(capture_btn)
                .title(getString(R.string.showcase_image_button))
                .customView(R.layout.view_showcase, new OnViewInflateListener() {
                    @Override
                    public void onViewInflated(View view) {
//                        View root_view = view.findViewById(R.id.root_view);
//                        root_view.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//
//                            }
//                        });
                        TextView showcase_text = view.findViewById(R.id.showcase_text);
                        showcase_text.setText(getString(R.string.showcase_image_button));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        showcase_img.setVisibility(View.GONE);
//                        Glide.with(getApplicationContext()).load(R.drawable.showcase2).into(showcase_img);
                        Button close_forever_btn = view.findViewById(R.id.close_forever_btn);
                        close_forever_btn.setVisibility(View.VISIBLE);
                        close_forever_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("has_shown_showcase_img_btn", true).apply();
//                                FancyShowCaseView fshowcase = (FancyShowCaseView)view;
//                                fshowcase.dismi
                                searchFancyShowcaseView.hide();

                            }
                        });
                    }

                })
                .build();

        searchFancyShowcaseView.show();

    }

    @Override
    protected void onResume() {
        super.onResume();






        checkPermissions();


    }



    @Override
    protected void onPause() {




        stopCamera();

        if(handlerThread!=null){
            handlerThread.quitSafely();
            try {
                handlerThread.join();
                handlerThread = null;
                inferenceHandler = null;
            } catch (final InterruptedException e) {
                Log.e(TAG, "Exception! " + e.getMessage());
            }
        }


        super.onPause();
//        mCameraView.

    }



    private void resetChip(){

        ArrayList<Integer> ids = new ArrayList<>();
        int totalChildCount = filter_group.getChildCount();
        for(int i =0 ; i < totalChildCount; i++){
            View currentChild = filter_group.getChildAt(filter_group.getChildCount() - 1);
//            Chip chip = (Chip)filter_group.getChildAt(i);
//            chip.getId()
//            filter_group.removeViewAt(i);
//            ids.add(i);
            filter_group.removeView(currentChild);
        }

//        for(int i : ids){
//            filter_group.removeViewAt(i);
//        }
//        filter_group.no
//        for(int id : filter_group.getCheckedChipIds()){
//            Log.d("Chip", "id : " + id);
//            filter_group.removeViewAt(id);
//        }



    }
    private void testChip(){
        ChipHelper.addActionChipView(this, filter_group, 10000, "모양", filterListener);
        ChipHelper.addActionChipView(this, filter_group, 10001, "색상", filterListener);
        ChipHelper.addActionChipView(this, filter_group, 10002, "마크", filterListener);

    }
    View.OnClickListener filterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            SearchFilterActivity.open(CameraMainActivity.this, search_edittext, search_edittext.getText().toString(), REQUEST_FILTER);
        }
    };

    void checkPermissions() {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        } else {
            handlerThread = new HandlerThread("inference");
            handlerThread.start();
            inferenceHandler = new Handler(handlerThread.getLooper());

            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    /**
     * Starting Camera
     */
    void startCamera() {
        mCameraView = findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    mCameraProvider = cameraProviderFuture.get();

//                    cameraProvider.unbind(im);
//                    ViewPort viewPort = preview.getViewPort();
//                    UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
//                            .setViewPort(viewPort)
//                            .addUseCase(preview)
//                            .addUseCase(imageAnalysis)
//                            .build();
//
//                    cameraProvider.
//                    cameraProvider.unbindAll();
                    CameraMainActivity.this.bindPreview(mCameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));


//        cameraProviderFuture.canc
//        mCameraView.getpro
    }

    /**
     * Binding to camera
     */
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mCameraView.createSurfaceProvider());

//        graphics_overlay.setCameraInfo(mCameraView.getWidth(), mCameraView.getHeight(), 0);
        //Image Analysis Function
        //Set static size according to your device or write a dynamic function for it
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
//                        .setTargetResolution(new Size(720, 1488))
                        .setTargetResolution(new Size(mCameraView.getWidth(), mCameraView.getHeight()))
//                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                        .setTargetAspectRatio(AspectRatio.RA)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();




        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                //changing normal degrees into Firebase rotation
//                Rect rect = new Rect();
//                rect.top
//                image.setCropRect();


                int rotationDegrees = degreesToFirebaseRotation(image.getImageInfo().getRotationDegrees());
                if (image == null || image.getImage() == null) {
                    return;
                }
                //Getting a FirebaseVisionImage object using the Image object and rotationDegrees
                final Image mediaImage = image.getImage();
                FirebaseVisionImage images = FirebaseVisionImage.fromMediaImage(mediaImage, rotationDegrees);
                //Getting bitmap from FirebaseVisionImage Object
                Bitmap bmp = images.getBitmap();


                int layout_w = 320;
                int layout_h = 240;


                layout_w = mCameraView.getWidth();
                layout_h = mCameraView.getHeight();



                //Getting the values for cropping
                DisplayMetrics displaymetrics = new DisplayMetrics();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

                    getDisplay().getRealMetrics(displaymetrics);
                } else {
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                }


                int height = bmp.getHeight();
                int width = bmp.getWidth();



                float newWidth = mCameraView.getWidth();
                float newHeight = newWidth;

                float ratioX = newWidth / (float) mCameraView.getWidth();
                float ratioY = newHeight / (float) mCameraView.getHeight();
                float middleX = newWidth / 2.0f;
                float middleY = newHeight / 2.0f;
//
                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);


                int left, top, right, bottom;

                left = Math.abs((int)middleX - mCameraView.getWidth() / 2);
                top = Math.abs((int)middleY - mCameraView.getHeight() / 2);
                right = Math.abs((int)middleX + mCameraView.getWidth() / 2);
                bottom = Math.abs((int)middleY + mCameraView.getHeight() / 2);

                xOffset = left;
                yOffset = top;
                boxHeight = bottom - top;
                boxWidth = right - left;




                //Creating new cropped bitmap
//                final Bitmap bitmap = Bitmap.createBitmap(bmp, left, top, boxWidth, boxHeight);
                final Bitmap bitmap = Bitmap.createBitmap(bmp, xOffset, yOffset, boxWidth, boxHeight);


//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        cropImg.setImageBitmap(bitmap);
//                    }
//                });

                int cropSize = detector.getInputSize();

//                boolean MAINTAIN_ASPECT = true;
//                Matrix  frameToCropTransform =
//                        ImageUtils.getTransformationMatrix(
//                                boxWidth, boxHeight,
//                                cropSize, cropSize,
//                                0, MAINTAIN_ASPECT);


//                Matrix  cropToFrameTransform = new Matrix();
//                frameToCropTransform.invert(cropToFrameTransform);

//                int targetWidth = imageView.getWidth();
//
//                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
//                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, cropSize, cropSize, false);

                int rotation = image.getImageInfo().getRotationDegrees();
                int orientation = getScreenOrientation();

                Log.d(TAG, "rotation : " + rotation + " orientation : " + orientation);
                frameToCropTransform =
                        ImageUtils.getTransformationMatrix(
                                boxWidth, boxHeight,
                                cropSize, cropSize,
                                0, true);


                cropToFrameTransform = new Matrix();
                frameToCropTransform.invert(cropToFrameTransform);


//                Bitmap croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

//                croppedBitmap.setPi



                if(!inferencing){
                    runInBackground(new Runnable() {
                        @Override
                        public void run() {

                            inferencing = true;

                            Log.i(TAG, "Running detection on image ");
                            final long startTime = SystemClock.uptimeMillis();

                            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                            Log.i(TAG, "results: " + results);
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

                                        bm_last = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);

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

                            tracker.trackResults(mappedRecognitions, croppedBitmap);






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
                                        cropImg.setImageBitmap(finalBm_last);

                                        detectBitmap = finalBm_last;
                                    }



                                    if(finalFocusBitmap !=null){
                                        String colors = getMostCommonColour(finalFocusBitmap);
                                        String colors_info = String.format(getString(R.string.color_form), colors);

                                        extract_color_info_txt.setText(colors_info);

                                    }
                                    if(!finalStr_result.equals("")){
                                        String shape_info = String.format(getString(R.string.shape_form), LabelHelper.intuitionLabel(finalStr_result));
                                        shape_info_txt.setText(shape_info);
                                        detectedShape = finalStr_result;
                                    }

                                    inferencing = false;


                                }
                            });

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
                        }
                    });
                }




                //initializing FirebaseVisionTextRecognizer object
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();
                //Passing FirebaseVisionImage Object created from the cropped bitmap
                Task<FirebaseVisionText> result = detector.processImage(FirebaseVisionImage.fromBitmap(bitmap))
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...

                                graphics_overlay_ocr.clear();



//                                firebaseVisionText.getTextBlocks()


                                //getting decoded text
                                String text = firebaseVisionText.getText();
                                //Setting the decoded text in the texttview

                                //for getting blocks and line elements
                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();

                                   if(block.getBoundingBox()!=null){
                                       int top = block.getBoundingBox().top;
                                       int left = block.getBoundingBox().left;
                                       int right = block.getBoundingBox().right;
                                       int bottom = block.getBoundingBox().bottom;

                                       if(left> 0 && top > 0 && right - left > 0 && bottom - top > 0){
                                           final Bitmap _bm = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
                                           runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
    //                                               cropImg.setImageBitmap(_bm);
                                                   foundKeyword= text;

    //                                               String colors = getMostCommonColour(_bm);
                                                   textView.setText(text);

                                               }
                                           });
                                       }

                                   }

                                    for (FirebaseVisionText.Line line : block.getLines()) {
                                        String lineText = line.getText();
                                        for (FirebaseVisionText.Element element : line.getElements()) {
                                            String elementText = element.getText();

                                            GraphicOverlay.Graphic textGraphic = new TextGraphic(graphics_overlay_ocr, element);
                                            graphics_overlay_ocr.add(textGraphic);



                                        }
                                    }
                                }
                                image.close();
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.e("Error", e.toString());
                                        image.close();
                                    }
                                });
            }


        });
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);


        cControl = camera.getCameraControl();
        cInfo = camera.getCameraInfo();

        //AutoFocus Every X Seconds
        MeteringPointFactory AFfactory = new SurfaceOrientedMeteringPointFactory((float)mCameraView.getWidth(),(float)mCameraView.getHeight());
        float centerWidth = (float)mCameraView.getWidth()/2;
        float centerHeight = (float)mCameraView.getHeight()/2;
        MeteringPoint AFautoFocusPoint = AFfactory.createPoint(centerWidth, centerHeight);
        try {
            FocusMeteringAction action = new FocusMeteringAction.Builder(AFautoFocusPoint,FocusMeteringAction.FLAG_AF).setAutoCancelDuration(1, TimeUnit.SECONDS).build();
            cControl.startFocusAndMetering(action);
        }catch (Exception e){

        }



        //AutoFocus CameraX
        mCameraView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handler.removeCallbacks(focusingTOInvisible);
//                focusView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_focus));
//                focusView.setVisibility(View.VISIBLE);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory((float) mCameraView.getWidth(), (float)mCameraView.getHeight());
                MeteringPoint autoFocusPoint = factory.createPoint(event.getX(), event.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(autoFocusPoint,FocusMeteringAction.FLAG_AF).setAutoCancelDuration(5,TimeUnit.SECONDS).build();
                ListenableFuture future = cControl.startFocusAndMetering(action);

                future.addListener(()->{
                    handler.postDelayed(focusingTOInvisible,3000);
                    try{
                        FocusMeteringResult result = (FocusMeteringResult) future.get();
                        if(result.isFocusSuccessful()){
//                            focusView.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_focus_green));

                        }
                    }catch (Exception e){

                    }
                },executor);



                return true;
            } else {

                return false;
            }
        });



//        float point = SurfaceOrientedMeteringPointFactory.getDefaultPointSize();


//        FocusMeteringAction.Builder()
//        camera.getCameraControl().startFocusAndMetering()
    }



    private Runnable focusingTOInvisible = new Runnable() {
        @Override
        public void run() {
//            focusView.setVisibility(View.INVISIBLE);
        }
    };

    private boolean isPortraitMode() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }


   private void stopCamera(){
        if(mCameraProvider!=null)
            mCameraProvider.unbindAll();
   }


    private void DrawFocusRectV2(int color) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            getDisplay().getRealMetrics(displaymetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }

        if(mCameraView==null) return;
//        int height = mCameraView.getHeight();
//        int width = mCameraView.getWidth();

        //cameraHeight = height;
        //cameraWidth = width;
        int left, right, top, bottom, diameter;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);

        float newWidth = mCameraView.getWidth();
        float newHeight = newWidth;

        float ratioX = newWidth / (float) mCameraView.getWidth();
        float ratioY = newHeight / (float) mCameraView.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;
//
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);



        left = Math.abs((int)middleX - mCameraView.getWidth() / 2);
        top = Math.abs((int)middleY - mCameraView.getHeight() / 2);
        right = Math.abs((int)middleX + mCameraView.getWidth() / 2);
        bottom = Math.abs((int)middleY + mCameraView.getHeight() / 2);

        xOffset = left;
        yOffset = top;
        boxHeight = bottom - top;
        boxWidth = right - left;
//
//        Canvas canvas = new Canvas(scaledBitmap);
//        canvas.setMatrix(scaleMatrix);
//        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
       // canvas.setMatrix(scaleMatrix);

        int margin = 20;

        canvas.drawRect(left + margin, margin, right - margin, newWidth - margin, paint);



        Log.d("surface", "ratioX: " + ratioX + " ratioY : " + ratioY + " middleX : " + middleX + " middleY : " + middleY );
        Log.d("surface", "left: " + left + " top : " + top + " right : " + right + " bottom : " + bottom );



        holder.unlockCanvasAndPost(canvas);


//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(boxWidth, boxHeight);
//        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(mCameraView.getWidth(), mCameraView.getHeight());
//        layoutParams.setMargins(left, top, right, bottom);
//        layoutParams.leftMargin = xOffset;
//        layoutParams.topMargin = yOffset;


        int finalLeft = left;
        int finalTop = top;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(boxWidth, boxHeight);
////        layoutParams.setMargins(left, top, right, bottom);
//                layoutParams.leftMargin = finalLeft;
//                layoutParams.topMargin = finalTop;
//                graphics_overlay.setLayoutParams(layoutParams);
//            }
//        });

//        graphics_overlay.setLayoutParams(layoutParams);
//        graphics_overlay_ocr.setLayoutParams(layoutParams);
//        graphics_overlay.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.confirm_test));

    }



    /**
     * For drawing the rectangular box
     */
    private void DrawFocusRect(int color) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            getDisplay().getRealMetrics(displaymetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }
        int height = mCameraView.getHeight();
        int width = mCameraView.getWidth();

        //cameraHeight = height;
        //cameraWidth = width;

        int left, right, top, bottom, diameter;

        diameter = width;
        if (height < width) {
            diameter = height;
        }

        int offset = (int) (0.05 * diameter);
        diameter -= offset;

        canvas = holder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //border's properties
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);

        left = width / 2 - diameter / 3;
        top = height / 2 - diameter / 3;
        right = width / 2 + diameter / 3;
        bottom = height / 2 + diameter / 3;

        xOffset = left;
        yOffset = top;
        boxHeight = bottom - top;
        boxWidth = right - left;
        //Changing the value of x in diameter/x will change the size of the box ; inversely proportionate to x
        canvas.drawRect(left, top, right, bottom, paint);



        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mCameraView.getWidth(), mCameraView.getHeight());
//        layoutParams.setMargins(left, top, right, bottom);
//        layoutParams.leftMargin = xOffset;
//        layoutParams.topMargin = yOffset;


        int finalLeft = left;
        int finalTop = top;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(boxWidth, boxHeight);
////        layoutParams.setMargins(left, top, right, bottom);
//                layoutParams.leftMargin = finalLeft;
//                layoutParams.topMargin = finalTop;
//                graphics_overlay.setLayoutParams(layoutParams);
//            }
//        });

//        graphics_overlay.setLayoutParams(layoutParams);
//        graphics_overlay_ocr.setLayoutParams(layoutParams);
//        graphics_overlay.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.clear_gray));


        holder.unlockCanvasAndPost(canvas);


//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(boxWidth, boxHeight);


    }

    /**
     * Callback functions for the surface Holder
     */
    protected int getScreenOrientation() {
        int rotation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            Display display = this.getDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();


            display.getRealMetrics(displayMetrics);

            rotation = display.getRotation();

//            float density  = getResources().getDisplayMetrics().density;
//            float dpHeight = displayMetrics.heightPixels / density;
//            float dpWidth  = displayMetrics.widthPixels / density;
//
//            Log.d(TAG, "OmesChecka R: "+"second width:"+dpWidth+"second h:"+dpHeight);

        }else {

            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics ();
            display.getMetrics(outMetrics);

            rotation = display.getRotation();


        }
        switch (rotation) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Drawing rectangle

        DrawFocusRectV2(Color.parseColor("#6AF4F4F4"));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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

    @Override
    public void onClick(View v) {

        if( v.getId() == R.id.text || v.getId() == R.id.search_btn){
            String keyword = "";
            if(foundKeyword!=null) {
                keyword = foundKeyword;
            }
//                    SearchDrugActivity.open(getApplicationContext(), foundKeyword, extract_colors);

            SearchDrugActivity.open(CameraMainActivity.this, textView, detectedShape,  keyword, extract_colors);


        }else if( v.getId() == R.id.capture_btn){

            if (searchMethod == SearchMethod.OCR){

                SearchDrugActivity.open(CameraMainActivity.this, textView, detectedShape, foundKeyword, extract_colors);

            }else if (searchMethod == SearchMethod.IMAGE){

                if(detectBitmap!=null) {

                    SearchDrugActivity.open(CameraMainActivity.this, textView, detectedShape, extract_colors, detectBitmap);

                }else{
                    Toast.makeText(getApplicationContext(), "Detect 된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

        }else if( v.getId() == R.id.guide_btn){


            showShowcaseview();

        }else if( v.getId() == R.id.search_ocr_btn){
            SelectSearchMethod(SearchMethod.OCR);
            showShowcaseOCRButton();
        }else if( v.getId() == R.id.search_img_btn){
            SelectSearchMethod(SearchMethod.IMAGE);
            showShowcaseImageButton();
        }else if(v.getId() == R.id.filter_clear_btn){

            extract_color_info_txt.setText("");
            shape_info_txt.setText(getString(R.string.guide_init_user));

            extract_colors.clear();
            textView.setText("");
            detectedShape = null;
            foundKeyword = null;

        }else if(v.getId() ==R.id.more_btn){
//            SettingsActivity.open(getApplicationContext());

            if(extra_menu.getVisibility() == View.VISIBLE){
                extra_menu.setVisibility(View.GONE);
            }else{
                extra_menu.setVisibility(View.VISIBLE);
            }
        }else if(v.getId() ==R.id.keyword_search_btn){
            SearchDrugActivity.open(getApplicationContext());
        }else if(v.getId() ==R.id.feedback_btn){
            send_email();
        }
        else if(v.getId() ==R.id.app_info_btn){
            AppInfoActivity.open(getApplicationContext());
        }
        else if(v.getId() ==R.id.extra_menu){
            extra_menu.setVisibility(View.GONE);
        }



    }

    protected synchronized void runInBackground(final Runnable r) {
        if (inferenceHandler != null) {
            inferenceHandler.post(r);
        }
    }


    private void SelectSearchMethod(SearchMethod type){

        if(type == SearchMethod.OCR){
            search_type_text.setText("OCR");
            searchMethod = SearchMethod.OCR;

            search_ocr_btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.capture_stroke_inactive));
            search_img_btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.searchbox_text_color));

        }else if(type == SearchMethod.IMAGE){
            search_type_text.setText("IMG");
            searchMethod = SearchMethod.IMAGE;
            search_ocr_btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.searchbox_text_color));
            search_img_btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.capture_stroke_inactive));
        }


//        searchMethod

//        search_type_text.
    }


    private void send_email(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"main.infinyx@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "[또약] 피드백");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "[또약] 피드백"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "이메일 관련 어플이 설치되어 있지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
