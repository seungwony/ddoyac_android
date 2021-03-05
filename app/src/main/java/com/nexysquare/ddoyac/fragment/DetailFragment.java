package com.nexysquare.ddoyac.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.api.ApiManager;
import com.nexysquare.ddoyac.util.JsonHelper;
import com.nexysquare.ddoyac.view.NestedWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private NestedWebView webView;
    private static final String ASSETS_PATH = "file:///android_asset/druginfo.html";
    private String url;
    private ProgressBar progress_bar;

    private String apiResultXml;

    private String apiResultJson;
    public static DetailFragment newInstance(String json) {

        Bundle args = new Bundle();

        args.putString("json", json);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
        progress_bar = v.findViewById(R.id.progress_bar);
        webView = v.findViewById(R.id.nested_webview);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progress_bar.setVisibility(View.GONE);
        webView.setBackgroundColor(Color.TRANSPARENT);

        webView.getSettings().setDefaultTextEncodingName("utf-8");

        webView.setWebViewClient(new MyWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);


        webView.addJavascriptInterface(new AndroidInterface(getActivity()), "Android");



        String json="";
        Bundle bundle = getArguments();
        if(bundle!=null){
            json = bundle.getString("json");
            Gson gson = new Gson();
            JsonElement element = gson.fromJson (json, JsonElement.class);
            JsonObject jsonObj = element.getAsJsonObject();

            int p_no = Integer.parseInt(JsonHelper.hasJsonAndGetString(jsonObj, "p_no"));

            getDetailXml(String.valueOf(p_no));


        }

//        String content = json + getString(R.string.lorem);

//        String code = "<html><head><meta name='viewport' content='width=device-width, user-scalable=no'><base href=''><style>* { font-size :1.1em; line-height: 1.5em; } body { padding:10px; }</style></head><body>"+content+"</body></html>";

//        webView.loadData(code, "text/html; charset=utf-8", "UTF-8");


        if(url == null){
            url = ASSETS_PATH;
        }






//        TextView title = (TextView)findViewById(R.id.actionbar_title);

//        if(subject != null){
//            title.setText(subject);
//        }


//        progress.setVisibility(View.VISIBLE);
//        webView.setwe
    }

    private void getDetailXml(String id){

        final Call<String> call = ApiManager.getInstance().getApiCommonService().getDrugDetail(id);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {

//                    String xmlString;  // some XML String previously created

                    String xml  = response.body();

                    apiResultXml = xml;

                    loadContent();


                }else{
                    Log.e(TAG, String.valueOf(response.raw().code()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }


    private void getDetail(String id){

        final Call<String> call = ApiManager.getInstance().getApiCommonService().getDrugDetail(id);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {

//                    String xmlString;  // some XML String previously created

                    String xml  = response.body();


                   // Log.d(TAG, xml);

                    XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();


                    JSONObject jsonObject = xmlToJson.toJson();



                    String detailJson = jsonObject.toString();

//                    writeDescriptor("json_d_result", detailJson);
//                    Log.d(TAG, detailJson);
//                    writeDescriptor(detailJson);

                    try {

                    if(jsonObject.has("response")){


                            JSONObject res = jsonObject.getJSONObject("response");

                            if(res.has("header")){

                                JSONObject header =  res.getJSONObject("header");
                                String resultCode =  header.getString("resultCode");
                                String resultMsg =  header.getString("resultMsg");

                                if(res.has("body")){


                                    JsonObject jsObj = new JsonObject();
                                    JSONObject jsonBody = new JSONObject();


                                    JSONObject body =  res.getJSONObject("body");

                                    if(body.has("items")){
                                        JSONObject items = body.getJSONObject("items");
                                        if(items.has("item")){
                                            JSONObject item = items.getJSONObject("item");


                                            if(item.has("BAR_CODE")){


                                                //표준코드
                                                String bar_code = item.getString("BAR_CODE");
                                                jsonBody.put("bar_code", bar_code);
                                                jsObj.addProperty("bar_code", bar_code);
                                            }


                                            if(item.has("CANCEL_NAME")){
                                                //취하구분
                                                String cancel_name = item.getString("CANCEL_NAME");
                                                jsonBody.put("cancel_name", cancel_name);
                                                jsObj.addProperty("cancel_name", cancel_name);
                                            }

                                            if(item.has("CHART")){

                                                //성상

                                                String chart = item.getString("CHART");
                                                jsonBody.put("chart", chart);
                                                jsObj.addProperty("chart", chart);
                                            }
                                            if(item.has("CLASS_NO")){

                                                //분류
                                                String class_no = item.getString("CLASS_NO");
                                                jsonBody.put("class_no", class_no);
                                                jsObj.addProperty("class_no", class_no);
                                            }
                                            if(item.has("CNSGN_MANUF")){

                                                //위탁제조업체
                                                String cnsgn_manuf = item.getString("CNSGN_MANUF");
                                                jsonBody.put("cnsgn_manuf", cnsgn_manuf);
                                                jsObj.addProperty("cnsgn_manuf", cnsgn_manuf);
                                            }

                                            if(item.has("DOC_TEXT")){

                                                //제조방법
                                                String doc_text = item.getString("DOC_TEXT");
                                                jsonBody.put("doc_text", doc_text);
                                                jsObj.addProperty("doc_text", doc_text);
                                            }

                                            if(item.has("EDI_CODE")){

                                                //보험코드
                                                String edi_code = item.getString("EDI_CODE");
                                                jsonBody.put("edi_code", edi_code);
                                                jsObj.addProperty("edi_code", edi_code);
                                            }

                                            if(item.has("EE_DOC_ID")){

                                                //효능효과 pdf
                                                String ee_doc_id = item.getString("EE_DOC_ID");
                                                jsonBody.put("ee_doc_id", ee_doc_id);
                                                jsObj.addProperty("ee_doc_id", ee_doc_id);
                                            }

                                            if(item.has("EE_DOC_DATA")){

                                                //효능효과

//                                                jsObj.add();
                                                JSONArray arr = new JSONArray();

                                                JSONObject EE_DOC_DATA = item.getJSONObject("EE_DOC_DATA");

                                                if(EE_DOC_DATA.has("DOC")){
                                                    JSONObject docJson = EE_DOC_DATA.getJSONObject("DOC");
                                                    if(docJson.has("SECTION")){
                                                        JSONObject sectionJson = docJson.getJSONObject("SECTION");

                                                        if(sectionJson.has("ARTICLE")){

                                                            if(sectionJson.get("ARTICLE") instanceof  JSONObject){
                                                                JSONObject articleJson = sectionJson.getJSONObject("ARTICLE");


                                                                if(articleJson.has("PARAGRAPH")){

                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONObject){
                                                                        JSONObject p =  articleJson.getJSONObject("PARAGRAPH");
                                                                        if(p.has("content")){
                                                                            String content = p.getString("content");
                                                                            arr.put(content);
                                                                        }
                                                                    }


                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONArray){

                                                                        JSONArray paragraph = articleJson.getJSONArray("PARAGRAPH");


                                                                        for(int i = 0 ; i < paragraph.length() ; i++ ){
                                                                            JSONObject p = (JSONObject)paragraph.get(i);
                                                                            if(p.has("content")){
                                                                                String content = p.getString("content");
                                                                                arr.put(content);
                                                                            }
                                                                        }
                                                                    }

                                                                }
                                                            }

                                                            if(sectionJson.get("ARTICLE") instanceof  JSONArray) {
                                                                JSONArray articles = sectionJson.getJSONArray("ARTICLE");
                                                                for(int j = 0 ; j < articles.length() ; j++ ){
                                                                    JSONObject a = (JSONObject)articles.get(j);
                                                                    if(a.has("title")){
                                                                        String title = a.getString("title");
                                                                        arr.put(title);
                                                                    }
                                                                }
                                                            }

                                                        }

                                                    }
                                                }


                                                jsonBody.put("ee_doc_data", arr);
                                            }

                                            if(item.has("ENTP_NAME")){

                                                //업체명
                                                String entp_name = item.getString("ENTP_NAME");
                                                jsonBody.put("entp_name", entp_name);
                                                jsObj.addProperty("entp_name", entp_name);
                                            }

                                            if(item.has("ENTP_NO")){

                                                //업체허가번호
                                                String entp_no = item.getString("ENTP_NO");
                                                jsonBody.put("entp_no", entp_no);
                                                jsObj.addProperty("entp_no", entp_no);
                                            }

                                            if(item.has("ETC_OTC_CODE")){

                                                //전문일반
                                            }


                                            if(item.has("GBN_NAME")){

                                                //변경이력
                                                String gbn_name = item.getString("GBN_NAME");
                                                jsonBody.put("gbn_name", gbn_name);
//                                                jsObj.addProperty("gbn_name", gbn_name);
                                            }


                                            if(item.has("INDUTY_TYPE")){

                                                //업종구분

                                                String induty_type = item.getString("INDUTY_TYPE");
                                                jsonBody.put("induty_type", induty_type);
                                                jsObj.addProperty("induty_type", induty_type);
                                            }


                                            if(item.has("INGR_NAME")){

                                                //첨가제
                                                String ingr_name = item.getString("INGR_NAME");
                                                jsonBody.put("ingr_name", ingr_name);
                                                jsObj.addProperty("ingr_name", ingr_name);
                                            }

                                            if(item.has("INSERT_FILE")){

                                                //첨부문서
                                            }


                                            if(item.has("ITEM_NAME")){

                                                //품목명
                                                String item_name = item.getString("ITEM_NAME");
                                                jsonBody.put("item_name", item_name);
                                                jsObj.addProperty("item_name", item_name);
                                            }

                                            if(item.has("ITEM_PERMIT_DATE")){

                                                //허가일자

                                                String item_permit_date = item.getString("ITEM_PERMIT_DATE");
                                                jsonBody.put("item_permit_date", item_permit_date);
                                                jsObj.addProperty("item_permit_date", item_permit_date);
                                            }

                                            if(item.has("ITEM_SEQ")){

                                                //품목기준코드
                                                String item_seq = item.getString("ITEM_SEQ");
                                                jsonBody.put("item_seq", item_seq);
                                                jsObj.addProperty("item_seq", item_seq);
                                            }

                                            if(item.has("MAIN_ITEM_INGR")){

                                                //유효성분
                                                String main_item_ingr = item.getString("MAIN_ITEM_INGR");
                                                jsonBody.put("main_item_ingr", main_item_ingr);
                                                jsObj.addProperty("main_item_ingr", main_item_ingr);
                                            }

                                            if(item.has("MAKE_MATERIAL_FLAG")){

                                                //완제/원료구분
                                                String make_material_flag = item.getString("MAKE_MATERIAL_FLAG");
                                                jsonBody.put("make_material_flag", make_material_flag);
                                                jsObj.addProperty("make_material_flag", make_material_flag);
                                            }

                                            if(item.has("MATERIAL_NAME")){

                                                //원료성분
                                                String material_name = item.getString("MATERIAL_NAME");
                                                jsonBody.put("material_name", material_name);
                                                jsObj.addProperty("material_name", material_name);
                                            }

                                            if(item.has("NARCOTIC_KIND_CODE")){

                                                //마약종류코드
                                            }

                                            if(item.has("NB_DOC_DATA")){

                                                JSONArray arr = new JSONArray();
                                                //주의사항(일반) 문서 데이터
                                                JSONObject NB_DOC_DATA = item.getJSONObject("NB_DOC_DATA");

                                                if(NB_DOC_DATA.has("DOC")){
                                                    JSONObject docJson = NB_DOC_DATA.getJSONObject("DOC");
                                                    if(docJson.has("SECTION")){
                                                        JSONObject sectionJson = docJson.getJSONObject("SECTION");

                                                        if(sectionJson.has("ARTICLE")){

                                                            if(sectionJson.get("ARTICLE")instanceof JSONObject){

                                                                JSONObject articleJson = sectionJson.getJSONObject("ARTICLE");
                                                                if(articleJson.has("PARAGRAPH")){

                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONObject){

                                                                        JSONObject paragraph = articleJson.getJSONObject("PARAGRAPH");


                                                                        if(paragraph.has("content")){
                                                                            String content = paragraph.getString("content");
                                                                            arr.put(content);
                                                                        }
                                                                    }

                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONArray){

                                                                        JSONArray paragraph = articleJson.getJSONArray("PARAGRAPH");


                                                                        for(int i = 0 ; i < paragraph.length() ; i++ ){
                                                                            JSONObject p = (JSONObject)paragraph.get(i);
                                                                            if(p.has("content")){
                                                                                String content = p.getString("content");
                                                                                arr.put(content);
                                                                            }
                                                                        }
                                                                    }


                                                                }
                                                            }

                                                            if(sectionJson.get("ARTICLE")instanceof JSONArray){
                                                                JSONArray articleArr = sectionJson.getJSONArray("ARTICLE");

                                                                for(int i = 0 ; i < articleArr.length() ; i++){
                                                                    JSONObject a = (JSONObject)articleArr.get(i);

                                                                    JSONObject jsonArticle = new JSONObject();
                                                                    JSONArray a_arr = new JSONArray();

                                                                    if(a.has("PARAGRAPH")){



                                                                        if(a.get("PARAGRAPH") instanceof JSONObject){
                                                                            JSONObject p = a.getJSONObject("PARAGRAPH");
                                                                            String content = p.getString("content");

                                                                            a_arr.put(content);

                                                                        }

                                                                        if(a.get("PARAGRAPH") instanceof JSONArray){
                                                                            JSONArray parr = a.getJSONArray("PARAGRAPH");
                                                                            for(int j = 0 ; j < parr.length() ; j++ ){
                                                                                JSONObject p = (JSONObject)parr.get(j);
                                                                                if(p.has("content")){

                                                                                    String content = p.getString("content");

                                                                                    a_arr.put(content);


                                                                                }

                                                                            }
                                                                        }


                                                                        String title = a.getString("title");

                                                                        jsonArticle.put("title", title);

//                                                                    String p = paragraph.toString();
//                                                                    arr.put(p);
//                                                                    Log.d(TAG, p);

//                                                                    if(paragraph.has("title")){
//
//                                                                    }


                                                                        jsonArticle.put("p", a_arr);
                                                                    }

//                                                                arr.put(jsonArticle);

                                                                    arr.put(jsonArticle);
                                                                }
                                                            }



                                                        }

                                                    }
                                                }
                                                jsonBody.put("nb_doc_data", arr);
                                            }

                                            if(item.has("NB_DOC_ID")){

                                                //주의사항 pdf
                                                String nb_doc_id = item.getString("NB_DOC_ID");
                                                jsonBody.put("nb_doc_id", nb_doc_id);
                                                jsObj.addProperty("nb_doc_id", nb_doc_id);
                                            }

                                            if(item.has("NEWDRUG_CLASS_NAME")){

                                                //신약
                                                String newdrug_class_name = item.getString("NEWDRUG_CLASS_NAME");
                                                jsonBody.put("newdrug_class_name", newdrug_class_name);
                                                jsObj.addProperty("newdrug_class_name", newdrug_class_name);
                                            }

                                            if(item.has("PACK_UNIT")){

                                                //포장단위
                                                String pack_unit = item.getString("PACK_UNIT");
                                                jsonBody.put("pack_unit", pack_unit);
                                                jsObj.addProperty("pack_unit", pack_unit);
                                            }

                                            if(item.has("PERMIT_KIND_NAME")){

                                                //허가/신고구분
                                                String permit_kind_name = item.getString("PERMIT_KIND_NAME");
                                                jsonBody.put("permit_kind_name", permit_kind_name);
                                                jsObj.addProperty("permit_kind_name", permit_kind_name);
                                            }


                                            if(item.has("PN_DOC_DATA")){

                                                //주의사항(전문) 문서 데이터
                                                String pn_doc_data = item.getString("PN_DOC_DATA");
                                                jsonBody.put("pn_doc_data", pn_doc_data);
//                                                jsObj.addProperty("permit_kind_name", permit_kind_name);
                                            }


                                            if(item.has("REEXAM_DATE")){

                                                //재심사기간
                                                String reexam_date = item.getString("REEXAM_DATE");
                                                jsonBody.put("reexam_date", reexam_date);
                                                jsObj.addProperty("reexam_date", reexam_date);
                                            }

                                            if(item.has("REEXAM_TARGET")){

                                                //재심사대상
                                                String reexam_target = item.getString("REEXAM_TARGET");
                                                jsonBody.put("reexam_target", reexam_target);
                                                jsObj.addProperty("reexam_target", reexam_target);
                                            }

                                            if(item.has("STORAGE_METHOD")){

                                                //저장방법
                                                String storage_method = item.getString("STORAGE_METHOD");
                                                jsonBody.put("storage_method", storage_method);
                                                jsObj.addProperty("storage_method", storage_method);
                                            }

                                            if(item.has("TOTAL_CONTENT")){

                                                //
                                            }

                                            if(item.has("UD_DOC_ID")){

                                                //용법용량 pdf

                                            }

                                            if(item.has("UD_DOC_DATA")){

                                                JSONArray arr = new JSONArray();
                                                //용법용량 데이터
                                                JSONObject UD_DOC_DATA = item.getJSONObject("UD_DOC_DATA");

                                                if(UD_DOC_DATA.has("DOC")){
                                                    JSONObject docJson = UD_DOC_DATA.getJSONObject("DOC");
                                                    if(docJson.has("SECTION")){
                                                        JSONObject sectionJson = docJson.getJSONObject("SECTION");

                                                        if(sectionJson.has("ARTICLE")){


                                                            if(sectionJson.get("ARTICLE")instanceof JSONObject){

                                                                JSONObject articleJson = sectionJson.getJSONObject("ARTICLE");
                                                                if(articleJson.has("PARAGRAPH")){

                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONObject){

                                                                        JSONObject paragraph = articleJson.getJSONObject("PARAGRAPH");


                                                                        if(paragraph.has("content")){
                                                                            String content = paragraph.getString("content");
                                                                            arr.put(content);
                                                                        }
                                                                    }

                                                                    if(articleJson.get("PARAGRAPH") instanceof JSONArray){

                                                                        JSONArray paragraph = articleJson.getJSONArray("PARAGRAPH");


                                                                        for(int i = 0 ; i < paragraph.length() ; i++ ){
                                                                            JSONObject p = (JSONObject)paragraph.get(i);
                                                                            if(p.has("content")){
                                                                                String content = p.getString("content");
                                                                                arr.put(content);
                                                                            }
                                                                        }
                                                                    }


                                                                }
                                                            }


                                                            if(sectionJson.get("ARTICLE") instanceof JSONArray){

                                                                JSONArray articleArr = sectionJson.getJSONArray("ARTICLE");

                                                                for(int i = 0 ; i < articleArr.length() ; i++){
                                                                    JSONObject a = (JSONObject)articleArr.get(i);

                                                                    JSONObject jsonArticle = new JSONObject();
                                                                    JSONArray a_arr = new JSONArray();

                                                                    if(a.has("PARAGRAPH")){



                                                                        if(a.get("PARAGRAPH") instanceof JSONObject){
                                                                            JSONObject p = a.getJSONObject("PARAGRAPH");
                                                                            String content = p.getString("content");

                                                                            a_arr.put(content);

                                                                        }

                                                                        if(a.get("PARAGRAPH") instanceof JSONArray){
                                                                            JSONArray parr = a.getJSONArray("PARAGRAPH");
                                                                            for(int j = 0 ; j < parr.length() ; j++ ){
                                                                                JSONObject p = (JSONObject)parr.get(j);
                                                                                if(p.has("content")){

                                                                                    String content = p.getString("content");

                                                                                    a_arr.put(content);


                                                                                }

                                                                            }
                                                                        }


                                                                        String title = a.getString("title");

                                                                        jsonArticle.put("title", title);

//                                                                    String p = paragraph.toString();
//                                                                    arr.put(p);
//                                                                    Log.d(TAG, p);

//                                                                    if(paragraph.has("title")){
//
//                                                                    }


                                                                        jsonArticle.put("p", a_arr);
                                                                    }

//                                                                arr.put(jsonArticle);

                                                                    arr.put(jsonArticle);
                                                                }

                                                            }



                                                        }

                                                    }
                                                }

                                                jsonBody.put("ud_doc_data", arr);


                                            }

                                            if(item.has("VALID_TERM")){

                                                //유효기간

                                                String valid_term = item.getString("VALID_TERM");
                                                jsonBody.put("valid_term", valid_term);

                                                jsObj.addProperty("valid_term", valid_term);
                                            }
                                        }



                                    }


                                    apiResultJson = jsonBody.toString().replace("\\n", "<br>");
//                                    apiResultJson = jsObj.toString();

//                                    Log.d(TAG,apiResultJson);

                                }
                            }




                    }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }finally {
                        loadContent();
                    }

                }else{
                    Log.e(TAG, String.valueOf(response.raw().code()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void writeDescriptor(String fileName, String content){
        try {


            File logs = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/opencv4test/"+fileName+".txt");

            FileWriter fw;
            BufferedWriter bw;

            File dir = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath() + "/opencv4test/");
            dir.mkdirs();

            logs.createNewFile();

            logs = new File(
                    Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/opencv4test/"+fileName+".txt");

            fw = new FileWriter(logs, true);
            bw = new BufferedWriter(fw);

            bw.write(content + "\n");
            bw.close();



        } catch (IOException e1) {

            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }


    private void loadContent(){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(url);
//                if(apiResultJson!=null){
////                html = "javascript:showJson('"+apiResultJson+"')";
//                    String html = "javascript:initExtend('"+apiResultJson+"')";
//                    webView.loadUrl(html);
//
////                    progress_bar.setVisibility(View.GONE);
//                }
            }
        }, 100);
    }

    private class MyWebViewClient extends WebViewClient {
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            progress.setVisibility(View.GONE);


//            String uid = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("uid", null);
            String json="";
            Bundle bundle = getArguments();
            if(bundle!=null){
                json = bundle.getString("json");
            }
//            String html = "javascript:initData('"+json+"')";

//            Log.d(TAG, html);


            if(apiResultJson!=null){
//                html = "javascript:showJson('"+apiResultJson+"')";
//                StringEscapeUtils.
//                try {
//
////                    String html = "javascript:initExtend('"+URLEncoder.encode(apiResultJson, "UTF8")+"')";
//                    String html = "javascript:initExtend('"+apiResultJson+"')";
//                    writeDescriptor(html);
//                    webView.loadUrl(html);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

//                String html = "javascript:initExtend('"+apiResultJson+"')";
                String html = "javascript:initDataWithExtend('"+json+"','"+apiResultJson+"')";

                webView.loadUrl(html);



            }else if(apiResultXml!=null) {
                String html = "javascript:initDataWithExtendXml('"+json+"','"+apiResultXml+"')";

                webView.loadUrl(html);
            }else {
                String html = "javascript:initData('"+json+"')";
                webView.loadUrl(html);
            }

            progress_bar.setVisibility(View.GONE);

//            webView.loadUrl(html);
        }
    }


    class AndroidInterface {
        private final String TAG = getClass().getSimpleName();
        Context mContext;


        public AndroidInterface(Context c){
            mContext = c;
        }



        @JavascriptInterface
        public void showToast(String msg) {


            Log.d(TAG, msg);
            Toast.makeText(mContext ,  msg, Toast.LENGTH_SHORT).show();


        }

        @JavascriptInterface
        public void saveHtml(String code) {


            Log.d(TAG, code);
//            writeDescriptor("htmlfile5", code);
//            Toast.makeText(mContext ,  msg, Toast.LENGTH_SHORT).show();


        }

        @JavascriptInterface
        public void exceptionJson(String code) {



            Date date = new Date();
            Log.d(TAG, code);
            writeDescriptor("exception_xml_log_"+date.getTime(), code);
//            Toast.makeText(mContext ,  msg, Toast.LENGTH_SHORT).show();


        }

    }
}
