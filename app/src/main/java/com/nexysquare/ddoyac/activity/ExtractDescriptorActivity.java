package com.nexysquare.ddoyac.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nexysquare.ddoyac.adapter.PillAdapter;
import com.nexysquare.ddoyac.core.CompareTask;
import com.nexysquare.ddoyac.core.ExtractDescriptorFromDirectoryTaskV2;
import com.nexysquare.ddoyac.core.ExtractDescriptorTask;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.Pill;
import com.nexysquare.ddoyac.model.PillRealm;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.core.ExtractDescriptorFromDirectoryTask;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.MatConvertor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;

public class ExtractDescriptorActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ExtractDescriptorActivity";
    private static final int PICKFILE_REQUEST_CODE = 100;

    private static final int PICK_PHOTO_REQUEST_CODE = 101;

    private ImageView sel_img;
    private PillAdapter adapter;
//    private ArrayList<Pill> items;
    private LinearLayoutManager layoutManager;
    private TextView folder_path_txt, result_txt, color_info_txt;
    private static int count = 0;
    private static int total_count = 0;
    private ProgressDialog pd;

    private ReentrantLock lock = new ReentrantLock();

    private Realm realm;
    private RealmResults<PillRealm> pills;

    private ArrayList<String> extract_colors = new ArrayList<>();
    private ProgressBar progressBar;

    private ArrayList<Pill> results;
    private String sel_path, des_json;
    private ArrayList<Bitmap> bitmaps;
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
    public static void open(Context context) {
        Intent intent = new Intent(context, ExtractDescriptorActivity.class);
        context.startActivity(intent);
    }
    public static void open(Context context, Uri imgUri) {
        Intent intent = new Intent(context, ExtractDescriptorActivity.class);
        intent.setData(imgUri);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract_descriptor);
        results = new ArrayList<>();
        sel_img = findViewById(R.id.sel_img);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        progressBar = findViewById(R.id.progress_bar);
//        progressBar.des_json = intent.getStringExtra("json");setVisibility(View.COLL);
        result_txt = findViewById(R.id.result_txt);
        folder_path_txt = findViewById(R.id.folder_path_txt);
        Button sel_btn = findViewById(R.id.sel_btn);
        color_info_txt = findViewById(R.id.color_info_txt);

        Button test_btn = findViewById(R.id.test_btn);
        Button folder_sel_btn = findViewById(R.id.folder_sel_btn);
        Button extract_descriptor_btn = findViewById(R.id.extract_descriptor_btn);

        Button compare_process_btn = findViewById(R.id.compare_process_btn);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        initRealm();
        adapter = new PillAdapter(getApplicationContext(), results);
        adapter.setClickListener(new PillAdapter.PillClickListener() {
            @Override
            public void onClick(Pill pillModel) {
                Log.d(TAG, pillModel.getDescriptor());
//                pillModel.setMatched(pillModel.getMatched()+1);
                DetailPillActivity.open(getApplicationContext(), GlobalApp.IMAGE_PATH+"/"+pillModel.getName(), pillModel.getDescriptor());

            }
        });
        recyclerView.setHasFixedSize(true);
        //  mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition();

//                if(firstVisibleItem == 0){
//                    // your code
//                    shadow_view.setVisibility(View.GONE);
//                }else{
//                    shadow_view.setVisibility(View.VISIBLE);
//                }
            }
        });

        recyclerView.setAdapter(adapter);

        sel_btn.setOnClickListener(this);
        folder_sel_btn.setOnClickListener(this);
        extract_descriptor_btn.setOnClickListener(this);
        compare_process_btn.setOnClickListener(this);


        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        initRealm();

        Uri imgUri = getIntent().getData();

        if(imgUri!=null){
            setComparedImage(imgUri);
        }
//        Uri imgUri = getIntent().getUri
    }

    private void initRealm(){
        realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
        pills = realm.where(PillRealm.class).findAllAsync().sort("matched", Sort.DESCENDING);


//        Log.d(TAG, "pill size : " + pills.size());
        pills.addChangeListener(realmChangeListener);
    }

    private final RealmChangeListener<RealmResults<PillRealm>> realmChangeListener  = pills -> {
        adapter.notifyDataSetChanged();
//        showStatus("Person was loaded, or written to. " + insertions + deletions + changes);
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pills.removeAllChangeListeners();
        realm.close();
    }
    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );

        cursor.moveToNext();

        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );

        cursor.close();


        return path;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE) {

            if(data!=null){
                String folderPath = data.getDataString();
                //TODO
                folder_path_txt.setText(folderPath);



                File directory = new File(folderPath);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: "+ files.length);
                for (int i = 0; i < files.length; i++)
                {
                    Log.d("Files", "FileName:" + files[i].getName());
                }
            }

        }else if (requestCode == PICK_PHOTO_REQUEST_CODE){



            if (resultCode == RESULT_OK) {


                Uri selectedImage = data.getData();
                setComparedImage(selectedImage);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void setComparedImage(Uri selectedImage){
        try {
            InputStream imageStream = getContentResolver().openInputStream(
                    selectedImage);


            Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);

            Bitmap resized = BitmapUtil.resizeBitmapImage(selectedImageBitmap, 150);
//            Bitmap fastblur = BitmapUtil.fastblur(resized, (float)1, 2);

            Bitmap focusBitmap = BitmapUtil.fixedSizeWithCenterCrop50x50(selectedImageBitmap);
            sel_img.setImageBitmap(resized);
            sel_img.invalidate();

            String path = selectedImage.getPath();

//            sel_path = getPathFromUri(selectedImage);

            getMostCommonColour(focusBitmap);

//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();


            new ExtractDescriptorTask(this, resized).execute();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void test(){
        String color = "빨강, 투명";
        int s_cound =  (int)realm.where(Drug.class).equalTo("p_no", 201207435)
                .and()
                .beginGroup()
                .equalTo("color_front", color)
                .or()
                .equalTo("color_back", color)
                .endGroup()
                .count();


        Log.d(TAG, "result count : " + s_cound);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sel_btn:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);

                break;

            case R.id.folder_sel_btn:
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("file/*");
//                startActivityForResult(intent, PICKFILE_REQUEST_CODE);

                if(permissionGranted()) {
                    DirectoryPickerDialog directoryPickerDialog = new DirectoryPickerDialog(this,
                            () -> Toast.makeText(getApplicationContext(), "Canceled!!", Toast.LENGTH_SHORT).show(),
                            files -> {

//                                Toast.makeText(getApplicationContext(), files[0].getPath(), Toast.LENGTH_SHORT).show();
                                String folder_path = files[0].getPath();
                                folder_path_txt.setText(folder_path);



//                                File directory = new File(folder_path);


//                                File[] _files = files[0].listFiles();
//                                Log.d("Files", "Size: " + files.length);
//                                for (int i = 0; i < files.length; i++) {
//                                    Log.d("Files", "FileName:" + files[i].getName());
//
//
//                                }
                            }


                    );
                    directoryPickerDialog.show();
                }
                else{
                    requestPermission();
                }
                break;
            case R.id.extract_descriptor_btn:

                String path = folder_path_txt.getText().toString();
                File directory = new File(folder_path_txt.getText().toString());
//                new ExtractDescriptorFromDirectoryTask(this, directory).execute();

                new ExtractDescriptorFromDirectoryTaskV2(this, path).execute();





//                count = 0;
//                total_count = 0;
//                bitmaps = new ArrayList<>();
//
//                String path = folder_path_txt.getText().toString();
//
//                Log.d(TAG, "path : " + path);
//                File directory = new File(folder_path_txt.getText().toString());
//                File[] files = directory.listFiles();
//                Log.d("Files", "Size: " + files.length);
//                pd = new ProgressDialog(this);
//                pd.setIndeterminate(true);
//                pd.setCancelable(true);
//                pd.setCanceledOnTouchOutside(false);
//                pd.setMessage("Processing...");
//                pd.show();
//                result_txt.setText("Processing...");

//                total_count = files.length;

//                for (int i = 0; i < files.length; i++) {
//                    Log.d("Files", "(" +(i+1) + "" + "/ " +files.length+" ) FileName:" + files[i].getName());
//
//
//
//                    lock.lock();
//
//                    String name = files[i].getName();
//
//                    String filePath = files[i].getPath();
//
//                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
////                    bitmaps.add(bitmap);
//                    new ExtractDescriptorTask(this,name, bitmap).execute();
//                    lock.unlock();
//
//
//                }


//                pd.dismiss();

                break;

            case R.id.compare_process_btn:
                if(des_json!=null){
                    results.clear();
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.VISIBLE);
                    new CompareTask(this, MatConvertor.DeserializeToMat(des_json), ColorUtils.getInstance().convertedColorListToIdx( extract_colors), results).execute();
                }

                break;
        }
    }

    private boolean permissionGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

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
        //matching_updated

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("matching_updated");
        intentFilter.addAction("matching_completed");
        intentFilter.addAction("extract_complete");
        intentFilter.addAction("extract_des_json_complete");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
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

    BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();



            if (action.equals("extract_complete")) {

//                count ++ ;
//
//                result_txt.setText("Processing... ( " + count + " / " + total_count + " )");
//                if(total_count <= count){
////                    pd.dismiss();
//                }



            }else if(action.equals("extract_des_json_complete")){


//                des_json = json
                des_json = intent.getStringExtra("json");
//                Log.d("extract_des_json_complete", json);
                DetailPillActivity.open(getApplicationContext(),sel_path, des_json);
            }else if(action.equals("matching_completed")){


//                ArrayList<Pill> pills = intent.getParcelableArrayListExtra("results");
//                if(results.size()>0){
//                        results.clear();
//                }
//                results.addAll(pills);
//
//                Log.d(TAG, "results : " + results.size());

                Collections.sort(results, new Comparator<Pill>() {
                    @Override
                    public int compare(Pill o1, Pill o2) {
                        if(o1.getMatched() < o2.getMatched()){
                            return 1;
                        }else if(o1.getMatched()> o2.getMatched()){
                            return -1;
                        }

                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }else if(action.equals("matching_updated")){


                Collections.sort(results, new Comparator<Pill>() {
                    @Override
                    public int compare(Pill o1, Pill o2) {
                        if(o1.getMatched() < o2.getMatched()){
                            return 1;
                        }else if(o1.getMatched()> o2.getMatched()){
                            return -1;
                        }

                        return 0;
                    }
                });
                adapter.notifyDataSetChanged();
            }
        }
    };
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

//            closest_color_view.setBackgroundColor(closestColorInt);

//            closest_color_txt.setText("Closest Named Color: " + ColorUtils.getInstance().getNearestColorHexFromRgb(rgb[0], rgb[1], rgb [2])) ;
//            result_color_txt.setText(colorName + "\n"+ Colors_kor.toString());


            color_info_txt.setText("Colors : "+ Colors_kor.toString() + "\n"+ ColorUtils.getInstance().convertedColorListToIdx(Colors_kor).toString());


            extract_colors.clear();
            extract_colors.addAll(Colors_kor);

            //Converting RGB color value to hex string
//            colourTemp= "#" + ColorUtils.To00Hex(rgb[0]) + ColorUtils.To00Hex(rgb[1]) +  ColorUtils.To00Hex(rgb[2]);
//            int colorInt = Color.parseColor(colourTemp);
//            dominant_color_view.setBackgroundColor(colorInt);

//            return To00Hex(rgb[0]) + To00Hex(rgb[1])  + To00Hex(rgb[2]);
            return colorName;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }


}
