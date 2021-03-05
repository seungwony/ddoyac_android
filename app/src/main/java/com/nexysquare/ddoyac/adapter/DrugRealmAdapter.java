package com.nexysquare.ddoyac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.R;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;

public class DrugRealmAdapter extends RealmRecyclerViewAdapter<Drug, DrugRealmAdapter.MyViewHolder> {

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<>();
    private OrderedRealmCollection<Drug> data;
    private Context context;
    public DrugRealmAdapter(Context context, OrderedRealmCollection<Drug> data) {
        super(data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(true);

        this.data = data;

        this.context = context;
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drug, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Drug obj = data.get(position);

//        final int itemId = obj.getId();

        String p_name =  obj.getP_name();

        String img =  obj.getImg();

        //noinspection ConstantConditions
        holder.name_txt.setText(p_name);



        Glide.with(context)
                .load(img)
                .centerCrop()
                .into(holder.preview_img);

//        holder.deletedCheckBox.setChecked(countersToDelete.contains(itemId));
//        if (inDeletionMode) {
//            holder.deletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        countersToDelete.add(itemId);
//                    } else {
//                        countersToDelete.remove(itemId);
//                    }
//                }
//            });
//        } else {
//            holder.deletedCheckBox.setOnCheckedChangeListener(null);
//        }
//        holder.deletedCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getP_no();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView preview_img;

        TextView name_txt;

        MyViewHolder(View view) {
            super(view);
            preview_img = view.findViewById(R.id.preview_img);
            name_txt = view.findViewById(R.id.name_txt);
        }
    }
}
