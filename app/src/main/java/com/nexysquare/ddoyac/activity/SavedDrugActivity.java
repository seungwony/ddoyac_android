package com.nexysquare.ddoyac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.nexysquare.ddoyac.GlobalApp;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.adapter.DrugAdapterV2;
import com.nexysquare.ddoyac.model.DataHelper;
import com.nexysquare.ddoyac.model.Drug;
import com.nexysquare.ddoyac.model.DrugParcelable;
import com.nexysquare.ddoyac.util.DateUtil;
import com.nexysquare.ddoyac.util.SavedDatabaseHelper;
import com.nexysquare.ddoyac.util.Utils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SavedDrugActivity extends AppCompatActivity {
    private final String TAG = "SavedDrugActivity";
    private Realm realm;
    private RecyclerView recyclerView;
    private DrugAdapterV2 adapter;
    private RealmResults<Drug> realmResults;
    private ProgressBar progressBar;
    private ArrayList<DrugParcelable> items = new ArrayList<>();


    private TextView warning_msg;
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
//            DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
//            items.get()
//            items.remove()
            final DrugParcelable obj = (DrugParcelable)viewHolder.itemView.getTag();
            Toast.makeText(getApplicationContext(), "["+obj.getP_name() + "] 목록에서 삭제했습니다.", Toast.LENGTH_SHORT).show();

            SavedDatabaseHelper dbHelper = new SavedDatabaseHelper(getApplicationContext());
            dbHelper.deleteDrugDataWithRel(getIntent().getIntExtra("id", -1), obj.getP_no());

            int idx = items.indexOf(obj);
            items.remove(obj);
            adapter.notifyItemRangeRemoved(idx, items.size());
//            adapter.notifyDataSetChanged();



        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }

    public static void open(Activity activity, View v, int id, ArrayList<Integer> ids, String name) {
        Intent intent = new Intent(activity, SavedDrugActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, v, "name_view");
        intent.putExtra("id", id);
        intent.putExtra("ids", ids);
        intent.putExtra("name", name);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_drug);
        progressBar = findViewById(R.id.progress_bar);
        ImageButton back_btn = findViewById(R.id.back_btn);
        ImageButton share_btn = findViewById(R.id.share_btn);
        ImageButton alt_btn = findViewById(R.id.alt_btn);
        realm = Realm.getInstance(GlobalApp.getRealmConfiguration());
        realmResults = realm.where(Drug.class).findAllAsync();


        warning_msg = findViewById(R.id.warning_msg);
//        RealmResults<Drugs> items = realm.where(Drugs.class)
//                .contains("p_name", keyword, Case.INSENSITIVE)
//                .or()
//                .contains("mark_front", keyword, Case.INSENSITIVE)
//                .or()
//                .contains("mark_back", keyword, Case.INSENSITIVE)
//                .findAllAsync();
//        realm = Realm.getDefaultInstance();

        TextView title_txt = findViewById(R.id.title_txt);
        title_txt.setText(getIntent().getStringExtra("name"));

        recyclerView = findViewById(R.id.recycler_view);
        setUpRecyclerView();


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getCopyContent().length()>0){
                    showShareDialog();
                }else{
                    Toast.makeText(getApplicationContext(), "공유할 수 있는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifySavedList();
            }
        });
        searchWithFilter();

    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void searchWithFilter(){

        ArrayList<Integer> ids = getIntent().getIntegerArrayListExtra("ids");
        Integer[] ids_arr = Arrays.copyOf(ids.toArray(), ids.toArray().length, Integer[].class);
        RealmQuery<Drug> query = realmResults.where();
        query.in("p_no", ids_arr);
        RealmResults<Drug> results = query
                .findAllAsync();

        realmResults = results;
        addAllDrugs(realmResults);



        if(items.size()==0){
            warning_msg.setVisibility(View.VISIBLE);
        }else{
            warning_msg.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private void addAllDrugs(RealmResults<Drug> realmResults){

        if(items.size()>0) items.clear();

        for(Drug drug : realmResults){
            items.add(new DrugParcelable(drug));
        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        items.clear();
        realmResults = null;
        recyclerView.setAdapter(null);
        realm.close();
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
        touchHelper.attachToRecyclerView(recyclerView);

        adapter.setClickListener(new DrugAdapterV2.onClickListener() {
            @Override
            public void onClick(DrugParcelable drug, ImageView imageView) {

                DrugDetailV2Activity.open(SavedDrugActivity.this, imageView, drug.getP_no());

            }
        });
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
        for(DrugParcelable drug : items){
            content += drug.getP_name()+ ", ";
        }


        if(items.size()>0){
            int contentLength = content.length();
            content = content.substring(0, contentLength - 2);
        }

        return content;
    }


    private void showModifySavedList(){
        DialogPlus dialog = DialogPlus.newDialog(this)

                .setHeader(R.layout.dialog_saved_header)

                .setContentBackgroundResource(R.drawable.upper_corner_background)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setContentHolder(new ViewHolder(R.layout.dialog_modify_saved_list))
                .setGravity(Gravity.BOTTOM)
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
        TextView p_title_txt = findViewById(R.id.title_txt);
        name_tf.setText(p_title_txt.getText().toString());

        Button add_item_btn = dialog.getHeaderView().findViewById(R.id.add_item_btn);
        View remove_btn = dialog.getHolderView().findViewById(R.id.remove_btn);

        TextView title_txt = dialog.getHeaderView().findViewById(R.id.title_txt);

        title_txt.setText("수정");
        add_item_btn.setText("저장하기");

        add_item_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name_tf.getText().length()>1){

                    String name = name_tf.getText().toString();

                    int id = getIntent().getIntExtra("id", -1);

                    SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());
//
                    drugDBHelper.updateData(id, name);
//                    int priority = drugDBHelper.getAllData().getCount();
//                    drugDBHelper.insertData(priority, name, DateUtil.convertedSimpleFormat(new Date()));
                    Toast.makeText(getApplicationContext(), "수정했습니다.", Toast.LENGTH_SHORT).show();
                    TextView title_txt = findViewById(R.id.title_txt);
                    title_txt.setText(name);
                    dialog.dismiss();
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
////                            showSheetBottoms();
//                        }
//                    }, 800);

                }


            }
        });

        remove_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = getIntent().getIntExtra("id", -1);

                SavedDatabaseHelper drugDBHelper = new SavedDatabaseHelper(getApplicationContext());
                drugDBHelper.deleteDrugData(id);
                drugDBHelper.deleteData(id);

                Toast.makeText(getApplicationContext(), "삭제했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        dialog.show();
    }
    private void hideKeyboard(){
        Utils.hideKeyboard(this);
    }
}
