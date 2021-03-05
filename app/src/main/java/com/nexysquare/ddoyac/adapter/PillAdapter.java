package com.nexysquare.ddoyac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.Pill;

import java.util.ArrayList;

public class PillAdapter extends RecyclerView.Adapter<PillAdapter.ViewHolder>   {
    public interface PillClickListener {
        void onClick(Pill pillModel);
    }

    private final Context mContext;
    private final ArrayList<Pill> mItems;
    private int mCurrentItemId = 0;


    private PillClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView preview_img;

        public final TextView name_txt;
        public final TextView result_txt;



        private final View root_view;


        public ViewHolder(View view) {
            super(view);
            preview_img = view.findViewById(R.id.preview_img);

            name_txt = view.findViewById(R.id.name_txt);
            result_txt = view.findViewById(R.id.result_txt);

            root_view = view.findViewById(R.id.root_view);

        }
    }



    public PillAdapter(Context context, ArrayList<Pill> list) {

        mContext = context;
        mItems = list;
    }

    public void setClickListener(PillClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compared, parent, false);

        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Pill pill = mItems.get(position);



        holder.name_txt.setText(pill.getName());


        String result = "Matched : " + pill.getMatched();
        holder.result_txt.setText(result);

        String pill_path = GlobalApp.IMAGE_PATH +"/"+ pill.getName();
        Glide.with(mContext).load(pill_path).into(holder.preview_img);

//        holder.preview_img.setImageDrawable(EventUtil.getEventImg(mContext, eventModel.getType()));

        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null){
                    clickListener.onClick(pill);
                }
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
