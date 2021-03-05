package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nexysquare.ddoyac.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;

public class OpenCVTestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private final String TAG = "OpenCVTestActivity";
    private boolean hasFlash;

    //OpenCV camera
    JavaCameraView javaCameraView;

    //Matrixes
    Mat imageRGBA, imageGray, imageCanny, imageinRange, imageTMP, imageContours, imageToBmpTMP;

    //Interface
    LinearLayout linearLayout;
    Button b1, b2, b3;
    TextView tv1, tvSeekBar1, tvSeekBar2, tvImageInfo, tvObjectInfo;
    SeekBar seekBar1, seekBar2;

    //variables
    int frameCounter = 0;
    int tresh1 = 50;
    int tresh2 = 150;
    int erosionSize = 1;
    int dilatationSize = 1;
    boolean isLight = false;
    boolean isb2 = false;
    String stream;

    Scalar contourColor = new Scalar(255,0,0,255);

    //file parametres
    String filename = "file";
    String filepath = "MyAlbum";
    FileOutputStream out = null;
    boolean success = false;
    Bitmap bmp = null;

    Date data = new Date();
    CharSequence currentDataAndTime;


    int counter; //testing

    BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    javaCameraView.enableView();
                    break;
                }

                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }

            super.onManagerConnected(status);
        }
    };

    public static void open(Context context) {
        Intent intent = new Intent(context, OpenCVTestActivity.class);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv_test);
        javaCameraView = (JavaCameraView) findViewById(R.id.cameraView);
        javaCameraView.setCvCameraViewListener(this);



        //initializing interface elements
        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);

        tv1 = (TextView) findViewById(R.id.textView);
        tvSeekBar1 = (TextView) findViewById(R.id.textView2);
        tvSeekBar2 = (TextView) findViewById(R.id.textView3);
        tvImageInfo = (TextView)findViewById(R.id.imageInfo);
        tvObjectInfo = (TextView)findViewById(R.id.objectInfo);

        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        b2.setVisibility(View.INVISIBLE);
        tv1.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        javaCameraView.setVisibility(View.VISIBLE);


//        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
//        if (!hasFlash)
//        {
//            // device doesn't support flash
//            // Show alert message and close the application
//            AlertDialog alert = new AlertDialog.Builder(this)
//                    .create();
//            alert.setTitle("Error");
//            alert.setMessage("Sorry, your device doesn't support flash light!");
//            alert.setButton("OK", new DialogInterface.OnClickListener()
//            {
//                public void onClick(DialogInterface dialog, int which)
//                {
//                    // closing the application
//                    finish();
//                }
//            });
//            alert.show();
//            return;
//        }
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                                            {
                                                @Override
                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                                                {
                                                    tresh1 = progress;
                                                    tvSeekBar1.setText("Treshhold 1: " + String.valueOf(tresh1));
                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar)
                                                {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar)
                                                {

                                                }
                                            }
        );
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                                            {
                                                @Override
                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                                                {
                                                    tresh2 = progress;
                                                    tvSeekBar2.setText("Treshhold 2: " + String.valueOf(tresh2));

                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar)
                                                {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar)
                                                {

                                                }
                                            }
        );
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            success = false;
        }
        else {
            success = true;
        }
        javaCameraView.setOnClickListener(new JavaCameraView.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                savePicture();
            }
        });
    }

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    protected void onPause()
    {
        super.onPause();
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }

        imageRGBA.release();
        imageGray.release();
        imageCanny.release();
        imageinRange.release();
        imageTMP.release();
        imageContours.release();

    }

    protected void onDestroy()
    {
        super.onDestroy();
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
        imageRGBA.release();
        imageGray.release();
        imageCanny.release();
        imageinRange.release();
        imageTMP.release();
        imageContours.release();
    }

    protected void onResume()
    {
        super.onResume();
//        if (!OpenCVLoader.initDebug())
//        {
//            Log.d(TAG, "OpenCV not loaded");
//            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        } else
//        {
//            Log.d(TAG, "OpenCV loaded");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
//            tv1.setText("OpenCV OK!");
//        }

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }

    }

    public void onCameraViewStarted(int width, int height)
    {
        imageRGBA = new Mat(height, width, CvType.CV_8UC4);
        imageGray = new Mat(imageRGBA.size(), CvType.CV_8UC1);
        imageCanny = new Mat(imageRGBA.size(), CvType.CV_8UC1);
        imageinRange = new Mat(imageRGBA.size(),CvType.CV_8UC1);
        imageTMP = new Mat(imageRGBA.size(),CvType.CV_8UC1);
        imageToBmpTMP = new Mat(imageRGBA.size(), CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped()
    {
        imageRGBA.release();
        imageGray.release();
        imageCanny.release();
        imageinRange.release();
        imageTMP.release();
        imageContours.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        imageRGBA = inputFrame.rgba();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        imageContours = new Mat(imageRGBA.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(imageRGBA, imageGray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.Canny(imageGray, imageCanny, tresh1,tresh2);    //only for test

        Core.inRange(imageGray, new Scalar(tresh1, tresh1, tresh1), new Scalar(tresh2, tresh2, tresh2),imageinRange);

        //openinng image (erode+dilate)
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2* erosionSize + 1, 2* erosionSize +1));
        Imgproc.erode(imageinRange,imageTMP,element1);
        Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2* dilatationSize + 1, 2* dilatationSize +1));
        Imgproc.dilate(imageinRange,imageTMP,element2);

        //finding countours
        Imgproc.findContours(imageTMP,contours,hierarchy,Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        RotatedRect[] minRect = new RotatedRect[contours.size()];
        double[] height = new double[contours.size()];
        double[] width = new double[contours.size()];


        switch (counter)
        {
            case 0:
                return imageRGBA;
            case 1:
                return imageGray;
            case 2:
                return imageCanny;
            case 3:
                return imageinRange;
            case 4:
                return imageTMP;
            case 5:
                for (int i = 0; i < contours.size(); i++)
                {
                    MatOfPoint matOfPoint = contours.get(i);
                    MatOfPoint2f matOfPoint2f = new MatOfPoint2f(matOfPoint.toArray());
                    minRect[i] = Imgproc.minAreaRect(matOfPoint2f);
                    Point[] rectPoints = new Point[4];
                    minRect[i].points(rectPoints);
                    for (int j = 0; j < 4; j++)
                    {
                        Imgproc.line(imageRGBA, rectPoints[j], rectPoints[(j + 1) % 4],contourColor);
                    }
                    if(minRect[i].size.height>100 && minRect[i].size.height<600 || minRect[i].size.width>100 && minRect[i].size.width<600)
                    {
                        height[i] = Math.round(minRect[i].size.height);
                        width[i] = Math.round(minRect[i].size.width);
                        stream = String.valueOf(i)+": "+String.valueOf(height[i]) +"x"+ String.valueOf(width[i]);

                        /**
                         * Draws a text string.
                         *
                         * The function cv::putText renders the specified text string in the image. Symbols that cannot be rendered
                         * using the specified font are replaced by question marks. See #getTextSize for a text rendering code
                         * example.
                         *
                         * @param img Image.
                         * @param text Text string to be drawn.
                         * @param org Bottom-left corner of the text string in the image.
                         * @param fontFace Font type, see #HersheyFonts.
                         * @param fontScale Font scale factor that is multiplied by the font-specific base size.
                         * @param color Text color.
                         * it is at the top-left corner.
                         */

                        Imgproc.putText(imageRGBA,stream, new Point(minRect[i].center.x - minRect[i].size.width/2,
                                        minRect[i].center.y - minRect[i].size.height/2),
                                FONT_HERSHEY_SIMPLEX, 1.0,contourColor);
                    }
                }
                // for (int contourId = 0; contourId < contours.size(); contourId++) {
                //     Imgproc.drawContours(imageContours, contours, contourId, contourColor, -1);
                // }
                return imageRGBA;
            default:
                return imageRGBA;
        }
    }


    public void onClick(View view)
    {
        switch (counter)
        {
            case 0:
                counter = 1;
                tvImageInfo.setText("Gray");
                b1.setText("Change to Canny");
                b2.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                break;
            case 1:
                counter = 2;
                tvImageInfo.setText("Canny");
                b1.setText("Change to opened");
                b2.setVisibility(View.VISIBLE);
                b2.setText("Show tresholds");
                isb2 = false;
                linearLayout.setVisibility(View.INVISIBLE);
                break;
            case 2:
                counter = 3;
                tvImageInfo.setText("Opened");
                b1.setText("Change to contours");
                b2.setVisibility(View.VISIBLE);
                b2.setText("Show scalars");
                isb2 = false;
                linearLayout.setVisibility(View.INVISIBLE);
                break;
            case 3:
                counter = 4;
                tvImageInfo.setText("Contours");
                b2.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                b1.setText("Change to drawing");
                break;
            case 4:
                counter = 5;
                tvImageInfo.setText("Drawing");
                b1.setText("Change to rgba");
                b2.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                break;
            case 5:
                counter = 0;
                tvImageInfo.setText("RGBA");
                b1.setText("Change to gray");
                b2.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void onClick2(View view)
    {
        switch (counter)
        {
            case 2:

                if (isb2 == false)
                {
                    b2.setText("Hide tresholds");
                    linearLayout.setVisibility(View.VISIBLE);
                    isb2 = true;
                } else
                {
                    b2.setText("Show tresholds");
                    linearLayout.setVisibility(View.INVISIBLE);
                    isb2 = false;

                }
                break;
            case 3:
                if (isb2 == false)
                {
                    b2.setText("Hide scalars");
                    linearLayout.setVisibility(View.VISIBLE);
                    isb2 = true;
                } else
                {
                    b2.setText("Show scalars");
                    linearLayout.setVisibility(View.INVISIBLE);
                    isb2 = false;

                }

        }
    }



    public void savePicture()
    {
        data = new Date();
        try {
            currentDataAndTime = DateFormat.format("yyyy-MM-dd hh:mm:ss", data.getTime());
            //testing on RGBA
            bmp = Bitmap.createBitmap(imageRGBA.cols(), imageRGBA.rows(), Bitmap.Config.ARGB_8888);
            switch (counter)
            {
                case 0:
                    imageRGBA.copyTo(imageToBmpTMP);
                    break;
                case 1:
                    imageGray.copyTo(imageToBmpTMP);
                    break;
                case 2:
                    imageCanny.copyTo(imageToBmpTMP);
                    break;
                case 3:
                    imageinRange.copyTo(imageToBmpTMP);
                    break;
                case 4:
                    imageTMP.copyTo(imageToBmpTMP);
                    break;
                case 5:
                    imageRGBA.copyTo(imageToBmpTMP);
                    break;
                default:
                    imageRGBA.copyTo(imageToBmpTMP);
                    break;
            }
            Utils.matToBitmap(imageToBmpTMP, bmp);

        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }
        out = null;
        filename ="Photo-"+String.valueOf(currentDataAndTime)+".png";
        if (success)
        {
            File dest = new File(getExternalFilesDir(filepath), filename);
            try {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        Log.d(TAG, "OK!!");
                    }
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }
        toast("Picture Taken!");
    }

    private void toast(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    // if USB cable is connected:

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
