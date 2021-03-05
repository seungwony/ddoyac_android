package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nexysquare.ddoyac.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.rectangle;

public class OpenCVContourActivity extends AppCompatActivity {
    private static final int PICK_PHOTO_REQUEST_CODE = 101;
    private final static String TAG = "OpenCVContourActivity";
    ImageView img1, img2, img3, img4;

    public static void open(Context context) {
        Intent intent = new Intent(context, OpenCVContourActivity.class);

        context.startActivity(intent);
    }

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opencv_contour);

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);

        Button img_pickup_btn = findViewById(R.id.img_pickup_btn);
        img_pickup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PHOTO_REQUEST_CODE){
            if (resultCode == RESULT_OK) {


                Uri uri = data.getData();

                Mat img = new Mat();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

//                    bitmap = BitmapUtil.bitmapResizer(bitmap, 512,512);
                    Utils.bitmapToMat(bitmap, img);
//                    testContour(img);
                    otsu(bitmap);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }




//                Utils.bitmapToMat(bmpimg2, img2);
//                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
//                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);
            }
        }
    }
    private double otsu(Bitmap bmp) {

        img1.setImageBitmap(bmp);
        Mat rgba = new Mat();
        Utils.bitmapToMat(bmp, rgba);

        Mat edges = new Mat(rgba.size(), CvType.CV_8UC4);
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGBA2GRAY, 4);

        double a = Imgproc.threshold(edges, edges, 0, 255, Imgproc.THRESH_OTSU);

        Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        int nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
        img2.setImageBitmap(scaled);



        Imgproc.threshold(edges, edges, 0, 255, Imgproc.THRESH_BINARY_INV);


        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
        img3.setImageBitmap(scaled);




        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.CHAIN_APPROX_NONE);

        Imgproc.drawContours(rgba, contours, 1, new Scalar(255, 0, 0), 1);


        /*
        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
*/
        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);

        // Imgproc.drawContours(edges2, contours, 1, new Scalar(255, 0, 0), 10);



        img4.setImageBitmap(scaled);




        return a;

    }
    private void testContour(Mat imageMat) {
        Mat rgb = new Mat();  //rgb color matrix
        rgb = imageMat.clone();
        Mat grayImage = new Mat();  //grey color matrix
        Imgproc.cvtColor(rgb, grayImage, Imgproc.COLOR_RGB2GRAY);

        Mat gradThresh = new Mat();  //matrix for threshold
        Mat hierarchy = new Mat();    //matrix for contour hierachy
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        //Imgproc.threshold(grayImage,gradThresh, 127,255,0);  global threshold
        Imgproc.adaptiveThreshold(grayImage, gradThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 12);  //block size 3
        Imgproc.findContours(gradThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        if(contours.size()>0) {
            for(int idx = 0; idx < contours.size(); idx++) {
                Rect rect = Imgproc.boundingRect(contours.get(idx));
                if (rect.height > 10 && rect.width > 40 && !(rect.width >= 512 - 5 && rect.height >= 512 - 5)){
                    rectangle(imageMat, new Point(rect.br().x - rect.width, rect.br().y - rect.height)
                            , rect.br()
                            , new Scalar(0, 255, 0), 5);
                }

            }



            matIntoImageView(rgb, img1);
            matIntoImageView(grayImage, img2);
            matIntoImageView(gradThresh, img3);
            matIntoImageView(imageMat, img4);

//            Imgcodecs.imwrite("/tmp/dev/doc_original.jpg", rgb);
//            Imgcodecs.imwrite("/tmp/dev/doc_gray.jpg", grayImage);
//            Imgcodecs.imwrite("/tmp/dev/doc_thresh.jpg", gradThresh);
//            Imgcodecs.imwrite("/tmp/dev/doc_contour.jpg", imageMat);
        }
    }

    private void matIntoImageView(Mat imgMat, ImageView imgView){
        Bitmap bmp = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(),
                Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2RGB);
        Utils.matToBitmap(imgMat, bmp);
        imgView.setImageBitmap(bmp);
    }

    public void removeVerticalLines(Mat img, int limit) {
        Mat lines=new Mat();
        int threshold = 100; //선 추출 정확도
        int minLength = 80; //추출할 선의 길이
        int lineGap = 5; //5픽셀 이내로 겹치는 선은 제외
        int rho = 1;
        Imgproc.HoughLinesP(img, lines, rho, Math.PI/180, threshold, minLength, lineGap);
        for (int i = 0; i < lines.total(); i++) {
            double[] vec=lines.get(i,0);
            Point pt1, pt2;
            pt1=new Point(vec[0],vec[1]);
            pt2=new Point(vec[2],vec[3]);
            double gapY = Math.abs(vec[3]-vec[1]);
            double gapX = Math.abs(vec[2]-vec[0]);
            if(gapY>limit && limit>0) {
                //remove line with black color
                Imgproc.line(img, pt1, pt2, new Scalar(0, 0, 0), 10);
            }
        }
    }
}
