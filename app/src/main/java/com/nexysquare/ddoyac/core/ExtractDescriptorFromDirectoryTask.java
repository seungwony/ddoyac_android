package com.nexysquare.ddoyac.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.model.PillRealm;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.MatConvertor;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.realm.Realm;

public class ExtractDescriptorFromDirectoryTask extends BackgroundTask {

    private ProgressDialog pd;
    //    private final Bitmap _bmpimg;
    private static long startTime, endTime;
    private static int min_dist = 80;



    private final File fd;
    //    private ReentrantLock lock = new ReentrantLock();
    public ExtractDescriptorFromDirectoryTask(Activity activity, File fd) {
        super(activity);
        pd = new ProgressDialog(activity);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Processing...");
        pd.show();
        startTime = System.currentTimeMillis();
        this.fd = fd;


//        _bmpimg = bmpimg;

    }



    @Override
    public void doInBackground() {



        File[] files = fd.listFiles();



//        total_count = files.length;

        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "(" + (i + 1) + "" + "/ " + files.length + " ) FileName:" + files[i].getName());
            Mat img1, descriptors;
            FastFeatureDetector detector;
            Feature2D DescExtractor;

            MatOfKeyPoint keypoints;
//        lock.lock();
            Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
            try {

                String name = files[i].getName();


                String filePath = files[i].getPath();

                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                Bitmap fastblur = BitmapUtil.fastblur(bitmap, (float)0.8, 2);
                Bitmap resized = BitmapUtil.resizeBitmapImage(bitmap, 150);
//                Bitmap fastblur = BitmapUtil.fastblur(resized, (float)1.0, 2);
//                Bitmap bmpimg =
//                    .copy(Bitmap.Config.ARGB_8888, true);

                img1 = new Mat();
                Utils.bitmapToMat(resized, img1);
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
                detector = FastFeatureDetector.create(8);

                DescExtractor = ORB.create();
//            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                keypoints = new MatOfKeyPoint();
//            dupKeypoints = new MatOfKeyPoint();

                descriptors = new Mat();
//            dupDescriptors = new Mat();

//            matches = new MatOfDMatch();
                detector.detect(img1, keypoints);
//            Log.d("LOG!", "number of query Keypoints= " + keypoints.size());

                // Descript keypoints
                DescExtractor.compute(img1, keypoints, descriptors);

//            Log.d("LOG!", "number of descriptors= " + descriptors.size());
//            Log.d("LOG!",
//                    "number of dupDescriptors= " + dupDescriptors.size());
                // matching descriptors

                String des = MatConvertor.SerializeFromMat(descriptors);
//            des2 = MatConvertor.matToJson(dupDescriptors);

                writeDescriptor(des);
                realm.beginTransaction();
                PillRealm pill = realm.createObject(PillRealm.class); // Create managed objects directly

                pill.setName(name);
                pill.setDescriptor(des);
                realm.commitTransaction();



//                writeDescriptor(des2);
//            matcher.match(descriptors, dupDescriptors, matches);
//            Log.d("LOG!", "Matches Size " + matches.size());
//            Log.d("LOG!", des1);
                // New method of finding best matches
//            List<DMatch> matchesList = matches.toList();
//            List<DMatch> matches_final = new ArrayList<DMatch>();
//            for (int i = 0; i < matchesList.size(); i++) {
//                if (matchesList.get(i).distance <= min_dist) {
//                    matches_final.add(matches.toList().get(i));
//                }
//            }

//            matches_final_mat = new MatOfDMatch();
//            matches_final_mat.fromList(matches_final);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                realm.close();
            }
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
            if(pd!=null){
                pd.dismiss();
            }

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
            Log.d("result", "Time taken="+ (endTime - startTime) + "ms");
//            pd.dismiss();
//            lock.unlock();

            final Intent intent = new Intent("extract_complete");
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

//            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void writeDescriptor(String des){
        try {


            File logs = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/opencv4test/descriptor_list_v3.txt");

            FileWriter fw;
            BufferedWriter bw;

            File dir = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath() + "/opencv4test/");
            dir.mkdirs();

            logs.createNewFile();

            logs = new File(
                    Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/opencv4test/descriptor_list_v3.txt");

            fw = new FileWriter(logs, true);
            bw = new BufferedWriter(fw);

            bw.write(des + "\n");
            bw.close();



        } catch (IOException e1) {

            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }
}