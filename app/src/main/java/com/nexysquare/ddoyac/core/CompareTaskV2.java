package com.nexysquare.ddoyac.core;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.model.MatchedInfo;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class CompareTaskV2 extends BackgroundTask {

    private final static String TAG ="CompareTaskV2";
    //    private final Mat descriptor;
    private ProgressDialog pd;
    private static long startTime, endTime;
    private static int min_dist = 40;
    //    private static int min_dist = 40;
//    private static int min_dist = 80;
    private WeakReference weakReferce;

    private final ArrayList<DrugParcelable> drugs;
    private final ArrayList<String> filter_colors;
    private final ArrayList<String> filter_shapes;
    private final ArrayList<MatchedInfo> matchedInfos = new ArrayList<>();
    private final String keyword;
//    private RealmQuery<Drug> query;
    public CompareTaskV2(Activity activity, Mat descriptor, ArrayList<DrugParcelable> drugs, String keyword, ArrayList<String> filter_colors, ArrayList<String> filter_shapes) {
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

        this.drugs = drugs;
        this.keyword = keyword;
        this.filter_colors = filter_colors;
        this.filter_shapes = filter_shapes;
    }


//    public void setFilter(ArrayList<String> filter_colors, ArrayList<String> filter_shapes){
//
//        this.filter_colors.addAll(filter_colors);
//        this.filter_shapes.addAll(filter_shapes);
//    }



//    public void setQuery(RealmQuery<Drug> query){
//
//        this.query = query;
//    }



    @Override
    public void doInBackground() {

        drugs.clear();
        DescriptorMatcher matcher = DescriptorMatcher
                .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfDMatch matches = new MatOfDMatch();
        MatOfDMatch matches_final_mat;

        Mat descriptor = (Mat)weakReferce.get();



        int num = 1;

//        if(query==null) return;
        Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());

        try {

            RealmResults<Drug> realmResults = realm.where(Drug.class).findAll();


            RealmQuery<Drug> query = realmResults.where();
            String str_query = "";

            Log.d(TAG, "filter_colors : "+ filter_colors.size());
            Log.d(TAG, "filter_shapes : "+ filter_shapes.size());


            if(keyword!=null && !keyword.equals("")){

                query
                        .contains("searchable", keyword, Case.INSENSITIVE);

                str_query+= ".contains(\"searchable\", "+keyword+", Case.INSENSITIVE)";

            }

            if(filter_colors.size()>0){
                String[] colors_arr = Arrays.copyOf(filter_colors.toArray(), filter_colors.toArray().length, String[].class);
                if(keyword!=null && !keyword.equals("")) {
                    query.and();
                    str_query+= ".and()";
                }

                query
                        .beginGroup()
                        .in("color_front", colors_arr)
                        .or()
                        .in("color_back", colors_arr)
                        .endGroup();


                str_query+= "                    .beginGroup()\n" +
                        "                    .in(\"color_front\", colors_arr)\n" +
                        "                    .or()\n" +
                        "                    .in(\"color_back\", colors_arr)\n" +
                        "                    .endGroup()";


//            if(shapes.size()>0){
//                query.and();
//                str_query+= " .and()";
//            }


            }

            if(filter_shapes.size()>0){
                String[] shape_arr = Arrays.copyOf(filter_shapes.toArray(), filter_shapes.toArray().length, String[].class);

                if(str_query.length()>0){
                    query.and();
                    str_query+= ".and()";
                }

                query
                        .in("shape", shape_arr);


                str_query+= ".in(\"shape\", shape_arr)";

            }

            RealmResults<Drug> results = query
                    .findAll();

//            drugs.addAll(results);


            final int total = results.size();

            for (int i = 0 ; i <  results.size() ; i++ ){

                Log.d(TAG, "process ("+i+"/"+results.size()+")");

                Drug drug = results.get(i);
                DrugParcelable drugParcelable = new DrugParcelable(drug);


                String descriptor_front = drug.getDescriptor_front();
                String descriptor_back = drug.getDescriptor_back();

                MatchedInfo matchedInfo = new MatchedInfo();
                matchedInfo.setId(drug.getId());

                if(descriptor_front!=null && !descriptor_front.equals("{}")&& !descriptor_front.equals("") && !descriptor_front.equals("{\"bytes\":[],\"cols\":0,\"rows\":0,\"type\":0}")){

                    Mat des = MatConvertor.DeserializeToMat(descriptor_front);

                    matcher.match(descriptor, des, matches);

                    List<DMatch> matchesList = matches.toList();



                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());


                    int matchesCount = matchesList.size();


                    matchedInfo.setFront_matched(matchesCount);

                    if(matchesCount>1){
//                        realm.executeTransactionAsync(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                drug.setMatched_count_front(matchesCount);
//                            }
//                        });
                        drugParcelable.setMatched_count_front(matchesCount);
                    }
                }


                if(descriptor_back!=null && !descriptor_back.equals("{}")&& !descriptor_back.equals("") && !descriptor_back.equals("{\"bytes\":[],\"cols\":0,\"rows\":0,\"type\":0}")){

                    Mat des = MatConvertor.DeserializeToMat(descriptor_back);

                    matcher.match(descriptor, des, matches);

                    List<DMatch> matchesList = matches.toList();



                    matchesList = matchesList.stream().filter(t->t.distance <= min_dist).collect(Collectors.toList());

                    int matchesCount = matchesList.size();


                    if(matchesCount>1){


//                        realm.executeTransactionAsync(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                drug.setMatched_count_back(matchesCount);
//                            }
//                        });
                        drugParcelable.setMatched_count_back(matchesCount);
                        matchedInfo.setBack_matched(matchesCount);

                    }

                }

//                if(matchedInfo.getMaxMatched()>0){
//                    matchedInfos.add(matchedInfo);
//                }



                if(drugParcelable.getMaxMatchedCount()>0){
                    drugs.add(drugParcelable);
                }


                if(i % 1000 == 0){
                    //notify data

//                            int finalNum = num - 1;
//                            final int percentage = (finalNum /total) * 100;
//                            Log.d("progress", percentage + "%  ("+finalNum + "/"+total+")" );

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            final Intent intent = new Intent("matching_updated");
//                                    intent.putExtra("progress", percentage);
//                            intent.putExtra("matched_arr", matchedInfos);
                            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                        }
                    });


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
        intent.putExtra("matched_arr", matchedInfos);
        //intent.putExtra("results", results);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }



}