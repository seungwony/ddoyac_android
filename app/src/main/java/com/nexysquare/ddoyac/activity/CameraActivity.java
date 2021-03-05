package com.nexysquare.ddoyac.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OutputFileOptions;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import com.nexysquare.ddoyac.R;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.nexysquare.ddoyac.Constants.FLASH_AUTO;
import static com.nexysquare.ddoyac.Constants.FLASH_OFF;
import static com.nexysquare.ddoyac.Constants.FLASH_ON;
import static com.nexysquare.ddoyac.Constants.LENS_BACK;
import static com.nexysquare.ddoyac.Constants.LENS_FRONT;

public class CameraActivity extends AppCompatActivity {
    private final String TAG = "CameraActivity";
    public static void open(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    public static final int REQ_CODE = 1012;
    public static final int RESULT_ACTIVITY_CODE = 1013;

    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    Executor executor = Executors.newSingleThreadExecutor();
    PreviewView mPreviewView;
    String qr1, qr2;

    ImageCapture imageCapture;
    CameraControl cameraControl;
    CameraInfo cameraInfo;
    TextView zoomTv;
    int flashStatus = FLASH_AUTO;
    int lensFacing = LENS_BACK;
    ImageView capture, preview;

    CameraSelector cameraSelector;
    ImageAnalysis imageAnalysis;

    ImageCaptureConfig imageCaptureConfig;
    View frameLayout;

    Bitmap roiBitmap, croppedImage;

    private SeekBar zoomSeekBar;
    @Override
    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        checkPermissions();

//        module = Module.load(ResultActivity.getPath("jit_model_320_EP_500.pt", MainActivity.this));
        zoomSeekBar = findViewById(R.id.zoomSeekBar);
        mPreviewView = findViewById(R.id.viewFinder);
        zoomTv = findViewById(R.id.tv_zoom);
        capture = findViewById(R.id.btn_capture);
        preview = findViewById(R.id.preview);
//        box = findViewById(R.id.box_main);

//        machineId = getIntent().getIntExtra(getString(R.string.machine_id), R.id.rb_machine_2);
//        machineImage = findViewById(R.id.box_machine);
//        resetMachineImage();

        MyGestureListener gestureListener = new MyGestureListener();
        ScaleGestureDetector detector = new ScaleGestureDetector(this, gestureListener);

        frameLayout = findViewById(R.id.frame_layout);
        frameLayout.setOnTouchListener((view, motionEvent) -> detector.onTouchEvent(motionEvent));

        ImageView flashBtn = findViewById(R.id.btn_flash);
        ImageView cameraFlip = findViewById(R.id.switchCamera);

        flashBtn.setOnClickListener(view -> {

            if(flashStatus == FLASH_AUTO){
                flashStatus = FLASH_ON;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                flashBtn.setImageResource(R.drawable.ic_baseline_flash_on_24);
            }
            else if(flashStatus == FLASH_ON){
                flashStatus = FLASH_OFF;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                flashBtn.setImageResource(R.drawable.ic_baseline_flash_off_24);
            }
            else {
                flashStatus = FLASH_AUTO;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                flashBtn.setImageResource(R.drawable.ic_baseline_flash_auto_24);
            }
        });

        cameraFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lensFacing == LENS_BACK){
                    lensFacing = LENS_FRONT;
                    startCamera(LENS_FRONT);
                }
                else {
                    lensFacing = LENS_BACK;
                    startCamera(LENS_BACK);
                }
            }
        });

        capture.setOnClickListener(view -> {
            File f1 =  new File(Environment.getExternalStorageDirectory() + "/AppCam" );
            f1.mkdirs();
            if(f1.exists()) {
                f1.mkdir();
            }

            long ts = Calendar.getInstance().getTimeInMillis();
            final File file = new File(f1, ts+".png");

            OutputFileOptions outputFileOptions = new OutputFileOptions.Builder(file)
                    .build();


            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                    Toast.makeText(CameraActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CameraActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            Uri uri = outputFileResults.getSavedUri();

//                            ExtractDescriptorActivity.open(getApplicationContext(), uri);
                            Uri img = Uri.fromFile(file);
                            if(img!=null){


                                startCrop(img);
//                                Log.d(TAG, " uri is " + img.getPath());
//                                preview.setImageURI(img);
                            }else{
//                                Log.e(TAG, "uri is null");
                            }

                        }
                    });


//                    ExtractDescriptorActivity.open(getApplicationContext(), outputFileResults.getSavedUri());
//                    startCrop(outputFileResults.getSavedUri());

//                    UCrop.of(sourceUri, destinationUri)
//                            .withAspectRatio(16, 9)
//                            .withMaxResultSize(maxWidth, maxHeight)
//                            .start(getApplicationContext());

                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Toast.makeText(CameraActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        });

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN : return true;

                    case MotionEvent.ACTION_UP:
                        MeteringPointFactory factory = mPreviewView.createMeteringPointFactory(cameraSelector);
                        MeteringPoint point = factory.createPoint(event.getX(), event.getY(), 200);

                        FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
//                               .addPoint(point2, FocusMeteringAction.FLAG_AE) // could have many
                                // auto calling cancelFocusAndMetering in 5 seconds
                                .setAutoCancelDuration(5, TimeUnit.SECONDS)
                                .build();
                        // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                        // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                        cameraControl.startFocusAndMetering(action);

                        return true;

                    default: return false;
                }
            }
        });


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
//        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width * (4.7 / 3f))));
//        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));

//        org.opencv.core.Rect rect = ResultActivity.getRect(machineId, width);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(rect.width, rect.height);
//        params.gravity = Gravity.CENTER_HORIZONTAL;
//        params.setMargins(0, rect.y, 0, 0);
//        box.setLayoutParams(params);



        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                changeZoomLevel(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

//    private class YourAnalyzer implements ImageAnalysis.Analyzer {
//
//        @Override
//        public void analyze(ImageProxy imageProxy) {
//            Image mediaImage = imageProxy.getImage();
//            if (mediaImage != null) {
//                InputImage image =
//                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                // Pass image to an ML Kit Vision API
//                // ...
//            }
//        }
//    }


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }





    private void startCrop(@NonNull Uri uri) {


//        CropImage.activity(uri)
//                .start(this);

        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".png";


        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));


        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);

//        uCrop.withAspectRatio(1,1).withOptions(options);

//        uCrop = basisConfig(uCrop);
//        uCrop = advancedConfig(uCrop);

        uCrop.start(this);

    }
    private void changeZoomLevel(float zoom){
        //camera?.cameraControl?.setLinearZoom(level)


        Log.d(TAG, "zoom : " + zoom);
        cameraControl.setLinearZoom(zoom);
    }

    @androidx.camera.core.ExperimentalGetImage
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider, int lensFacing) {

        cameraProvider.unbindAll();

        /* start preview */
        int aspRatioW = frameLayout.getWidth(); //get width of screen
        int aspRatioH = frameLayout.getHeight(); //get height
        Rational asp = new Rational (aspRatioW, aspRatioH); //aspect ratio
        Size screen = new Size(aspRatioW, aspRatioH); //size of the screen

        //config obj for preview/viewfinder thingy.
//        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(asp).setTargetResolution(screen).build();
        Preview preview = new Preview.Builder()
//                .setTargetAspectRatio((aspRatioW/aspRatioH))
                .setTargetResolution(screen)
                .build();


        if(lensFacing == LENS_FRONT){
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        }
        else {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        }

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        imageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

//        imageAnalysis.setAnalyzer();

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        cameraControl = camera.getCameraControl();
        cameraInfo = camera.getCameraInfo();


        // tap tp focus
        MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(frameLayout.getWidth(), frameLayout.getHeight());
        MeteringPoint point = factory.createPoint(100, 100, 100);
        FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
//                .addPoint(point2, FocusMeteringAction.FLAG_AE) // could have many
                // auto calling cancelFocusAndMetering in 5 seconds
                .setAutoCancelDuration(5, TimeUnit.SECONDS)
                .build();

        ListenableFuture future = cameraControl.startFocusAndMetering(action);
        future.addListener( () -> {
            try {
                FocusMeteringResult result = (FocusMeteringResult) future.get();
                // process the result
            } catch (Exception e) {
            }
        } , executor);

    }

    @androidx.camera.core.ExperimentalGetImage
    void checkPermissions() {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
        } else {
            startCamera(LENS_BACK);
        }
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                startCamera(LENS_BACK);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void startCamera(int lensFacing) {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, lensFacing);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // In the SimpleOnGestureListener subclass you should override
    // onDown and any other gesture that you want to detect.
    class MyGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float linearZoom = cameraInfo.getZoomState().getValue().getLinearZoom();
            float scale = linearZoom + (detector.getScaleFactor() - 1) / 10;
            cameraControl.setLinearZoom(scale);
            zoomTv.setText(String.format(Locale.getDefault(), "%d%%", (int) (linearZoom * 100)));
            return super.onScale(detector);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }

        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }


    }
    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            ExtractDescriptorActivity.open(getApplicationContext(), resultUri);
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




//    private void recognizeText(InputImage image) {
//
//        // [START get_detector_default]
//        TextRecognizer recognizer = TextRecognition.getClient();
//        // [END get_detector_default]
//
//        // [START run_detector]
//        Task<Text> result =
//                recognizer.process(image)
//                        .addOnSuccessListener(new OnSuccessListener<Text>() {
//                            @Override
//                            public void onSuccess(Text visionText) {
//                                // Task completed successfully
//                                // [START_EXCLUDE]
//                                // [START get_text]
//                                for (Text.TextBlock block : visionText.getTextBlocks()) {
//                                    Rect boundingBox = block.getBoundingBox();
//                                    Point[] cornerPoints = block.getCornerPoints();
//                                    String text = block.getText();
//
//                                    for (Text.Line line: block.getLines()) {
//                                        // ...
//                                        for (Text.Element element: line.getElements()) {
//                                            // ...
//                                        }
//                                    }
//                                }
//                                // [END get_text]
//                                // [END_EXCLUDE]
//                            }
//                        })
//                        .addOnFailureListener(
//                                new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        // Task failed with an exception
//                                        // ...
//                                    }
//                                });
//        // [END run_detector]
//    }
//
//    private void processTextBlock(Text result) {
//        // [START mlkit_process_text_block]
//        String resultText = result.getText();
//        for (Text.TextBlock block : result.getTextBlocks()) {
//            String blockText = block.getText();
//            Point[] blockCornerPoints = block.getCornerPoints();
//            Rect blockFrame = block.getBoundingBox();
//            for (Text.Line line : block.getLines()) {
//                String lineText = line.getText();
//                Point[] lineCornerPoints = line.getCornerPoints();
//                Rect lineFrame = line.getBoundingBox();
//                for (Text.Element element : line.getElements()) {
//                    String elementText = element.getText();
//                    Point[] elementCornerPoints = element.getCornerPoints();
//                    Rect elementFrame = element.getBoundingBox();
//                }
//            }
//        }
//        // [END mlkit_process_text_block]
//    }
//
//    private TextRecognizer getTextRecognizer() {
//        // [START mlkit_local_doc_recognizer]
//        TextRecognizer detector = TextRecognition.getClient();
//        // [END mlkit_local_doc_recognizer]
//
//        return detector;
//    }
}
