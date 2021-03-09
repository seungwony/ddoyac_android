package com.nexysquare.ddoyac.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.model.Migration;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Drug;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DrugDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private View mFab;
    private int mMaxScrollSize;
    private boolean mIsImageHidden;

    private ImageView imgView;
    private CollapsingToolbarLayout flexible_collapsing;
    public static void open(Context context, int p_no) {

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
//                뷰1, "호칭1");
        Intent intent = new Intent(context, DrugDetailActivity.class);

        intent.putExtra("p_no", p_no);
        context.startActivity(intent);
    }
    public static void open(Context context, Drug drug) {

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context,
//                뷰1, "호칭1");
        Intent intent = new Intent(context, DrugDetailActivity.class);

//        intent.putExtra("drug", drug);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);
        mFab = findViewById(R.id.fab);

        flexible_collapsing = findViewById(R.id.flexible_collapsing);

        imgView = findViewById(R.id.img_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

//        flexible_collapsing.setTitle();

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.flexible_example_appbar);
        appbar.addOnOffsetChangedListener(this);

        int p_no = getIntent().getIntExtra("p_no", -1);
        if(p_no >= 0){
//            RealmConfiguration config0 = new RealmConfiguration.Builder()
//                    .assetFile("drugs.realm")
//                    .migration(new Migration())
////                .name("drugs.realm")
//                    .schemaVersion(1)
//                    .build();

            // You can then manually call Realm.migrateRealm().
//        try {
//            Realm.migrateRealm(config0, new Migration());
//        } catch (FileNotFoundException ignored) {
//            // If the Realm file doesn't exist, just ignore.
//        }
            Realm realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
            Drug drug = realm.where(Drug.class).equalTo("p_no", p_no).findFirst();

            String name = drug.getP_name();
            String img = drug.getImg();
            realm.close();

            flexible_collapsing.setTitle(name);


            Glide.with(this).load(img).into(imgView);

        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100
                / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                ViewCompat.animate(mFab).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(mFab).scaleY(1).scaleX(1).start();
            }
        }
    }
}
