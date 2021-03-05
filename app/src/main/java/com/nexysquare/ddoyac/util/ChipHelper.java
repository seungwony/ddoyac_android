package com.nexysquare.ddoyac.util;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nexysquare.ddoyac.R;

public class ChipHelper {


     public static void addChipView(Activity activity, ChipGroup gp, int idx, String chipText) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
        chip.setText(chipText);
        chip.setId(idx);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }


    public static void addColorChipView(Activity activity, ChipGroup gp, int idx, String chipText, int res_id, int text_res_id) {

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view, gp, false);
//        chip.setChipIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lens));
//        chip.setChipIconTint(stateList);
        chip.setTextColor(ContextCompat.getColor(activity, text_res_id));
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(activity, res_id)));
        chip.setText(chipText);
        chip.setChipStrokeWidth(2);
        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.bright_gray)));
        DrawableCompat.setTint(chip.getCheckedIcon(), ContextCompat.getColor(activity, text_res_id));
//        chip.setCheckedIconTint(DrawableCompat.setTint(chip.getCheckedIcon(), ContextCompat.getColor(getApplicationContext(), text_res_id)));
        chip.setId(idx);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }


    public static void addChipViewCloseable(Activity activity, ChipGroup gp, int idx, String chipText, View.OnClickListener listener) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view_with_close_icon, gp, false);
        chip.setText(chipText);
        chip.setId(idx);
        chip.setOnCloseIconClickListener(listener);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }

    public static void addActionChipView(Activity activity, ChipGroup gp, int idx, String chipText, View.OnClickListener listener) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view_with_close_icon, gp, false);
        chip.setText(chipText);
        chip.setId(idx);
        chip.setTag(idx);
        chip.setOnClickListener(listener);
//        chip.setCloseIconVisible(View.GONE);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }

    public static void modifyChipView(Activity activity, ChipGroup gp, int idx, String chipText){
//         gp.chip
        for(int i=0; i<gp.getChildCount(); i++){
            if(gp.getChildAt(i) instanceof Chip){
                Chip cp = (Chip)gp.getChildAt(i);
                if(cp.getId()==idx){
                    cp.setText(chipText);
                }
            }
        }
    }

    public static void addChipViewCloseable(Activity activity, ChipGroup gp, int idx, String chipText, View.OnClickListener listener, View.OnClickListener listener_close) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view_with_close_icon, gp, false);
        chip.setText(chipText);
        chip.setId(idx);
        chip.setTag(idx);
        chip.setOnClickListener(listener);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(listener_close);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }

    public static void addChipViewCloseableWithIcon(Activity activity, ChipGroup gp, int idx, String iconURL, View.OnClickListener listener, View.OnClickListener listener_close) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view_with_close_icon, gp, false);
        chip.setText("마크");
        Glide.with(activity)
                .asBitmap()
                .load(iconURL)

                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                        Drawable d = new BitmapDrawable(activity.getResources(), resource);
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(activity.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        chip.setChipIcon(circularBitmapDrawable);
                        return false;
                    }
                }
               ).preload();
//        chip.setChipIcon(DrawableCompat.);

        chip.setId(idx);
        chip.setTag(iconURL);
        chip.setOnClickListener(listener);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(listener_close);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }


    public static void addChipViewCloseableWithImage(Activity activity, ChipGroup gp, int idx, Bitmap bitmap, View.OnClickListener listener, View.OnClickListener listener_close) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final Chip chip = (Chip) layoutInflater.inflate(R.layout.row_chip_view_with_close_icon, gp, false);
        chip.setText("사진");
        Glide.with(activity)
                .asBitmap()
                .load(bitmap)


                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                        Drawable d = new BitmapDrawable(activity.getResources(), resource);
                                  RoundedBitmapDrawable circularBitmapDrawable =
                                          RoundedBitmapDrawableFactory.create(activity.getResources(), resource);
                                  circularBitmapDrawable.setCircular(true);
                                  chip.setChipIcon(circularBitmapDrawable);
                                  return false;
                              }
                          }
                ).preload();
//        chip.setChipIcon(DrawableCompat.);

        chip.setId(idx);
        chip.setTag(bitmap);
        chip.setOnClickListener(listener);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(listener_close);
        //...

        // This is ChipGroup view
        gp.addView(chip);
    }
}
