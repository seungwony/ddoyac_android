package com.nexysquare.ddoyac.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nexysquare.ddoyac.adapter.DrugAdapter;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.activity.DrugDetailV2Activity;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.util.JsonHelper;

import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ReferenceFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    Realm realm;
    private ArrayList<Drug> items = new ArrayList<>();
    private RealmResults<Drug> realmResults;

    private TextView warning_txt;
    private View warning_view;
    private RecyclerView recyclerView;
    private DrugAdapter adapter;
    private ProgressBar progress_bar;
    public static ReferenceFragment newInstance(String json) {

        Bundle args = new Bundle();
        args.putString("json", json);
        ReferenceFragment fragment = new ReferenceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ref, container, false);
        recyclerView = v.findViewById(R.id.recycler_view);
        warning_view = v.findViewById(R.id.warning_view);
        warning_txt = v.findViewById(R.id.warning_txt);
        progress_bar = v.findViewById(R.id.progress_bar);
        setUpRecyclerView();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        warning_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);

        realm = Realm.getInstance(GlobalApp.getRealmConfiguration());

//        realm.close();


    }


    @Override
    public void onResume() {
        super.onResume();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                initData();
            }
        }, 500);

    }
    private void testData(){
        realmResults = realm.where(Drug.class).findAllAsync();
        items.addAll(realmResults);
        progress_bar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();

        if(items.size()==0){
            warning_view.setVisibility(View.VISIBLE);
        }else{
            warning_view.setVisibility(View.GONE);
        }
    }

    private void initData(){


//        realmResults = realm.where(Drug.class).findAllAsync();

        String jsonStr="";
        Bundle bundle = getArguments();
        if(bundle!=null){
            jsonStr = bundle.getString("json");


            Gson gson = new Gson();
            JsonElement element = gson.fromJson (jsonStr, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            int p_no = Integer.parseInt(JsonHelper.hasJsonAndGetString(jsonObj, "p_no"));
            String p_name = JsonHelper.hasJsonAndGetString(jsonObj, "p_name");
            String mark_front = JsonHelper.hasJsonAndGetString(jsonObj, "mark_front");
            String mark_back = JsonHelper.hasJsonAndGetString(jsonObj, "mark_back");
            String searchable = JsonHelper.hasJsonAndGetString(jsonObj, "searchable");

            ArrayList<String>  mark_filter = new ArrayList<>();
            if(!mark_front.equals("")){
                mark_filter.add(mark_front);

            }

            if(!mark_back.equals("")){
                mark_filter.add(mark_back);
            }


            String[] mark_filter_arr = Arrays.copyOf(mark_filter.toArray(), mark_filter.toArray().length, String[].class);

//            String mark[] =

            String shape = JsonHelper.hasJsonAndGetString(jsonObj, "shape");

            String color_front = JsonHelper.hasJsonAndGetString(jsonObj, "color_front");
            String color_back = JsonHelper.hasJsonAndGetString(jsonObj, "color_back");

            ArrayList<String>  color_filter = new ArrayList<>();
            if(!color_front.equals(""))
                color_filter.add(color_front);
            if(!color_back.equals(""))
                color_filter.add(color_back);

            String[] colors_arr = Arrays.copyOf(color_filter.toArray(), color_filter.toArray().length, String[].class);

            Log.d(TAG, "p_no: " + p_no + " mark_front : "+ mark_front +  " mark_back : " + mark_back + " front_color : " + color_front + " back_color : " +color_back);
            Log.d(TAG, "searchable: " + searchable);




            RealmQuery<Drug> query = realm.where(Drug.class);

            if(!mark_front.equals("")){

                query
                        .beginGroup()
                        .contains("searchable", mark_front, Case.INSENSITIVE);

                if(!mark_back.equals("")){

                    query
                            .or()
                            .contains("searchable", mark_back, Case.INSENSITIVE)
                            .endGroup()
                            .and();
                }else{
                    query
                            .endGroup()
                            .and();
                }
            }else{
//                query.beginGroup();

                if(!mark_back.equals("")){
                    query.contains("searchable", mark_back, Case.INSENSITIVE)
                            .and();
                }
            }


            query
//                    .contains("searchable", mark_front)
//                    .in("searchable", mark_filter_arr)
//                    .endGroup()

                    .beginGroup()
                    .in("color_front", colors_arr)
                    .or()
                    .in("color_back", colors_arr)
                    .endGroup()
                    .and()

                    .not()
                    .equalTo("p_no", p_no);




            realmResults = query
                    .limit(15)
                    .findAllAsync();

            Log.d("RefrenceFragment","results size : " +  realmResults.size());

            items.addAll(realmResults);
        }else{
            realmResults = realm.where(Drug.class).findAllAsync();
            items.addAll(realmResults);
        }

//        realmResults = realm.where(Drug.class).findAllAsync();
//        items.addAll(realmResults);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        progress_bar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();

        if(items.size()==0){
            warning_view.setVisibility(View.VISIBLE);
        }else{
            warning_view.setVisibility(View.GONE);
        }


    }
    private void initOldData(){


//        realmResults = realm.where(Drug.class).findAllAsync();

        String jsonStr="";
        Bundle bundle = getArguments();
        if(bundle!=null){
            jsonStr = bundle.getString("json");


            Gson gson = new Gson();
            JsonElement element = gson.fromJson (jsonStr, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            int p_no = Integer.parseInt(JsonHelper.hasJsonAndGetString(jsonObj, "p_no"));
            String p_name = JsonHelper.hasJsonAndGetString(jsonObj, "p_name");
            String mark_front = JsonHelper.hasJsonAndGetString(jsonObj, "mark_front");
            String mark_back = JsonHelper.hasJsonAndGetString(jsonObj, "mark_back");

            ArrayList<String>  mark_filter = new ArrayList<>();
            if(!mark_front.equals("")){
                mark_filter.add(mark_front);

            }

            if(!mark_back.equals("")){
                mark_filter.add(mark_back);
            }


            String[] mark_filter_arr = Arrays.copyOf(mark_filter.toArray(), mark_filter.toArray().length, String[].class);

//            String mark[] =

            String shape = JsonHelper.hasJsonAndGetString(jsonObj, "shape");

            String color_front = JsonHelper.hasJsonAndGetString(jsonObj, "color_front");
            String color_back = JsonHelper.hasJsonAndGetString(jsonObj, "color_back");

            ArrayList<String>  color_filter = new ArrayList<>();
            if(!color_front.equals(""))
                color_filter.add(color_front);
            if(!color_back.equals(""))
                color_filter.add(color_back);

            String[] colors_arr = Arrays.copyOf(color_filter.toArray(), color_filter.toArray().length, String[].class);

            Log.d(TAG, "p_no: " + p_no + " mark_front : "+ mark_front +  " mark_back : " + mark_back + " front_color : " + color_front + " back_color : " +color_back);


            RealmQuery<Drug> query = realm.where(Drug.class);


//            query
//                    .contains("p_name", p_name)
//                    .or();
            query
//                    .contains("p_name", p_name)
//                    .and()
//                    .beginGroup()
//                    .in("mark_front", mark_filter_arr)
//                    .or()
//                    .in("mark_back", mark_filter_arr)
//                    .endGroup()
//                    .and()
                    .beginGroup()
                    .in("color_front", colors_arr)
                    .or()
                    .in("color_back", colors_arr)
                    .endGroup()
                    .and()

                    .not()
                    .equalTo("p_no", p_no);




//            if(!mark_front.equals("")){
//                query.beginGroup().contains("mark_front", mark_front, Case.INSENSITIVE);
//                if(!mark_back.equals("")){
//                    query.or().contains("mark_front", mark_back, Case.INSENSITIVE);
//                }
//            }else{
//                query.beginGroup();
//            }


//            if(!mark_back.equals("")) {
//
//
//                query.contains("mark_back", mark_back, Case.INSENSITIVE);
//
//                if(!mark_front.equals("")){
//                    query.or()
//                            .contains("mark_back", mark_front, Case.INSENSITIVE);
//                }
//
//                query.endGroup();
//
//
//            }else {
//
//                query.endGroup();
//            }

//            query.and().beginGroup()
//                    .in("color_front", colors_arr)
//                    .or()
//                    .in("color_back", colors_arr)
//                    .endGroup()
//                    .and()
//                    .not()
//                    .equalTo("p_no", p_no);


            realmResults = query
//                    .limit(15)
                    .findAllAsync();

//            items.addAll(results);
//                  .beginGroup()
//                    .contains("mark_front", mark_front, Case.INSENSITIVE)
//                    .or()
//                    .contains("mark_back", mark_back, Case.INSENSITIVE)
//                    .endGroup()
//                    .and()
//                    .beginGroup()
//                    .equalTo("color_front", front_color)
//                    .or()
//                    .equalTo("color_back", back_color)
//                    .endGroup()
//            if(!mark_front.equals("")){
//                results = results.where().contains("mark_front", mark_front, Case.INSENSITIVE).findAll()
//            }



            ArrayList<Drug> filter = new ArrayList<>();



            final int LIMIT = 10;
            int count = 0;
            for(Drug drug : realmResults){
                String mark_front_rename =  drug.getMark_front().replaceAll("[^A-Za-z0-9\\s]", "");
                if(!mark_front_rename.equals("")){

                    for(String m : mark_filter_arr){
                        if(mark_front_rename.contains(m)){
                            Log.d(TAG, "mark_front_rename : "  + mark_front_rename);
                            filter.add(drug);
                            count++;
                            continue;
                        }
                    }

                }

                String mark_back_rename =  drug.getMark_back().replaceAll("[^A-Za-z0-9\\s]", "");
                if(!mark_back_rename.equals("")){
                    for(String m : mark_filter_arr){
                        if(mark_back_rename.contains(m)){

                            Log.d(TAG, "mark_back_rename : "  + mark_back_rename);
                            filter.add(drug);
                            count++;
                        }
                    }

                }

                if(count >= LIMIT){
                    break;
                }

            }

//            filter.

            Log.d("RefrenceFragment","results size : " +  filter.size());

            items.addAll(filter);
        }else{
            realmResults = realm.where(Drug.class).findAllAsync();
            items.addAll(realmResults);
        }

//        realmResults = realm.where(Drug.class).findAllAsync();
//        items.addAll(realmResults);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        progress_bar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();

        if(items.size()==0){
            warning_view.setVisibility(View.VISIBLE);
        }else{
            warning_view.setVisibility(View.GONE);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        recyclerView.setAdapter(null);

    }




    private void setUpRecyclerView() {

//        Log.d("SearchDrug", "items.size: "+items.size());
//        final MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false, getScreenHeight(this));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(RecyclerView.VERTICAL);

        adapter = new DrugAdapter(getContext(), items);
        recyclerView.setLayoutManager(lm);
//        recyclerView.setLayoutManager(new FrameLayoutMan);
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);

//        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//
//           getActivity().getDisplay().getRealMetrics(displaymetrics);
//        } else {
//            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        }
//
//
//        int height = displaymetrics.heightPixels;
//        int width = displaymetrics.widthPixels;
//
//        ViewGroup.LayoutParams params=recyclerView.getLayoutParams();
//        params.height = height;
//        params.width = width;
//        recyclerView.setLayoutParams(params);


//        recyclerView.setNestedScrollingEnabled(false);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

//        SearchDrugActivity.TouchHelperCallback touchHelperCallback = new SearchDrugActivity.TouchHelperCallback();
//        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
//        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
                Log.d("onViewRecycled","recyled-->"+ holder);
            }
        });
        adapter.setClickListener(new DrugAdapter.onClickListener() {
            @Override
            public void onClick(Drug drug, ImageView imageView) {
                DrugDetailV2Activity.open(getActivity(), imageView, drug.getP_no());
            }
        });
    }

//    private void addAllDrugs(RealmResults<Drug> realmResults){
//
//        if(items.size()>0) items.clear();
//
//        for(Drug drug : realmResults){
//            items.add(new DrugParcelable((drug)));
//        }
//    }
}
