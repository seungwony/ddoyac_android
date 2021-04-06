package com.nexysquare.ddoyac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.adapter.SavedAdapter;
import com.nexysquare.ddoyac.fragment.DetailFragment;
import com.nexysquare.ddoyac.fragment.ReferenceFragment;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.model.SavedModel;
import com.nexysquare.ddoyac.util.DateUtil;
import com.nexysquare.ddoyac.util.JsonHelper;
import com.nexysquare.ddoyac.util.SavedDatabaseHelper;
import com.nexysquare.ddoyac.util.Utils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;


import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;

public class DrugDetailV2Activity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "DrugDetailV2Activity";
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;

//    private ImageView mProfileImage;
    private int mMaxScrollSize;
    private ViewPager viewPager;
    private String p_name;
    private int p_no;
    private DialogPlus bottomDialog;
    private ArrayList<SavedModel> savedModels = new ArrayList<>();
    public static void open(Context context, int p_no) {

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
//                뷰1, "호칭1");
        Intent intent = new Intent(context, DrugDetailV2Activity.class);
        intent.putExtra("p_no", p_no);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();'
        viewPager.setVisibility(View.GONE);
        supportFinishAfterTransition();
    }

    public static void open(Activity activity, View v, int p_no) {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "img_view");
        Intent intent = new Intent(activity, DrugDetailV2Activity.class);
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
//                뷰1, "호칭1");

        intent.putExtra("p_no", p_no);
        activity.startActivity(intent, options.toBundle());
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail_v2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.materialup_tabs);
        viewPager  = (ViewPager) findViewById(R.id.materialup_viewpager);
        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.materialup_appbar);
        ImageView materialup_img = findViewById(R.id.materialup_img);
        TextView title_txt= findViewById(R.id.title_txt);
//        mProfileImage = (ImageView) findViewById(R.id.materialup_profile_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.materialup_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });


        ImageButton share_btn = findViewById(R.id.share_btn);
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(p_name!=null){
//                    String name= p_name;
//                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newPlainText("drug_name", name);
//                    clipboard.setPrimaryClip(clip);
//
//                    Toast.makeText(getApplicationContext(), getString(R.string.clipboard_p_name), Toast.LENGTH_SHORT).show();
//                }
                showShareDialog();

            }
        });


        ImageButton save_btn = findViewById(R.id.save_btn);

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSheetBottoms();
            }
        });

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        int p_no = getIntent().getIntExtra("p_no", -1);
        if(p_no >= 0){

            // You can then manually call Realm.migrateRealm().
//        try {
//            Realm.migrateRealm(config0, new Migration());
//        } catch (FileNotFoundException ignored) {
//            // If the Realm file doesn't exist, just ignore.
//        }
            Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
            Drug drug = realm.where(Drug.class).equalTo("p_no", p_no).findFirst();

            String name = drug.getP_name();

            this.p_no = p_no;
            this.p_name = name;
            String[] n_split = name.split("\\(");
            name = n_split[0];
            String img = drug.getImg();
            realm.close();

            title_txt.setText(name);


            Glide.with(this).load(img).into(materialup_img);


            JsonObject json = JsonHelper.drugsRealmToJson(drug);

//            JsonObject jsObj = new JsonObject();
//            jsObj.addProperty();

//            drug.

//            Gson gson = new Gson();
            String json_string = json.toString();
//            Log.d("DrugDetail", json_string);

            viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), json_string));

            tabLayout.setupWithViewPager(viewPager);

        }







    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

//            mProfileImage.animate()
//                    .scaleY(0).scaleX(0)
//                    .setDuration(200)
//                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

//            mProfileImage.animate()
//                    .scaleY(1).scaleX(1)
//                    .start();
        }
    }


    private static class TabsAdapter extends FragmentPagerAdapter {
        private static final int TAB_COUNT = 2;

        private String json_string;
        TabsAdapter(FragmentManager fm, String json_string) {

            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            this.json_string = json_string;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int i) {
            if(i==0){
                return DetailFragment.newInstance(json_string);
            }else{
                return ReferenceFragment.newInstance(json_string);
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {

            if(position == 0){
                return "정보";
            }else if(position==1){
                return "비슷한 알약";
            }else{
                return "";
            }


        }
    }



    private void showSheetBottoms(){
//        MaterialDialog materialDialog = new MaterialDialog(this, new BottomSheet(LayoutMode.WRAP_CONTENT));
//        materialDialog.show(()->{
//
//
//        });

        savedModels.clear();
        SavedDatabaseHelper dbHelper = new SavedDatabaseHelper(getApplicationContext());

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllData();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(SavedDatabaseHelper.COL_1));
                    int priority = cursor.getInt(cursor.getColumnIndex(SavedDatabaseHelper.COL_2));
                    String name = cursor.getString(cursor.getColumnIndex(SavedDatabaseHelper.COL_3));
                    String created = cursor.getString(cursor.getColumnIndex(SavedDatabaseHelper.COL_4));


                    SavedModel item = new SavedModel();
                item.setId(id);
                item.setPriority(priority);
                item.setName(name);

                savedModels.add(item);
//                item.setCreated(new Date());
                    Log.d(TAG, "id: " + id + ", name: " + name + ", priority: " + priority);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        SavedAdapter adapter = new SavedAdapter(this, savedModels);


        adapter.setOnClickListener(new SavedAdapter.onClickListener() {


            @Override
            public void onClick(TextView tv, int id, String name) {
                SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());

                int count = drugDBHelper.getRelDrugDataCount(id,p_no);
                if(count>0){
                    Toast.makeText(getApplicationContext(), "이미 저장된 정보입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int size = drugDBHelper.getAllDrugDataById(id).getCount();

                drugDBHelper.insertDrugData(id, p_no, size, DateUtil.convertedSimpleFormat(new Date()));


//                SavedModel item = new SavedModel();
//                item.setId(i);
//                item.setPriority(i);
//                item.setName("알약 #"+String.valueOf(i));
//                item.setCreated(new Date());
////                savedModels.add(item);
//                dbHelper.insertData(i, "알약 #"+String.valueOf(i), "");

                Toast.makeText(getApplicationContext(), "["+name+"]에 추가했습니다.", Toast.LENGTH_SHORT).show();

                if(bottomDialog!=null){
                    bottomDialog.dismiss();

                }
            }
        });

        adapter.setOnLongClickListener(new SavedAdapter.onLongClickListener() {
            @Override
            public void onLongClick(int id) {
                SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());
                drugDBHelper.deleteDrugData(id);
                Toast.makeText(getApplicationContext(), "저장 목록을 삭제했습니다.", Toast.LENGTH_SHORT).show();

                if(bottomDialog!=null){
                    bottomDialog.dismiss();

                }
            }
        });
//        materialDialog.show();

        bottomDialog = DialogPlus.newDialog(this)

                .setAdapter(adapter)

                .setHeader(R.layout.dialog_saved_header)

                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
                        bottomDialog = null;
                    }
                })
                .setContentBackgroundResource(R.drawable.upper_corner_background)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();


        TextView title_txt = bottomDialog.getHeaderView().findViewById(R.id.title_txt);

        Button add_btn = bottomDialog.getHeaderView().findViewById(R.id.add_item_btn);

        title_txt.setText("목록에 저장하기");

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                int i = savedModels.size();
//                SavedModel item = new SavedModel();
//                item.setId(i);
//                item.setPriority(i);
//                item.setName("알약 #"+String.valueOf(i));
//                item.setCreated(new Date());
////                savedModels.add(item);
//                dbHelper.insertData(i, "알약 #"+String.valueOf(i), "");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCreateSavedList();
                    }
                }, 500);


                bottomDialog.dismiss();
            }
        });

        bottomDialog.show();

    }


    private void showCreateSavedList(){
        DialogPlus dialog = DialogPlus.newDialog(this)

                .setHeader(R.layout.dialog_header_create_saved_list)
                .setFooter(R.layout.dialog_footer_create_saved_list)
                .setContentBackgroundResource(R.drawable.corner_background)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHolder(new ViewHolder(R.layout.dialog_create_saved_list))
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
//                        Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();

//                        Utils.hideInputMethod(dialog.getHolderView().findViewById(R.id.name_tf));

//                        Utils.hideKeyboard(getParent());
                        hideKeyboard();

                    }
                })
                .setCancelable(true)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();


        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(10),
                new InputFilter.AllCaps()
        };
        TextInputEditText name_tf = dialog.getHolderView().findViewById(R.id.name_tf);
        name_tf.setFilters(filters);
        Button add_item_btn = dialog.getFooterView().findViewById(R.id.add_item_btn);


        add_item_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name_tf.getText().length()>1){

                    String name = name_tf.getText().toString();


                    SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());

                    int priority = drugDBHelper.getAllData().getCount();
                    drugDBHelper.insertData(priority, name, DateUtil.convertedSimpleFormat(new Date()));
                    Toast.makeText(getApplicationContext(), "["+ name + "] 저장 목록이 만들어졌습니다.", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showSheetBottoms();
                        }
                    }, 800);

                }


            }
        });

        dialog.show();
    }


    private void showShareDialog(){
        DialogPlus dialog = DialogPlus.newDialog(this)


                .setContentBackgroundResource(R.drawable.upper_corner_background)

//                .setContentHeight(1100)
//                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentHolder(new ViewHolder(R.layout.dialog_share))

//                .setHeader(R.layout.dialog_share)
//                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setGravity(Gravity.BOTTOM)
//                .setExpanded(false)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogPlus dialog) {
//                        Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();

//                        Utils.hideInputMethod(dialog.getHolderView().findViewById(R.id.name_tf));

//                        Utils.hideKeyboard(getParent());
//                        hideKeyboard();

                    }
                })
                .setCancelable(true)
                .setExpanded(true, 1000)  // This will enable the expand feature, (similar to android L share dialog)
                .create();



//        TextInputEditText name_tf = dialog.getHolderView().findViewById(R.id.name_tf);
//        Button add_item_btn = dialog.getFooterView().findViewById(R.id.add_item_btn);



        TextView copy_content = dialog.getHolderView().findViewById(R.id.copy_content);

        copy_content.setText(getCopyContent());


        View clipboard_btn = dialog.getHolderView().findViewById(R.id.clipboard_btn);
        clipboard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                copyClipboard( getCopyContent());
            }
        });

        View kakaotalk_btn = dialog.getHolderView().findViewById(R.id.kakaotalk_btn);

        kakaotalk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareKakao(getCopyContent());
            }
        });

        View mail_btn = dialog.getHolderView().findViewById(R.id.mail_btn);
        mail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_email(getCopyContent());
            }
        });

        dialog.show();
    }
    private void send_email(String content){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("plain/Text");
//        email.putExtra(Intent.EXTRA_EMAIL, getString(R.string.email));
        email.putExtra(Intent.EXTRA_SUBJECT, "[" + getString(R.string.app_name) + "] 공유");
        email.putExtra(Intent.EXTRA_TEXT, content);
        email.setType("message/rfc822");

        try {
            startActivity(email);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "이메일 관련 어플이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }

    }
    private void copyClipboard(String content){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("drug_name", content);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getApplicationContext(), getString(R.string.clipboard_p_name), Toast.LENGTH_SHORT).show();
    }


    private void shareKakao(String content){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setPackage("com.kakao.talk");


        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "카카오톡 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private String getCopyContent(){

        String content = "";


        if(p_name!=null) {
            content = p_name;
        }

        return content;
    }

    private void hideKeyboard(){
        Utils.hideKeyboard(this);
    }
    private ArrayList<SavedModel> getItems(){
        ArrayList<SavedModel> items = new ArrayList<>();
        for(int i = 0 ; i < 100 ; i++){
            SavedModel item = new SavedModel();
            item.setId(i);
            item.setPriority(i);
            item.setName("알약 #"+String.valueOf(i));
            item.setCreated(new Date());

            items.add(item);
        }
        return items;
    }
}
