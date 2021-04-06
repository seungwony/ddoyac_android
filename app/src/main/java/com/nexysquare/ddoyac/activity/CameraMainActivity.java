package com.nexysquare.ddoyac.activity;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
//import com.google.firebase.ml.vision.text.FirebaseVisionText;
//import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.nexysquare.ddoyac.adapter.SavedAdapter;
import com.nexysquare.ddoyac.env.ImageUtils;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.model.SavedModel;
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
import com.nexysquare.ddoyac.util.DateUtil;
import com.nexysquare.ddoyac.util.ImageConversionUtil;
import com.nexysquare.ddoyac.util.LabelHelper;
import com.nexysquare.ddoyac.util.SavedDatabaseHelper;
import com.nexysquare.ddoyac.util.Utils;
import com.nexysquare.ddoyac.view.ColorCircleDrawable;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CameraMainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private final static String TAG  = "CameraXOCRActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final int PICK_PHOTO_REQUEST_CODE = 101;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private ChipGroup filter_group;
    EditText textView;
    PreviewView mCameraView;
    SurfaceHolder holder;
    SurfaceView surfaceView;
    ProcessCameraProvider mCameraProvider;
    Canvas canvas;
    Paint paint;
    boolean islightOn = false;
    boolean isAiProcessing = true;
    boolean isShapeClassificationProcessing = true;
    boolean isColorExtracting = true;

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


    private TextView shape_info_txt, extract_color_info_txt, search_type_text, warning_msg;

    private Button search_ocr_btn, search_img_btn, saved_list_btn;

    FancyShowCaseView searchFancyShowcaseView;
    private View search_expanded_box;
    private Bitmap detectBitmap;


    private View extra_menu;
    /**
     * Responsible for converting the rotation degrees from CameraX into the one compatible with Firebase ML
     */

    private DialogPlus bottomDialog;
    private ArrayList<SavedModel> savedModels = new ArrayList<>();
    public static void open(Context context) {
        Intent intent = new Intent(context, CameraMainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax_main);

        warning_msg = findViewById(R.id.warning_msg);
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

        saved_list_btn = findViewById(R.id.log_btn);
        ImageButton img_lib_btn = findViewById(R.id.img_lib_btn);
        ImageButton more_btn = findViewById(R.id.more_btn);
        ImageButton light_btn= findViewById(R.id.light_btn);
        ImageButton ai_mode_btn = findViewById(R.id.ai_mode_btn);

        ImageButton shape_filter_btn = findViewById(R.id.shape_filter_btn);
        ImageButton color_pick_btn = findViewById(R.id.color_pick_btn);
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
//        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
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
        ai_mode_btn.setOnClickListener(this);
        color_pick_btn.setOnClickListener(this);
        shape_filter_btn.setOnClickListener(this);
        shape_info_txt.setOnClickListener(this);
        extract_color_info_txt.setOnClickListener(this);
        img_lib_btn.setOnClickListener(this);
        extract_color_img.setOnClickListener(this);
        saved_list_btn.setOnClickListener(this);
//        search_cardview.setOnClickListener(this);

        extract_color_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String color = extract_color_info_txt.getText().toString();
                if(color!=null && !color.equals("")){
                    Toast.makeText(getApplicationContext(), color, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            light_btn.setOnClickListener(this);
        }


        search_btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SearchDrugActivity.open(CameraMainActivity.this, textView);
                return true;
            }
        });

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


        CompatibilityList compactList = new CompatibilityList();
        if(compactList.isDelegateSupportedOnThisDevice()){


//            detector.useBestOption(compactList.getBestOptionsForThisDevice());

            detector.setNumThreads(4);
            Log.d(TAG,"isDelegateSupportedOnThisDevice true");

        }else {
           // detector.useNNAPI();
            detector.setNumThreads(4);
            Log.d(TAG,"isDelegateSupportedOnThisDevice false");
        }
//        detector.useNNAPI();
//        detector.useGpu();

//        detector.setNumThreads(2);

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

//        lastImgFromGallery();


        boolean hasShownShowcase = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_shown_showcase", false);
        if(!hasShownShowcase){

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    showShowcaseview();
                }
            }, 300);


        }

        if(warning_msg.getVisibility() == View.VISIBLE){
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
//                    warning_msg.setVisibility(View.GONE);
                    warning_msg.animate().alpha(1).setDuration(800).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            warning_msg.animate().alpha(0).setDuration(800).setInterpolator(new AccelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    warning_msg.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                        }
                    }).start();
                }
            }, 2000);
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
                .build();

        FancyShowCaseView savedListFancyShowcaseView = new FancyShowCaseView.Builder(this)
                .focusOn(saved_list_btn)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(90)
                .title(getString(R.string.showcase_saved_list))
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
                        showcase_text.setText(getString(R.string.showcase_saved_list));

                        ImageView showcase_img = view.findViewById(R.id.showcase_img);
                        Glide.with(getApplicationContext()).load(R.drawable.showcase4).into(showcase_img);
                    }
                })
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(String s) {
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
        queue.add(savedListFancyShowcaseView);

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
    private Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
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
            public void analyze(@NonNull ImageProxy imageProxy) {
                //changing normal degrees into Firebase rotation
//                Rect rect = new Rect();
//                rect.top
//                image.setCropRect();


//                int rotationDegrees = degreesToFirebaseRotation(imageProxy.getImageInfo().getRotationDegrees());
                if (imageProxy == null || imageProxy.getImage() == null) {
                    return;
                }
                //Getting a FirebaseVisionImage object using the Image object and rotationDegrees
                final Image mediaImage = imageProxy.getImage();

//                if(mediaImage.getFormat()==ImageFormat.YUV_420_888){
//
//                }else if(mediaImage.getFormat()==ImageFormat.NV21){
//
//                }
//                Bitmap bmp = imageProxyToBitmap(imageProxy);
//                FirebaseVisionImage images = FirebaseVisionImage.fromMediaImage(mediaImage, rotationDegrees);

//                InputImage inputImage =
//                            InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                Bitmap bmp = ImageConversionUtil.convertYUV420888ToNV21_bitmap(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                Bitmap bmp = ImageConversionUtil.con(mediaImage);




                if(bmp!=null){
//                    Log.d(TAG, "bmp w : " + bmp.getWidth() + " h : " + bmp.getHeight());

                }else{
                    Log.d(TAG, "bmp is null");
                    imageProxy.close();
                }
//                if (mediaImage != null) {
//                InputImage image =
//                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                    // Pass image to an ML Kit Vision API
//                    // ...
//                }
                //Getting bitmap from FirebaseVisionImage Object
//                Bitmap bmp = images.getBitmap();

                if(!isAiProcessing){
                    imageProxy.close();
                    return;
                }



                //Getting the values for cropping
                DisplayMetrics displaymetrics = new DisplayMetrics();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

                    getDisplay().getRealMetrics(displaymetrics);
                } else {
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                }


//                int height = bmp.getHeight();
//                int width = bmp.getWidth();



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



//                Bitmap bmp = toBitmap(mediaImage);

//                if(boxWidth<=0 && boxHeight<=0) return;

//                Log.d(TAG, "boxWidth : "+ boxWidth + " boxHeight : " + boxHeight + " xOffset : " + xOffset + " yOffset : " + yOffset);

                if(bmp==null){
                    imageProxy.close();
                    return;
                }
                //Creating new cropped bitmap
//                final Bitmap bitmap = Bitmap.createBitmap(bmp, left, top, boxWidth, boxHeight);
                Bitmap bitmap = null;
                try{


                    bitmap = Bitmap.createBitmap(bmp, xOffset, yOffset, boxWidth, boxHeight);
                }catch (Exception e){
                    Log.e(TAG, "created bitmap exception");
                    Log.e(TAG, e.getMessage());
                    Log.e(TAG, "bmp width : " + bmp.getWidth()+" bmp height : " + bmp.getHeight());
                    imageProxy.close();
                    return;
                }



                InputImage inputImage2 =
                        InputImage.fromBitmap(bitmap, imageProxy.getImageInfo().getRotationDegrees());


                if(bitmap==null){
                    imageProxy.close();
                    return;
                }

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

                int rotation = imageProxy.getImageInfo().getRotationDegrees();
                int orientation = getScreenOrientation();

//                Log.d(TAG, "rotation : " + rotation + " orientation : " + orientation);
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
                    Bitmap finalBitmap = bitmap;
                    runInBackground(new Runnable() {
                        @Override
                        public void run() {

                            inferencing = true;

                            Log.i(TAG, "Running detection on image ");
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
                                                imageProxy.close();
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
//                                    result.setTitle("");

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
                if (mediaImage != null) {
                    InputImage inputImage =
                            InputImage.fromBitmap(bitmap, 0);
//                    InputImage image =
//                            InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    // Pass image to an ML Kit Vision API
                    // ...
                    recognizeText(inputImage, imageProxy);
                }


            }


        });
        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);

//        camera.setLensFacing(CameraX.LensFacing.BACK)
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



    private void recognizeText(InputImage image,ImageProxy imageProxy) {

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
                                imageProxy.close();

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
                                        imageProxy.close();
                                    }
                                });



        // [END run_detector]
    }

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



        holder.unlockCanvasAndPost(canvas);


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

//            extract_color_info_txt.setText(getString(R.string.guide_init_user));
            shape_info_txt.setText("");

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
            SearchDrugActivity.open(CameraMainActivity.this, textView);
        }else if(v.getId() ==R.id.feedback_btn){
            send_email();
        }
        else if(v.getId() ==R.id.app_info_btn){
            AppInfoActivity.open(getApplicationContext());
        }
        else if(v.getId() ==R.id.extra_menu){
            extra_menu.setVisibility(View.GONE);
        }else if(v.getId() == R.id.light_btn){



            islightOn =  !islightOn;
            cControl.enableTorch(islightOn);

            ImageButton imgBtn = (ImageButton)v;
            if(islightOn){
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flashlight));
//                imgBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_fill_pri));
            }else{
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flashlight_off));
//                imgBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_fill_gray));
            }

        }else if(v.getId() == R.id.ai_mode_btn){
            isAiProcessing = !isAiProcessing;
            ImageButton imgBtn = (ImageButton)v;
            if(isAiProcessing){
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_ai_chip));

//                extract_color_info_txt.setText(getString(R.string.guide_init_user));

//                imgBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_fill_pri));
            }else{
                extract_color_info_txt.setText("");
                extract_color_img.setImageBitmap(null);
                shape_info_txt.setText(getString(R.string.off_auto_classification));

//                extract_colors.clear();
//                textView.setText("");
//                detectedShape = null;
//                foundKeyword = null;


                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_ai_chip_off));
//                imgBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_fill_gray));
            }
        }else if(v.getId() == R.id.shape_filter_btn || v.getId() == R.id.shape_info_txt){
            isShapeClassificationProcessing = !isShapeClassificationProcessing;
            ImageButton imgBtn = findViewById(R.id.shape_filter_btn);
            if(isShapeClassificationProcessing){
                shape_info_txt.setText("");
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shape_filter));
            }else{
                detectedShape = null;
                shape_info_txt.setText(getString(R.string.off_auto_shape_classification));
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shape_filter_off));
            }
        }else if(v.getId() == R.id.color_pick_btn || v.getId() == R.id.extract_color_info_txt || v.getId() == R.id.extract_color_img){

            isColorExtracting = !isColorExtracting;
            ImageButton imgBtn = findViewById(R.id.color_pick_btn);
//            extract_colors.clear();
            if(isColorExtracting){
                extract_color_img.setImageBitmap(null);
                extract_color_info_txt.setText("");
                extract_color_img.setVisibility(View.VISIBLE);
                extract_color_info_txt.setVisibility(View.GONE);
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_color_dropper_picker));
            }else{
                extract_color_info_txt.setVisibility(View.VISIBLE);
                extract_color_img.setVisibility(View.GONE);
                extract_colors.clear();
                extract_color_info_txt.setText(getString(R.string.off_auto_extract_color));
                imgBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_color_dropper_picker_off));
            }
        }else if(v.getId() == R.id.img_lib_btn){
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);
        }else if(v.getId() == R.id.log_btn){
            showSavedListSheetBottoms();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST_CODE){



            if (resultCode == RESULT_OK) {


                Uri selectedImage = data.getData();

                startCrop(selectedImage);
                try {
                    InputStream imageStream = getContentResolver().openInputStream(
                            selectedImage);


                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
//                    Bitmap fastblur = BitmapUtil.fastblur(selectedImageBitmap, (float)0.8, 2);
//                    Bitmap resized = BitmapUtil.resizeBitmapImage(selectedImageBitmap, 160);


//                    Bitmap focusBitmap = BitmapUtil.fixedSizeWithCenterCrop50x50(bitmap);
//                    preview_img.setImageBitmap(bitmap);
//                    preview_img.invalidate();
//
//                    cropped_preview_img.setImageBitmap(focusBitmap);
//                    cropped_preview_img.invalidate();
//
//                    getMostCommonColour(focusBitmap);


                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }else if(requestCode == UCrop.REQUEST_CROP){
            if (resultCode == RESULT_OK) {
                handleCropResult(data);
            }
        }else if (resultCode == UCrop.RESULT_ERROR){
            handleCropError(data);
        }

    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            StaticImageActivity.open(getApplicationContext(), resultUri);


        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startCrop(@NonNull Uri uri) {


//        CropImage.activity(uri)
//                .start(this);



        // get width and height
        int size = getScreenWidth(this);

        String destinationFileName = "cropPickedImage.png";


        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));


        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);

        uCrop.withAspectRatio(1,1).withOptions(options);
        uCrop.withMaxResultSize(size, size);

//        uCrop = basisConfig(uCrop);
//        uCrop = advancedConfig(uCrop);

        uCrop.start(this);

    }

    public static int getScreenWidth(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    private void lastImgFromGallery(){
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

// Put it in the image view
        if (cursor.moveToFirst()) {
            final ImageView imageView = (ImageView) findViewById(R.id.img_lib_btn);
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {   // TODO: is there a better way to do this?
                Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                imageView.setImageBitmap(bm);
            }
        }
    }




    private void showSavedListSheetBottoms(){
//        MaterialDialog materialDialog = new MaterialDialog(this, new BottomSheet(LayoutMode.WRAP_CONTENT));
//        materialDialog.show(()->{
//
//
//        });

        savedModels.clear();
        SavedDatabaseHelper dbHelper = new SavedDatabaseHelper(getApplicationContext());

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllData();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(SavedDatabaseHelper.COL_1));
                    int priority = cursor.getInt(cursor.getColumnIndex(SavedDatabaseHelper.COL_2));
                    String name = cursor.getString(cursor.getColumnIndex(SavedDatabaseHelper.COL_3));
                    String created = cursor.getString(cursor.getColumnIndex(SavedDatabaseHelper.COL_4));


                    SavedModel item = new SavedModel();
                    item.setId(id);
                    item.setPriority(priority);
                    item.setName(name);

                    savedModels.add(item);
//                item.setCreated(new Date());
                    Log.d(TAG, "id: " + id + ", name: " + name + ", priority: " + priority);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        SavedAdapter adapter = new SavedAdapter(this, savedModels);


        adapter.setOnClickListener(new SavedAdapter.onClickListener() {

            @Override
            public void onClick(TextView tv, int id, String name) {
//                SavedDatabaseHelper dbHelper = new SavedDatabaseHelper(getApplicationContext());
                dbHelper.getAllDrugDataById(id);

                //   contentValues.put("id",id);
                //        contentValues.put("priority",priority);
                //        contentValues.put("rel_id",rel_id);
                //        contentValues.put("created",created);
                ArrayList<Integer> ids = new ArrayList<>();
                Cursor cursor = null;
                try {
                    cursor = dbHelper.getAllDrugDataById(id);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
//                            int id = cursor.getInt(cursor.getColumnIndex("id");
//                            int priority = cursor.getInt(cursor.getColumnIndex("priority)");
                            int rel_id = cursor.getInt(cursor.getColumnIndex("rel_id"));

                            ids.add(rel_id);


                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

//                if(ids.size()>0){
//
//
//                }else{
//                    Toast.makeText(getApplicationContext(), "저장된 정보가 없습니다. 개별 알약 페이지에서 하트를 누르고 저장해보세요.", Toast.LENGTH_SHORT).show();
//                }
                SavedDrugActivity.open(CameraMainActivity.this, tv, id, ids, name);

                if(bottomDialog!=null){
                    bottomDialog.dismiss();

                }




            }
        });

//        adapter.setOnLongClickListener(new SavedAdapter.onLongClickListener() {
//            @Override
//            public void onLongClick(int id) {
//                SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());
//                drugDBHelper.deleteDrugData(id);
//                Toast.makeText(getApplicationContext(), "저장 목록을 삭제했습니다.", Toast.LENGTH_SHORT).show();
//
//                if(bottomDialog!=null){
//                    bottomDialog.dismiss();
//
//                }
//            }
//        });
//        materialDialog.show();

        bottomDialog = DialogPlus.newDialog(this)

                .setAdapter(adapter)

                .setHeader(R.layout.dialog_saved_header)

                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
                        bottomDialog = null;
                    }
                })
                .setContentBackgroundResource(R.drawable.upper_corner_background)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();


        TextView title_txt = bottomDialog.getHeaderView().findViewById(R.id.title_txt);

        Button add_btn = bottomDialog.getHeaderView().findViewById(R.id.add_item_btn);

        title_txt.setText("목록에 저장하기");

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                int i = savedModels.size();
//                SavedModel item = new SavedModel();
//                item.setId(i);
//                item.setPriority(i);
//                item.setName("알약 #"+String.valueOf(i));
//                item.setCreated(new Date());
////                savedModels.add(item);
//                dbHelper.insertData(i, "알약 #"+String.valueOf(i), "");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCreateSavedList();
                    }
                }, 500);


                bottomDialog.dismiss();
            }
        });

        bottomDialog.show();

    }
    private void showCreateSavedList(){
        DialogPlus dialog = DialogPlus.newDialog(this)

                .setHeader(R.layout.dialog_header_create_saved_list)
                .setFooter(R.layout.dialog_footer_create_saved_list)
                .setContentBackgroundResource(R.drawable.corner_background)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHolder(new ViewHolder(R.layout.dialog_create_saved_list))
                .setGravity(Gravity.CENTER)

                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
//                        Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();

//                        Utils.hideInputMethod(dialog.getHolderView().findViewById(R.id.name_tf));

//                        Utils.hideKeyboard(getParent());
                        hideKeyboard();

                    }
                })
                .setCancelable(true)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();


        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(10),
                new InputFilter.AllCaps()
        };
        TextInputEditText name_tf = dialog.getHolderView().findViewById(R.id.name_tf);
        name_tf.setFilters(filters);
//        name_tf.findFocus();
        Button add_item_btn = dialog.getFooterView().findViewById(R.id.add_item_btn);


        add_item_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name_tf.getText().length()>1){

                    String name = name_tf.getText().toString();


                    SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());

                    int priority = drugDBHelper.getAllData().getCount();
                    drugDBHelper.insertData(priority, name, DateUtil.convertedSimpleFormat(new Date()));
                    Toast.makeText(getApplicationContext(), "["+ name + "] 저장 목록이 만들어졌습니다.", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showSavedListSheetBottoms();
                        }
                    }, 800);

                }


            }
        });

        dialog.show();

    }

    private void hideKeyboard(){
        Utils.hideKeyboard(this);
    }
}
