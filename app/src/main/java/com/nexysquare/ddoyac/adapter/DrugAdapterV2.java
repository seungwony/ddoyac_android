package com.nexysquare.ddoyac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.util.SavedDatabaseHelper;

import java.util.ArrayList;

public class DrugAdapterV2  extends RecyclerView.Adapter<DrugAdapterV2.ViewHolder>   {
    public interface onClickListener {
        void onClick(DrugParcelable drug, ImageView imageView);
    }

    private final Context mContext;
    private final ArrayList<DrugParcelable> mItems;
    private int mCurrentItemId = 0;


    private onClickListener clickListener;
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView preview_img;

        TextView name_txt;
        TextView des_txt;
        View root_view;
        TextView matched_count_txt;

        ViewHolder(View view) {
            super(view);
            preview_img = view.findViewById(R.id.preview_img);
            name_txt = view.findViewById(R.id.name_txt);
            des_txt = view.findViewById(R.id.des_txt);
            root_view = view.findViewById(R.id.root_view);
            matched_count_txt= view.findViewById(R.id.matched_count_txt);
        }
    }


    public DrugAdapterV2(Context context, ArrayList<DrugParcelable> list) {

        mContext = context;
        mItems = list;


    }

    public void setClickListener(onClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drug, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DrugParcelable obj = mItems.get(position);

        String p_name =  obj.getP_name();

        String img =  obj.getImg();

        //noinspection ConstantConditions
        holder.name_txt.setText(p_name);
        holder.itemView.setTag(obj);


        String back_mark = obj.getMark_back().equals("") ? "" : " | " + obj.getMark_back();

        String mark  = "표시 : " + obj.getMark_front() + back_mark;
        holder.des_txt.setText(mark);


        if(obj.getMaxMatchedCount()>0){
            holder.matched_count_txt.setVisibility(View.VISIBLE);
            holder.matched_count_txt.setText("Matched : " + obj.getMaxMatchedCount());
        }else{
            holder.matched_count_txt.setVisibility(View.GONE);
        }
        Glide.with(mContext)
                .load(img)
                .centerCrop()
                .into(holder.preview_img);

        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null) clickListener.onClick(obj, holder.preview_img);
            }
        });
    }




    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
