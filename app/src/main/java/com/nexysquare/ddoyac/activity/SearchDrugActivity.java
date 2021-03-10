package com.nexysquare.ddoyac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nexysquare.ddoyac.Constants;
import com.nexysquare.ddoyac.adapter.DrugAdapter;
import com.nexysquare.ddoyac.adapter.DrugAdapterV2;
import com.nexysquare.ddoyac.core.CompareTask;
import com.nexysquare.ddoyac.core.CompareTaskV2;
import com.nexysquare.ddoyac.core.ExtractDescriptorTask;
import com.nexysquare.ddoyac.model.Contraindicant;
import com.nexysquare.ddoyac.model.DataHelper;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.model.FilterBundle;
import com.nexysquare.ddoyac.model.MatchedInfo;
import com.nexysquare.ddoyac.model.Pill;
import com.nexysquare.ddoyac.model.PillRealm;
import com.nexysquare.ddoyac.util.BitmapUtil;
import com.nexysquare.ddoyac.util.ChipHelper;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.util.ColorUtils;
import com.nexysquare.ddoyac.util.MatConvertor;
import com.nexysquare.ddoyac.util.Utils;
import com.yalantis.ucrop.UCrop;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SearchDrugActivity extends AppCompatActivity {
    private final String TAG = "SearchDrugActivity";

    private final int REQUEST_FILTER = 1001;
    private Realm realm;
    private RecyclerView recyclerView;
    private DrugAdapterV2 adapter;
    private EditText search_edittext;

    private ChipGroup filter_group;

    private ArrayList<DrugParcelable> items = new ArrayList<>();

    private RealmResults<Drug> realmResults;

    private String keyword = "", mark_img = "", des_json;

    private ProgressBar progressBar;
    private Bitmap comparedBitmap;


    ArrayList<String> filter_colors = new ArrayList<>();
    ArrayList<String> filter_shapes = new ArrayList<>();


    private static final int PICK_PHOTO_REQUEST_CODE = 101;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    protected Handler imgSearchHandler;

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }



    public static void open(Context context) {
        Intent intent = new Intent(context, SearchDrugActivity.class);
        context.startActivity(intent);
    }

    public static void open(Context context, String mark, ArrayList<String> colors) {

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
//                뷰1, "호칭1");

        Intent intent = new Intent(context, SearchDrugActivity.class);

        intent.putExtra("colors", colors);
        intent.putExtra("mark", mark);
        context.startActivity(intent);
    }
    public static void open(Activity activity, View v) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchDrugActivity.class);

        activity.startActivity(intent, options.toBundle());
    }


    public static void open(Activity activity, View v, String mark, ArrayList<String> colors) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchDrugActivity.class);

        intent.putExtra("colors", colors);
        intent.putExtra("mark", mark);
        activity.startActivity(intent, options.toBundle());
    }

    public static void open(Activity activity, View v, String shape,  String mark, ArrayList<String> colors) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchDrugActivity.class);

        intent.putExtra("colors", colors);
        intent.putExtra("shape", shape);
        intent.putExtra("mark", mark);
        activity.startActivity(intent, options.toBundle());
    }


    public static void open(Activity activity, View v, String shape, ArrayList<String> colors, Bitmap bitmap) {

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "search_edit_view");
        Intent intent = new Intent(activity, SearchDrugActivity.class);

        intent.putExtra("colors", colors);
        intent.putExtra("shape", shape);
        intent.putExtra("bitmap", bitmap);
        activity.startActivity(intent, options.toBundle());
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        supportFinishAfterTransition();
    }

    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_drug);
        progressBar = findViewById(R.id.progress_bar);
        imgSearchHandler = new Handler(getMainLooper());
        search_edittext = findViewById(R.id.search_edittext);
        filter_group = findViewById(R.id.filter_group);
        ImageButton back_btn = findViewById(R.id.back_btn);


        ImageButton filter_btn = findViewById(R.id.filter_btn);

        realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
        realmResults = realm.where(Drug.class).findAllAsync();

//        RealmResults<Drugs> items = realm.where(Drugs.class)
//                .contains("p_name", keyword, Case.INSENSITIVE)
//                .or()
//                .contains("mark_front", keyword, Case.INSENSITIVE)
//                .or()
//                .contains("mark_back", keyword, Case.INSENSITIVE)
//                .findAllAsync();
//        realm = Realm.getDefaultInstance();


        recyclerView = findViewById(R.id.recycler_view);
        setUpRecyclerView();


        if(items.size()>0)
            items.clear();





        String shape = getIntent().getStringExtra("shape");
        String mark = getIntent().getStringExtra("mark");
        ArrayList<String> colors = getIntent().getStringArrayListExtra("colors");


        comparedBitmap = getIntent().getParcelableExtra("bitmap");

//        Log.d("Extract colors", colors.toString());

        if(comparedBitmap!=null){

            ChipHelper.addChipViewCloseableWithImage(SearchDrugActivity.this, filter_group, -1, comparedBitmap, filterWithImageListener, filterWithImageCloseListener);

        }

        if(colors!=null && colors.size()>0
                || shape!=null && !shape.equals("")
        ){


            if(mark!=null){
                mark = mark.replace("\n", " ");
                keyword = mark;
                search_edittext.setText(mark);
            }







            ArrayList<String> shapes = Constants.getShapes(shape);


            String[] colors_arr = Arrays.copyOf(colors.toArray(), colors.toArray().length, String[].class);

            Log.d("Extract colors", colors.toString());

            int idx_shape = 0;
            int idx_color = 100;

            for(String s : shapes){
                ChipHelper.addChipViewCloseable(SearchDrugActivity.this, filter_group, idx_shape++, s, filterListener,filterCloseListener);
            }

            for(String color : colors ){
                ChipHelper.addChipViewCloseable(SearchDrugActivity.this, filter_group, idx_color++, color, filterListener,filterCloseListener);
            }




//            RealmResults<Drug> results = realmResults.where()
//                    .beginGroup()
//                    .contains("mark_front", keyword, Case.INSENSITIVE)
//                    .or()
//                    .contains("mark_back", keyword, Case.INSENSITIVE)
//                    .endGroup()
//                    .and()
//                    .beginGroup()
//                    .in("color_front", colors_arr)
//                    .or()
//                    .in("color_back", colors_arr)
//                    .endGroup()
//                    .findAllAsync();
//            items.addAll(results);

//            search_edittext.keyboard

//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(search_edittext.getWindowToken(), 0);

            searchWithFilter();


        }else{

            initChip();

            if(mark!=null){
                mark = mark.replace("\n", " ");
                keyword = mark;
                search_edittext.setText(mark);
                searchWithFilter();
            }else{
                adapter.notifyDataSetChanged();
            }




//            addAllDrugs(realmResults);


//            items.addAll(realmResults);



//            shapeList();

        }







//        items.addChangeListener(new RealmChangeListener<RealmResults<Drugs>>() {
//            @Override
//            public void onChange(RealmResults<Drugs> drugs) {
//
//                adapter.notifyDataSetChanged();
//                Log.d("Search", "adapter.notifyDataSetChanged()");
//            }
//        });

        filter_btn.setOnClickListener(filterListener);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button extract_btn = findViewById(R.id.extract_btn);
        extract_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(des_json!=null){
                    progressBar.setVisibility(View.VISIBLE);


                    Log.d(TAG, "items size : " + items.size());

                    new CompareTaskV2(SearchDrugActivity.this, MatConvertor.DeserializeToMat(des_json),  items, keyword, filter_colors, filter_shapes).execute();
//                    if(imgSearchHandler.hasCallbacks(searchRunnable))

//                    imgSearchHandler.removeCallbacks(searchRunnable);
//
//                    imgSearchHandler.post(searchRunnable);


//                    Thread thread = new Thread(searchRunnable);
//                    thread.start();
//                    searchImageProcess( MatConvertor.DeserializeToMat(des_json));

                }else{
                    Log.e(TAG, "des_json is null");

                    if(comparedBitmap!=null){
                        Bitmap resized = BitmapUtil.resizeBitmapImage(comparedBitmap, 150);
                        try {
                            new ExtractDescriptorTask(SearchDrugActivity.this, comparedBitmap).execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

//                if(comparedBitmap!=null){
//                    Bitmap resized = BitmapUtil.resizeBitmapImage(comparedBitmap, 150);
//                    try {
//                        new ExtractDescriptorTask(SearchDrugActivity.this, comparedBitmap).execute();
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }

//                }
            }
        });

        Button pickup_img_btn = findViewById(R.id.pickup_img_btn);
        pickup_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST_CODE);

            }
        });

        Button read_csv_btn = findViewById(R.id.read_csv_btn);

        read_csv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        readData();
                    }
                }).start();

            }
        });

        Button db_copy_btn = findViewById(R.id.db_copy_btn);
        db_copy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                realm.getPath()


                exportRealm();


            }
        });

        Button check_count_btn = findViewById(R.id.check_count_btn);
        check_count_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = realm.where(Contraindicant.class).findAll().size();

                Toast.makeText(getApplicationContext(), "size : " + size, Toast.LENGTH_LONG).show();
            }
        });

        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    callSearchListener();
                    Utils.hideInputMethod(v);
                    return true;
                }
                return false;
            }
        });
        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();


                if(input.length()>1){

//                    keyword = input;
                    searchWithFilter();
//                    RealmResults<Drug> results = realmResults.where()
//                        .contains("p_name", keyword, Case.INSENSITIVE)
//                        .or()
//                        .contains("mark_front", keyword, Case.INSENSITIVE)
//                        .or()
//                        .contains("mark_back", keyword, Case.INSENSITIVE)
//                        .findAllAsync();
//
//
//                    if(items.size()>0){
//                        items.clear();
//
//                    }
//
//                    items.addAll(results);
//
//                    adapter.notifyDataSetChanged();

                }

            }
        });

        search_edittext.setFocusableInTouchMode(false);
        search_edittext.setFocusable(false);
        search_edittext.setFocusableInTouchMode(true);
        search_edittext.setFocusable(true);

        Utils.hideInputMethod(search_edittext);
        search_edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.showInputMethod(v);
                } else {
                    Utils.hideInputMethod(v);
                }
            }
        });
        search_edittext.clearFocus();

//        exportRealm();
    }


    private void shapeList(){


        ArrayList<String> ss = new ArrayList<>();

        for (DrugParcelable drug : items){
            String shape = drug.getShape();

            if(!ss.contains(shape)){
                ss.add(shape);
            }
        }


        Log.d("Shape","Shape size : "+ ss.size());

        for( String s : ss){
            Log.d("Shape", s);
        }

        //원형, 장방형, 타원형, 사각형, 기타, 팔각형, 오각형, 삼각형, 마름모형, 육각형, 반원형

        //원형

        // 장방형

        //타원형

        //사각형, 기타, 팔각형, 오각형, 삼각형, 마름모형, 육각형, 반원형
    }


    private void setUpRecyclerView() {

//        Log.d("SearchDrug", "items.size: "+items.size());
        adapter = new DrugAdapterV2(getApplicationContext(), items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
//        touchHelper.attachToRecyclerView(recyclerView);

        adapter.setClickListener(new DrugAdapterV2.onClickListener() {
            @Override
            public void onClick(DrugParcelable drug, ImageView imageView) {

                DrugDetailV2Activity.open(SearchDrugActivity.this, imageView, drug.getP_no());

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        items.clear();
        realmResults = null;
        recyclerView.setAdapter(null);
        realm.close();
    }


    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    View.OnClickListener filterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {



            SearchFilterActivity.open(SearchDrugActivity.this, search_edittext, search_edittext.getText().toString(), filter_shapes, filter_colors, mark_img, REQUEST_FILTER);
        }
    };

    View.OnClickListener filterCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //refresh
            filter_group.removeView(v);


            Log.d("Chips", "filter_group.getChildCount() " +filter_group.getChildCount());

            if(filter_group.getChildCount()==0){
                initChip();
            }
            searchWithFilter();
        }
    };


    View.OnClickListener filterWithImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener filterWithImageCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //refresh
            filter_group.removeView(v);

            comparedBitmap = null;

            Log.d("Chips", "filter_group.getChildCount() " +filter_group.getChildCount());

            if(filter_group.getChildCount()==0){
                initChip();
            }



            searchWithFilter();
        }
    };


    private void resetChip(){

        ArrayList<Integer> ids = new ArrayList<>();
        int totalChildCount = filter_group.getChildCount();
        for(int i =0 ; i < totalChildCount; i++){
            View currentChild = filter_group.getChildAt(filter_group.getChildCount() - 1);
//            Chip chip = (Chip)filter_group.getChildAt(i);
//            chip.getId()
//            filter_group.removeViewAt(i);
//            ids.add(i);
            filter_group.removeView(currentChild);
        }

//        for(int i : ids){
//            filter_group.removeViewAt(i);
//        }
//
//        filter_group.no
//        for(int id : filter_group.getCheckedChipIds()){
//            Log.d("Chip", "id : " + id);
//            filter_group.removeViewAt(id);
//        }


    }

    private void initChip(){
        ChipHelper.addActionChipView(this, filter_group, 10000, "모양", filterListener);
        ChipHelper.addActionChipView(this, filter_group, 10001, "색상", filterListener);
        ChipHelper.addActionChipView(this, filter_group, 10002, "마크", filterListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_FILTER && resultCode == RESULT_OK){

            //모양 0~99
            //색상 100~999
            //마크 1000~
//            String result_json = data.getStringExtra("result_json");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    resetChip();


                    FilterBundle filterBundle = (FilterBundle)data.getParcelableExtra("filter");


                    int idx_shape = 0;
                    int idx_color = 100;
                    int idx_mark = 1000;


                    String mark_img = filterBundle.getMark_img();


                    if(comparedBitmap!=null){

                        ChipHelper.addChipViewCloseableWithImage(SearchDrugActivity.this, filter_group, -1, comparedBitmap, filterWithImageListener, filterWithImageCloseListener);

                    }


                    for(String shape : filterBundle.getShapes() ){
                        ChipHelper.addChipViewCloseable(SearchDrugActivity.this, filter_group, idx_shape++, shape, filterListener,filterCloseListener);
                    }

                    for(String color : filterBundle.getColors() ){
                        ChipHelper.addChipViewCloseable(SearchDrugActivity.this, filter_group, idx_color++, color, filterListener,filterCloseListener);
                    }


                    if(mark_img!=null && !mark_img.equals("")){

                        ChipHelper.addChipViewCloseableWithIcon(SearchDrugActivity.this, filter_group, idx_mark, mark_img, filterListener, filterCloseListener);

                    }




                    keyword = filterBundle.getKeyword();
                    search_edittext.setText(keyword);

                    searchWithFilter();
                }
            }, 200);


        }


        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            } else if (requestCode == PICK_PHOTO_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                setComparedImage(selectedImage);
            }
        }

        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    private void setComparedImage(Uri selectedImage){
        try {
            InputStream imageStream = getContentResolver().openInputStream(
                    selectedImage);


            Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);

            Bitmap resized = BitmapUtil.resizeBitmapImage(selectedImageBitmap, 150);
//            Bitmap fastblur = BitmapUtil.fastblur(resized, (float)1, 2);

            Bitmap focusBitmap = BitmapUtil.fixedSizeWithCenterCrop50x50(selectedImageBitmap);
//            sel_img.setImageBitmap(resized);
//            sel_img.invalidate();

            String path = selectedImage.getPath();

//            sel_path = getPathFromUri(selectedImage);

//            getMostCommonColour(focusBitmap);

//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();


            new ExtractDescriptorTask(this, resized).execute();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
//            ExtractDescriptorActivity.open(getApplicationContext(), resultUri);

//            resultUri
//            Bitmap bitmap = null;
            try {
//                comparedBitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);


                InputStream imageStream = getContentResolver().openInputStream(
                        resultUri);


                Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap resized = BitmapUtil.resizeBitmapImage(selectedImageBitmap, 150);

                new ExtractDescriptorTask(this, resized).execute();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


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

    private void searchWithFilter(){

        filter_colors.clear();
        filter_shapes.clear();

        mark_img = "";

//        for(int id : filter_group.getCheckedChipIds()){
//            if(id >= 1000){
//                //mark
//
//            }else if(id >= 100 && id < 1000){
//                //color
//            }else{
//                //shape
//            }
//        }

        keyword = search_edittext.getText().toString();


        int totalChildCount = filter_group.getChildCount();
        for(int i =0 ; i < totalChildCount; i++){


            Chip chip = (Chip)filter_group.getChildAt(i);
            int id = chip.getId();

            String text = chip.getText().toString();
            if(id == 1000){
                //mark
                mark_img = chip.getTag().toString();


            }else if(id >= 100 && id < 1000){
                //color
                filter_colors.add(text);
            }else if(id >= 0 && id < 100){
                //shape
                filter_shapes.add(text);
            }
        }

        realmResults = realm.where(Drug.class).findAllAsync();
        RealmQuery<Drug> query = realmResults.where();

        String str_query = "";
//        Log.d("mark", mark );

        if(mark_img!=null && !mark_img.equals("")){
            keyword = "마크";

            query
                    .beginGroup()
                    .equalTo("mark_img_front", mark_img)
                    .or()
                    .equalTo("mark_img_back", mark_img)
                    .endGroup();



            str_query+= ".beginGroup()\n" +
                    "                        .equalTo(\"mark_img_front\", "+mark_img+")\n" +
                    "                        .or()\n" +
                    "                        .equalTo(\"mark_img_back\", "+mark_img+")\n" +
                    "                        .endGroup()";
        }

        if(keyword!=null && !keyword.equals("")){
            if(mark_img!=null && !mark_img.equals("")) {
                query.and();
                str_query+= ".and()";
            }

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
//            query
//                    .beginGroup()
//                    .in("color_front", colors_arr)
//                    .or()
//                    .in("color_back", colors_arr)
//                    .endGroup();


            query
                    .beginGroup();

//            for(String color : filter_colors){
////                query.contains("color_front", )
//            }

            for(int i = 0; i < filter_colors.size() ; i++){
                String color = filter_colors.get(i);
                query.contains("color_front", color)
                        .or()
                        .contains("color_back", color);

                if(i + 1 != filter_colors.size()){
                    query.or();
                }
            }

            query
                    .endGroup();


            str_query+= ".beginGroup()\n" +
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
                .findAllAsync();

        str_query+= ".findAllAsync()";

        if(items.size()>0){
            items.clear();
        }

        realmResults = results;

//        items.addAll(results);
//        addAllDrugs(results);
        if(comparedBitmap!=null){
            Bitmap resized = BitmapUtil.resizeBitmapImage(comparedBitmap, 150);

            new ExtractDescriptorTask(this, resized).execute();
        }else{
            addAllDrugs(realmResults);



            adapter.notifyDataSetChanged();
        }





        Log.d(TAG,"items size : "+ items.size());
        Log.d("Query", str_query);
//        if(comparedBitmap!=null){
////                progressBar.setVisibility(View.VISIBLE);
//
//
//            Uri imgUri = getImageUri(getApplicationContext(), comparedBitmap);
//            startCrop(imgUri);
//        }

    }


    private void exportRealm(){
        Log.d("REALM",realm.getPath());


//        File src = new File(realm.getPath());
//
//        File dst = new File(Environment
//                .getExternalStorageDirectory()
//                .getAbsolutePath()
//                + "/opencv4test/realm_210302_1.realm");
//
//        try {
//            copy(src, dst);
//        } catch (IOException e) {
//            e.printStackTrace();
//
//
//        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


//    private void readData() {
//        InputStream is = getResources().openRawResource(R.raw.contraindicant);
//        BufferedReader reader = new BufferedReader(
//                new InputStreamReader(is, Charset.forName("euc-kr")));
//        String line = "";
//        Log.d("csv", "start read");
//        int tdx = 0;
//
//        try {
//            while ((line = reader.readLine()) != null) {
//                // Split the line into different tokens (using the comma as a separator).
//                String[] tokens = line.split(",");
//
////                Log.d("csv", line);
//
//                if(tdx==0){
//                    tdx++;
//                    continue;
//                }
//
//
//
//
//                String type = tokens[0];
//                String A_ingreCode = tokens[1];
//                String A_productCode = tokens[2];
//                String A_productName = tokens[3];
//                String A_entpName = tokens[4];
//                String A_pay = tokens[5];
//
//                String B_ingreCode = tokens[6];
//                String B_productCode = tokens[7];
//                String B_productName = tokens[8];
//                String B_entpName = tokens[9];
//                String B_pay = tokens[10];
//
//
//
//                String created_no = tokens[11];
//                String created_date = tokens[12];
//
//                String des = tokens[13];
//
//                Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
//                realm.beginTransaction();
//                Contraindicant contraindicant = realm.createObject(Contraindicant.class);
////                Contraindicant contraindicant = new Contraindicant();
//                contraindicant.setConType(type);
//                contraindicant.setA_ingreCode(A_ingreCode);
//                contraindicant.setA_productCode(A_productCode);
//                contraindicant.setA_productName(A_productName);
//                contraindicant.setA_entpName(A_entpName);
//                contraindicant.setA_pay(A_pay);
//
//
//                contraindicant.setB_ingreCode(B_ingreCode);
//                contraindicant.setB_productCode(B_productCode);
//                contraindicant.setB_productName(B_productName);
//                contraindicant.setB_entpName(B_entpName);
//                contraindicant.setB_pay(B_pay);
//
//                contraindicant.setCreated_no(created_no);
//                contraindicant.setCreated_date(created_date);
//                contraindicant.setDes(des);
//
//                realm.commitTransaction();
//                realm.close();
////                contraindicants.add(contraindicant);
//                Log.d("csv", " tdx : "+ tdx);
//
//
//
//            }
//
//            Log.d("csv", "done read");
//        } catch (IOException e1) {
//            Log.e("MainActivity", "Error" + line, e1);
//            e1.printStackTrace();
//        }finally {
//
//        }
//
//        Log.d("csv", " tdx : "+ tdx);
//
//
//
//
//    }


    private void addAllDrugs(RealmResults<Drug> realmResults){

        if(items.size()>0) items.clear();

        for(Drug drug : realmResults){
            items.add(new DrugParcelable(drug));
        }
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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("matching_updated");
        intentFilter.addAction("matching_completed");
        intentFilter.addAction("extract_complete");
        intentFilter.addAction("extract_des_json_complete");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, intentFilter);

        Log.d(TAG, "items.size() : " +  items.size());
        adapter.notifyDataSetChanged();
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




            if (action.equals("matching_completed")){
                Log.d(TAG, "matching_completed");

//                ArrayList<Pill> pills = intent.getParcelableArrayListExtra("results");
//                if(results.size()>0){
//                        results.clear();
//                }
//                results.addAll(pills);
//
//                Log.d(TAG, "results : " + results.size());


//                for(Drug drug: items){
//                    Log.d(TAG,drug.getP_name() + " "+ drug.getMaxMatchedCount());
//                }
                ArrayList<MatchedInfo> matchedInfos = intent.getParcelableArrayListExtra("matched_arr");

                if(matchedInfos!=null){
                    Log.d(TAG, "matchedInfos.size() " + matchedInfos.size());


//                    for( MatchedInfo matchedInfo : matchedInfos){
//                        int id = matchedInfo.getId();
//                        int front = matchedInfo.getFront_matched();
//                        int back = matchedInfo.getBack_matched();
//                        int maxMatched = matchedInfo.getMaxMatched();
//
//
//                        for(int i = 0 ; i < items.size() ; i++){
//                            Drug drug=  items.get(i);
//
//                            if(drug.getId() == id){
//
//                                if(drug.getMaxMatchedCount()>0)
//                                    break;
//
//                                drug.setMatched_count_front(front);
//                                drug.setMatched_count_back(back);
//                                break;
//                            }
//                        }
//
//                    }
                }

                Collections.sort(items, new Comparator<DrugParcelable>() {
                    @Override
                    public int compare(DrugParcelable o1, DrugParcelable o2) {
                        if(o1.getMaxMatchedCount() < o2.getMaxMatchedCount()){
                            return 1;
                        }else if(o1.getMaxMatchedCount()> o2.getMaxMatchedCount()){
                            return -1;
                        }

                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }else if(action.equals("matching_updated")){

                Log.d(TAG, "matching_updated");



//                ArrayList<MatchedInfo> matchedInfos = intent.getParcelableArrayListExtra("matched_arr");
//
//                if(matchedInfos!=null){
//                    Log.d(TAG, "matchedInfos.size() " + matchedInfos.size());
//
//
//                    for( MatchedInfo matchedInfo : matchedInfos){
//                        int id = matchedInfo.getId();
//                        int front = matchedInfo.getFront_matched();
//                        int back = matchedInfo.getBack_matched();
//                        int maxMatched = matchedInfo.getMaxMatched();
//
//
//                        for(int i = 0 ; i < items.size() ; i++){
//                            Drug drug=  items.get(i);
//
//                            if(drug.getId() == id){
//
//                                if(drug.getMaxMatchedCount()>0)
//                                    break;
//
//                                drug.setMatched_count_front(front);
//                                drug.setMatched_count_back(back);
//                                break;
//                            }
//                        }
//
//                    }
//                }



                try{
                    Collections.sort(items, new Comparator<DrugParcelable>() {
                        @Override
                        public int compare(DrugParcelable o1, DrugParcelable o2) {
                            if(o1.getMaxMatchedCount() < o2.getMaxMatchedCount()){
                                return 1;
                            }else if(o1.getMaxMatchedCount()> o2.getMaxMatchedCount()){
                                return -1;
                            }

                            return 0;
                        }
                    });
                    adapter.notifyDataSetChanged();


                }catch (Exception ex){
                    Log.e(TAG, ex.getMessage());
                }





            }else if(action.equals("extract_des_json_complete")){
                des_json = intent.getStringExtra("json");

                Log.d(TAG, "extract_des_json_complete");
//                Log.d(TAG, des_json);

                if(des_json!=null){

                    progressBar.setVisibility(View.VISIBLE);

//                    Log.d(TAG, "filter_colors : "+ filter_colors.size());
//                    Log.d(TAG, "filter_shapes : "+ filter_shapes.size());


                    new CompareTaskV2(SearchDrugActivity.this, MatConvertor.DeserializeToMat(des_json), items, keyword, filter_colors, filter_shapes).execute();
//                    imgSearchHandler.removeCallbacks(searchRunnable);
//
//                    imgSearchHandler.post(searchRunnable);
//                    Thread thread = new Thread(searchRunnable);
//                    thread.start();

                }else{
                    Log.e(TAG, "des_json is null");


                }
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

//        imgSearchHandler.removeCallbacks(searchRunnable);

        Log.d(TAG, "onStop");


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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "temp", null);
        return Uri.parse(path);
    }


    Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if(des_json!=null){
//                searchImageProcess(MatConvertor.DeserializeToMat(des_json));
            }

        }
    };



//    private void searchImageProcess(Mat descriptor){
//        long startTime, endTime;
//        int min_dist = 40;
//
//        startTime = System.currentTimeMillis();
//
//        DescriptorMatcher matcher = DescriptorMatcher
//                .create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//
//        MatOfDMatch matches = new MatOfDMatch();
//        MatOfDMatch matches_final_mat;
//
//
//
//        int num = 1;
//
//
//
//        final int total = items.size();
//
//        for (int i = 0 ; i <  items.size() ; i++ ) {
//
//            Log.d(TAG, "process (" + i + "/" + items.size() + ")");
//
//            Drug drug = items.get(i);
//
//
//            String descriptor_front = drug.getDescriptor_front();
//            String descriptor_back = drug.getDescriptor_back();
//
//            if (descriptor_front != null && !descriptor_front.equals("{}") && !descriptor_front.equals("") && !descriptor_front.equals("{\"bytes\":[],\"cols\":0,\"rows\":0,\"type\":0}")) {
//
//                Mat des = MatConvertor.DeserializeToMat(descriptor_front);
//
//                matcher.match(descriptor, des, matches);
//
//                List<DMatch> matchesList = matches.toList();
//
//
//                matchesList = matchesList.stream().filter(t -> t.distance <= min_dist).collect(Collectors.toList());
//
//
//                int matchesCount = matchesList.size();
//
//
//                if (matchesCount > 1) {
//                    drug.setMatched_count_front(matchesCount);
//                }
//            }
//
//
//            if (descriptor_back != null && !descriptor_back.equals("{}") && !descriptor_back.equals("") && !descriptor_back.equals("{\"bytes\":[],\"cols\":0,\"rows\":0,\"type\":0}")) {
//
//                Mat des = MatConvertor.DeserializeToMat(descriptor_back);
//
//                matcher.match(descriptor, des, matches);
//
//                List<DMatch> matchesList = matches.toList();
//
//
//                matchesList = matchesList.stream().filter(t -> t.distance <= min_dist).collect(Collectors.toList());
//
//                int matchesCount = matchesList.size();
//
//
//                if (matchesCount > 1) {
//                    drug.setMatched_count_back(matchesCount);
//                }
//
//            }
//
//
//            if (i % 100 == 0) {
//
//                Log.d(TAG, "needed update");
//                //notify data
//
////                            int finalNum = num - 1;
////                            final int percentage = (finalNum /total) * 100;
////                            Log.d("progress", percentage + "%  ("+finalNum + "/"+total+")" );
//
////                runOnUiThread(new Runnable() {
////                    public void run() {
////                        final Intent intent = new Intent("matching_updated");
//////                                    intent.putExtra("progress", percentage);
////                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
////                    }
////                });
//
//
//            }
//        }
//
//        endTime = System.currentTimeMillis();
//        Log.d("result", "Time taken="+ (endTime - startTime) + "ms");
//
//        final Intent intent = new Intent("matching_completed");
//        //intent.putExtra("results", results);
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//    }
}
