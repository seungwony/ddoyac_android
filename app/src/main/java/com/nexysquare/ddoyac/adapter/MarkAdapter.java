package com.nexysquare.ddoyac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexysquare.ddoyac.R;

import java.util.ArrayList;

public class MarkAdapter  extends RecyclerView.Adapter<MarkAdapter.ViewHolder>   {
    public interface onClickListener {
        void onClick(String img_url);
    }

    private final Context mContext;
    private final ArrayList<String> mItems;
    private int mCurrentItemId = 0;


    private onClickListener clickListener;


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_view;


        View root_view;

        ViewHolder(View view) {
            super(view);
            img_view = view.findViewById(R.id.img_view);

            root_view = view.findViewById(R.id.root_view);
        }
    }


    public MarkAdapter(Context context, ArrayList<String> list) {

        mContext = context;
        mItems = list;
    }

    public void setClickListener(onClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public MarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mark, parent, false);

        return new MarkAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarkAdapter.ViewHolder holder, int position) {
        final String img = mItems.get(position);


        Glide.with(mContext)
                .load(img)
                .centerCrop()
                .into(holder.img_view);

        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null) clickListener.onClick(img);
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
