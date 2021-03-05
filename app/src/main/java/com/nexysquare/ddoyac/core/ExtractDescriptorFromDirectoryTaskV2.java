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
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.ExtractDrugInfo;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExtractDescriptorFromDirectoryTaskV2 extends BackgroundTask {

    private final static String TAG = "ExtractDescriptorFromDirectoryTaskV2";
    private ProgressDialog pd;
    //    private final Bitmap _bmpimg;
    private static long startTime, endTime;
    private static int min_dist = 80;


    private List<File> listf;

    //    private ReentrantLock lock = new ReentrantLock();
    public ExtractDescriptorFromDirectoryTaskV2(Activity activity, String dir) {
        super(activity);
        pd = new ProgressDialog(activity);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Processing...");
        pd.show();
        startTime = System.currentTimeMillis();

        File directory = new File(dir);
        listf =getListFiles(directory);



//        _bmpimg = bmpimg;

    }

    public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }
        //System.out.println(fList);
        return resultList;
    }

    private List<File> getListFiles(File parentDir) {
        List<File> inFiles = new ArrayList<>();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }

    @Override
    public void doInBackground() {




//        File[] files = fd.listFiles();



//        total_count = files.length;


        int i = 0;
        for (File file : listf) {
            Log.d("Files", "(" + (i + 1) + "" + "/ " + listf.size() + " ) FileName:" + file.getName());

            if(file.isDirectory()){
                Log.e("Extact", "it's directory : " + file.getAbsolutePath());
            }
            Mat img1, descriptors;
            FastFeatureDetector detector;
            Feature2D DescExtractor;

            i++;

            MatOfKeyPoint keypoints;
//        lock.lock();
            Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
            try {



                //{이름}_{모양}_{식벽번호}_{색깔}_{방향}
                //4 _   1  _  3 _  2


                //1. db 조회 1개 발견시 매칭되는 앞 뒤 디스크립터 삽입
                //2. db 조회 2개 이상 발견시 일치되는 방향과 색깔 모양 표시 구분


                String name = file.getName();

                ExtractDrugInfo drugInfo = new ExtractDrugInfo(name);

//                Log.d(TAG, "number : " + drugInfo.getProNumber() );
//                Log.d(TAG, "name : " + drugInfo.getProductName() );
//                Log.d(TAG, "shape : " + drugInfo.getShape() );
//                Log.d(TAG, "color : " + drugInfo.getColorName() );
//                Log.d(TAG, "isback : " + drugInfo.isBack() );


                String filePath = file.getPath();

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

//                writeDescriptor(des);


//                realm.beginTransaction();
//                PillRealm pill = realm.createObject(PillRealm.class); // Create managed objects directly
//
//                pill.setName(name);
//                pill.setDescriptor(des);
//                realm.commitTransaction();





                //dddddd
//                des = name;


                int result_count = (int)realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber()).count();


                if(result_count==1){

                    Drug drug = realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber()).findFirst();




                    realm.beginTransaction();
                    if(drug!=null && des!=null){
                        if(drugInfo.isBack()){
                            if(!drug.getMark_back().equals("")){
                                drug.setDescriptor_back(des);
                            }

                        }else{

                            if(!drug.getMark_front().equals("")){
                                drug.setDescriptor_front(des);
                            }

                        }
                    }


                    realm.commitTransaction();

                }else if(result_count>1){


                    //{이름}_{모양}_{식벽번호}_{색깔}_{방향}
                    //4 _   1  _  3 _  2

                    String filaNameAsColor = drugInfo.isBack() ? "color_back" : "color_front";

                    int s_cound =  (int)realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                            .and()
                            .beginGroup()
                            .equalTo("color_front", drugInfo.getColorName())
                            .or()
                            .equalTo("color_back", drugInfo.getColorName())
                            .endGroup()
                            .count();


                    if(s_cound == 1){

                        Drug drug = realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                .and()
                                .beginGroup()
                                .equalTo("color_front", drugInfo.getColorName())
                                .or()
                                .equalTo("color_back", drugInfo.getColorName())
                                .endGroup()
                                .findFirst();
                        writeRealm(realm, drug, des, drugInfo.isBack());

                    }else if(s_cound > 1){



//시네크캡슐_장방형_200810292_분홍_앞.png

                        if(drugInfo.hasMarkinfo()){




                            if(drugInfo.hasPic()){
                                String mark_img = drugInfo.getMarkImg();

                                String MarkImgFieldName = drugInfo.isBack() ? "mark_img_back" : "mark_img_front";
                                int found_count =  (int)realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                        .and()
                                        .beginGroup()
                                        .equalTo("color_front", drugInfo.getColorName())
                                        .or()
                                        .equalTo("color_back", drugInfo.getColorName())
                                        .endGroup()
                                        .and()
                                        .equalTo("shape", drugInfo.getShape())
                                        .and()
                                        .equalTo(MarkImgFieldName, mark_img).count();
                                if(found_count==1){
                                    Drug drug = realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber()).and()
                                            .beginGroup()
                                            .equalTo("color_front", drugInfo.getColorName())
                                            .or()
                                            .equalTo("color_back", drugInfo.getColorName())
                                            .endGroup()

                                            .and()
                                            .equalTo("shape", drugInfo.getShape())
                                            .and()
                                            .equalTo(MarkImgFieldName, mark_img).findFirst();
                                    writeRealm(realm, drug, des, drugInfo.isBack());


                                }else{
                                    String msg = drugInfo.getProNumber() + " " + drugInfo.getProductName() + " info : db 결과 "+found_count+"개 at : same number,color,shape,mark at 310";
                                    writeException(msg);
                                }

                            }else{

                                String MarkFieldName = drugInfo.isBack() ? "mark_back" : "mark_front";
                                String mark = drugInfo.getMark();
                                int found_count =  (int)realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                        .and()
                                        .beginGroup()
                                        .equalTo("color_front", drugInfo.getColorName())
                                        .or()
                                        .equalTo("color_back", drugInfo.getColorName())
                                        .endGroup()
                                        .and()
                                        .equalTo("shape", drugInfo.getShape())
                                        .and()
                                        .equalTo(MarkFieldName, mark).count();

                                if(found_count==1){
                                    Drug drug = realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                            .and()
                                            .beginGroup()
                                            .equalTo("color_front", drugInfo.getColorName())
                                            .or()
                                            .equalTo("color_back", drugInfo.getColorName())
                                            .endGroup()
                                            .and()
                                            .equalTo("shape", drugInfo.getShape())
                                            .and()
                                            .equalTo(MarkFieldName, mark).findFirst();
                                    writeRealm(realm, drug, des, drugInfo.isBack());


                                }else{

                                    String msg = drugInfo.getProNumber() + " " + drugInfo.getProductName() + " info : db 결과 "+found_count+"개 at : same number,color,shape,mark at 346";
                                    Log.e(TAG, "proNum : " + drugInfo.getProNumber() + " proName : " + drugInfo.getProductName() + " shape : " + drugInfo.getShape() + " hasMark : " + drugInfo.hasMarkinfo() + " mark : " + drugInfo.getMark() + " color : "  + drugInfo.getColorName());
                                    writeException(msg);
                                }

                            }


                        }else{
                            int found_count =  (int)realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                    .and()
                                    .beginGroup()
                                    .equalTo("color_front", drugInfo.getColorName())
                                    .or()
                                    .equalTo("color_back", drugInfo.getColorName())
                                    .endGroup()
                                    .and()
                                    .equalTo("shape", drugInfo.getShape()).count();

                            if(found_count==1){
                                Drug drug = realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                        .and()   .beginGroup()
                                        .equalTo("color_front", drugInfo.getColorName())
                                        .or()
                                        .equalTo("color_back", drugInfo.getColorName())
                                        .endGroup()
                                        .and().equalTo("shape", drugInfo.getShape()).findFirst();


                                writeRealm(realm, drug, des, drugInfo.isBack());

                            }else{
                                String msg = drugInfo.getProNumber() + " " + drugInfo.getProductName() + " info : db 결과 "+found_count+"개 at : same number,color,shape, no mark info at 375";

                                Log.e(TAG, "proNum : " + drugInfo.getProNumber() + " proName : " + drugInfo.getProductName() + " shape : " + drugInfo.getShape() + " hasMark : " + drugInfo.hasMarkinfo() + "");
                                writeException(msg);
                            }
                        }



                    }else{


                        String msg = drugInfo.getProNumber() + " " + drugInfo.getProductName() + " info : db 결과 "+s_cound+"개 (it should be 0) at 385 \n" +
                                "color extract " + drugInfo.getColorName() + "\n" +
                                "isback " + drugInfo.isBack() ;


                       Drug drug =  realm.where(Drug.class).equalTo("p_no", drugInfo.getProNumber())
                                .and()
                                .beginGroup()
                                .equalTo("color_front", drugInfo.getColorName())
                                .or()
                                .equalTo("color_back", drugInfo.getColorName())
                                .endGroup().findFirst();

                       if(drug!=null){
                           Log.e(TAG, "drug is "+ drug.getP_name() + " ( " + drug.getP_no() + " ) - " + drug.getColor_front() + " : " + drug.getColor_back());
                       }else{
                           Log.e(TAG, "drug is null");
                           Log.e(TAG, "drugInfo.getColorName() : " + drugInfo.getColorName()  + " proNum : " +drugInfo.getProNumber()+" proName : " +drugInfo.getProductName());
                       }
                        writeException(msg);
                    }

//                    drugInfo.getColorName();

//                    for( Drug drug : results){
//
//                    }


                }else{

                    String msg = drugInfo.getProNumber() + " " + drugInfo.getProductName() + " error : db 결과 0개 at 400";
                    writeException(msg);
                }




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

    private void writeRealm(Realm realm, Drug drug, String des, boolean isBack){

        realm.beginTransaction();

        if(isBack){
                if(!drug.getMark_back().equals("")){
                    drug.setDescriptor_back(des);
                }

            }else{

                if(!drug.getMark_front().equals("")){
                    drug.setDescriptor_front(des);
                }

            }



        realm.commitTransaction();
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

    private void writeException(String des){
        try {


            File logs = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/opencv4test/exception_log.txt");

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
                            + "/opencv4test/exception_log.txt");

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