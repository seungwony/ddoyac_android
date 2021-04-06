package com.nexysquare.ddoyac.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.model.SavedModel;
import com.nexysquare.ddoyac.util.SavedDatabaseHelper;

import java.util.ArrayList;

public class  SavedAdapter extends BaseAdapter {
    public interface onClickListener {
        void onClick(TextView tv, int id, String name);
    }

    public interface onLongClickListener {
        void onLongClick(int id);
    }
    private onLongClickListener longClickListener;
    private onClickListener clickListener;
    private Context context;
    ArrayList<SavedModel> items;
    private SavedDatabaseHelper dbHelper;
    public SavedAdapter(Context context, ArrayList<SavedModel> items){
        this.context = context;
        this.items = items;
        dbHelper = new SavedDatabaseHelper(context);
    }


    public void setOnClickListener(onClickListener clickListener){
        this.clickListener = clickListener;
    }


    public void setOnLongClickListener(onLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder userViewHolder;
        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.item_saved, parent, false);
            userViewHolder = new ViewHolder();

            userViewHolder.root_view = convertView.findViewById(R.id.root_view);
            userViewHolder.txtName = convertView.findViewById(R.id.name_txt);
            userViewHolder.count_txt = convertView.findViewById(R.id.count_txt);

            userViewHolder.root_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener!=null){
                        clickListener.onClick(userViewHolder.txtName, items.get(position).getId(), items.get(position).getName());
                    }

                }
            });
            userViewHolder.root_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(longClickListener!=null)
                        longClickListener.onLongClick(items.get(position).getId());
                    return true;
                }

            });



            convertView.setTag(userViewHolder);
        }
        else {
            userViewHolder = (ViewHolder) convertView.getTag();
        }


        int count = dbHelper.getRelDrugDataCount(items.get(position).getId());


        userViewHolder.bind(items.get(position).getName(), count);

        return convertView;
    }

    public static class ViewHolder {
        TextView txtName;

        TextView count_txt;
        View root_view;
        public void bind(String name, int count) {

            txtName.setText(name);


            if(count==0){
                count_txt.setVisibility(View.GONE);
            }else{
                count_txt.setVisibility(View.VISIBLE);
                count_txt.setText(String.valueOf(count));
            }



        }
    }
}
