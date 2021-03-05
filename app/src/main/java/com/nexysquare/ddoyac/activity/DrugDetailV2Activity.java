package com.nexysquare.ddoyac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.gson.JsonObject;
import com.nexysquare.ddoyac.fragment.DetailFragment;
import com.nexysquare.ddoyac.fragment.ReferenceFragment;
import com.nexysquare.ddoyac.model.Migration;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.util.JsonHelper;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DrugDetailV2Activity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;

//    private ImageView mProfileImage;
    private int mMaxScrollSize;
    private ViewPager viewPager;
    private String p_name;
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

                if(p_name!=null){
                    String name= p_name;
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("drug_name", name);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(getApplicationContext(), getString(R.string.clipboard_p_name), Toast.LENGTH_SHORT).show();
                }

            }
        });
        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        int p_no = getIntent().getIntExtra("p_no", -1);
        if(p_no >= 0){
            RealmConfiguration config0 = new RealmConfiguration.Builder()
                    .assetFile("drugs.realm")
                    .migration(new Migration())
                    .schemaVersion(1)
                    .build();

            // You can then manually call Realm.migrateRealm().
//        try {
//            Realm.migrateRealm(config0, new Migration());
//        } catch (FileNotFoundException ignored) {
//            // If the Realm file doesn't exist, just ignore.
//        }
            Realm realm = Realm.getInstance(config0);
            Drug drug = realm.where(Drug.class).equalTo("p_no", p_no).findFirst();

            String name = drug.getP_name();

            p_name = name;
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
}
