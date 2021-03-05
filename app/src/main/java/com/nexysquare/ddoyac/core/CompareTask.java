package com.nexysquare.ddoyac.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.model.Pill;
import com.nexysquare.ddoyac.model.PillRealm;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.util.MatConvertor;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.Realm;

public class CompareTask extends BackgroundTask {

//    private final Mat descriptor;
    private ProgressDialog pd;
    private static long startTime, endTime;
    private static int min_dist = 40;
//    private static int min_dist = 40;
//    private static int min_dist = 80;
    private WeakReference weakReferce;
    private final ArrayList<Pill> results;
    private ArrayList<Integer> extractedColors;
    public CompareTask(Activity activity, Mat descriptor, ArrayList<Integer> extractedColors, ArrayList<Pill> results) {
        super(activity);
//        pd = new ProgressDialog(activity);
//        pd.setIndeterminate(true);
//        pd.setCancelable(true);
//        pd.setCanceledOnTouchOutside(false);
//        pd.setMessage("Processing...");
//        pd.show();

        startTime = System.currentTimeMillis();
//        mItems = list;
//        this.descriptor = descriptor;
        weakReferce = new WeakReference(descriptor);
        this.results =  results;
        this.extractedColors = extractedColors;
    }

    private int getIdx(String color){
        for(int i = 0 ; i < ColorUtils.COLORS_KOR.length ; i++){
            if(ColorUtils.COLORS_KOR[i].equals(color)){
                return i;
            }
        }

        return -1;
    }



    private boolean isExtractedColor(String filename){

//        ArrayList<String> colors = new ArrayList<>();
        String[] split = filename.split("_");
        if(split.length>2){



            String colorName = split[split.length - 1 - 1];

            //trim and split comma
            colorName = colorName.replaceAll(" ", "");




            String[] pillArr = colorName.split(",");

            for (String p:pillArr){

//                Log.d("Pill color", p);

//                if(!colors.contains(p)){
//                    colors.add(p);
//                }

//                if(extractedColors.contains(p)){
//                    Log.d("Compared equality color", p + " :: " + filename);
//                    return true;
//                }
//                StringBuilder sb = new StringBuilder();
//                sb.append(pillArr[i]);
//                String pill = sb.toString();
                int colorIdx = ColorUtils.getColorIdx(p);
                if(extractedColors.contains(colorIdx)){
                    return true;
                }
//                for(int exColor : extractedColors){
//
//                    if(exColor == colorIdx){
//                        Log.d("Compared equality color", exColor + " :: " + colorIdx + " :: " + filename);
//                        return true;
//                    }else{
//                        Log.d("Compared equality color", "diffrent :: " + exColor + " :: " + colorIdx + " :: "+ filename);
//                    }
//
//                }

            }
        }

        return false;

    }

    @Override
    public void doInBackground() {




        DescriptorMatcher matcher = DescriptorMatcher
                .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfDMatch matches = new MatOfDMatch();
        MatOfDMatch matches_final_mat;
        Realm realm =Realm.getInstance(GlobalApp.getRealmConfiguration());
        Mat descriptor = (Mat)weakReferce.get();
        final int total = realm.where(PillRealm.class).findAll().size();
        int num = 1;
        try {
            for ( PillRealm pill : realm.where(PillRealm.class).findAll() ){

                if(!isExtractedColor(pill.getName())){
                    continue;
                }

                String json = pill.getDescriptor();
                if(json!=null && !json.equals("{}")&& !json.equals("") && !json.equals("{\"bytes\":[],\"cols\":0,\"rows\":0,\"type\":0}")){

//                    Log.d("compareTask :: ", num + " / " + total);
//                    Log.d("compareTask json :: ", json);
                    //pd.setMessage("Processing... ( " + num+" / "+total + " )");
                    num++;
                    Mat des = MatConvertor.DeserializeToMat(json);

                    matcher.match(descriptor, des, matches);

                    List<DMatch> matchesList = matches.toList();



                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());
//                    Log.d("match :: ", "match : "+ matchesList.size());
//                    List<DMatch> matches_final = new ArrayList<DMatch>();
//                    for (int i = 0; i < matchesList.size(); i++) {
//                        if (matchesList.get(i).distance <= min_dist) {
//                            matches_final.add(matches.toList().get(i));
//                        }
//                    }
//
//                    matches_final_mat = new MatOfDMatch();
//                    matches_final_mat.fromList(matches_final);
//
//
//                    List<DMatch> finalMatchesList = matches_final_mat.toList();
//                    int matchesCount = finalMatchesList.size();



                    int matchesCount = matchesList.size();

                    // Asynchronously update objects on a background thread
//                    realm.executeTransactionAsync(new Realm.Transaction() {
//                        @Override
//                        public void execute(Realm bgRealm) {
//                            pill.setMatched(matchesCount);
//                        }
//                    }, new Realm.Transaction.OnSuccess() {
//                        @Override
//                        public void onSuccess() {
//                            // Original queries and Realm objects are automatically updated.
////                            puppies.size(); // => 0 because there are no more puppies younger than 2 years old
////                            managedDog.getAge();   // => 3 the dogs age is updated
//                        }
//                    });
//                    realm.beginTransaction();
//                    pill.setMatched(matchesCount);
//                    realm.commitTransaction();

                    if(matchesCount>1){
                        Pill item = new Pill();
                        item.setName(pill.getName());
                        item.setMatched(matchesCount);
                        item.setDescriptor(pill.getDescriptor());
                        results.add(item);

                        if(results.size() % 100 == 0){
                            //notify data


//                            int finalNum = num - 1;
//                            final int percentage = (finalNum /total) * 100;
//                            Log.d("progress", percentage + "%  ("+finalNum + "/"+total+")" );
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    final Intent intent = new Intent("matching_updated");



//                                    intent.putExtra("progress", percentage);
                                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                                }
                            });


                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            realm.close();
        }


//            pd.dismiss();
//            lock.unlock();


    }

    @Override
    public void onPostExecute() {
        if(pd!=null){
            pd.dismiss();
        }

//        Collections.sort(results, new Comparator<Pill>() {
//            @Override
//            public int compare(Pill o1, Pill o2) {
//                if(o1.getMatched() < o2.getMatched()){
//                    return 1;
//                }else if(o1.getMatched() > o2.getMatched()){
//                    return -1;
//                }
//
//                return 0;
//            }
//        });

//        for(Pill p : results){
//            Log.d("result", "name : " + p.getName() + " matches : " + p.getMatched() );
//        }

        endTime = System.currentTimeMillis();
        Log.d("result", "Time taken="+ (endTime - startTime) + "ms");

        final Intent intent = new Intent("matching_completed");
        //intent.putExtra("results", results);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }



}