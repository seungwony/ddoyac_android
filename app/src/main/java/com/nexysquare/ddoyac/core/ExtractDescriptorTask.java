package com.nexysquare.ddoyac.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nexysquare.ddoyac.util.MatConvertor;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.lang.ref.WeakReference;

public class ExtractDescriptorTask extends BackgroundTask {


    private WeakReference weakReferce;
//    private final Bitmap _bmpimg;
    private static long startTime, endTime;
    private static int min_dist = 80;
//    private ReentrantLock lock = new ReentrantLock();
    private String desJson;
    public ExtractDescriptorTask(Activity activity, Bitmap bmpimg) {
        super(activity);
//        pd = new ProgressDialog(activity);
//        pd.setIndeterminate(true);
//        pd.setCancelable(true);
//        pd.setCanceledOnTouchOutside(false);
//        pd.setMessage("Processing...");
//        pd.show();
        startTime = System.currentTimeMillis();

        weakReferce = new WeakReference(bmpimg);
//        _bmpimg = bmpimg;

    }


    @Override
    public void doInBackground() {
        Mat img1, descriptors;
         FastFeatureDetector detector;
        Feature2D DescExtractor;

        MatOfKeyPoint keypoints;
//        lock.lock();
//        Realm realm = Realm.getDefaultInstance();
        try {

            Bitmap bmpimg = (Bitmap) weakReferce.get();
//            Bitmap img = bmpimg.copy(Bitmap.Config.ARGB_8888, true);

            img1 = new Mat();
            Utils.bitmapToMat(bmpimg, img1);
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
            detector = FastFeatureDetector.create(3);

            DescExtractor = ORB.create();
            keypoints = new MatOfKeyPoint();

            descriptors = new Mat();
            detector.detect(img1, keypoints);
            DescExtractor.compute(img1, keypoints, descriptors);

            desJson = MatConvertor.SerializeFromMat(descriptors);
//            des2 = MatConvertor.matToJson(dupDescriptors);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {


        }

    }

    @Override
    public void onPostExecute() {
        try {
//            Mat img3 = new Mat();
//            MatOfByte drawnMatches = new MatOfByte();
//            Features2d.drawMatches(img1, keypoints, img2, dupKeypoints,
//                    matches_final_mat, img3, GREEN, RED, drawnMatches, Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
//            bmp = Bitmap.createBitmap(img3.cols(), img3.rows(),
//                    Bitmap.Config.ARGB_8888);
//            Imgproc.cvtColor(img3, img3, Imgproc.COLOR_BGR2RGB);
//            Utils.matToBitmap(img3, bmp);
//            List<DMatch> finalMatchesList = matches_final_mat.toList();
//            final int matchesFound = finalMatchesList.size();
            endTime = System.currentTimeMillis();
//                if (finalMatchesList.size() > min_matches)// dev discretion for
//                // number of matches to
//                // be found for an image
//                // to be judged as
//                // duplicate
//                {
//                    text = finalMatchesList.size()
//                            + " matches were found. Possible duplicate image.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = true;
//                } else {
//                    text = finalMatchesList.size()
//                            + " matches were found. Images aren't similar.\nTime taken="
//                            + (endTime - startTime) + "ms";
//                    isDuplicate = false;
//                }
//            text = finalMatchesList.size()+ " matches were found.\nTime taken="+ (endTime - startTime) + "ms";;
//            pd.dismiss();
//            lock.unlock();

            if(desJson!=null){
                final Intent intent = new Intent("extract_des_json_complete");
                intent.putExtra("json", desJson);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }else{
                Log.e("ExtractDescriptorTask", "desJson is null");
            }


//            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

}