package com.nexysquare.ddoyac.model;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.JsonElement;

public class WebViewInterface {
    private final String TAG = getClass().getSimpleName();
    Context mContext;

    public interface StateChangeListener{

        void onProgress();
        void onSuccess(String msg);
        void onFailed(String msg);
    }

    StateChangeListener listener;
    public WebViewInterface(Context c){
        mContext = c;
    }

    public WebViewInterface(Context c, StateChangeListener listener){
        mContext = c;
        this.listener = listener;
    }


    @JavascriptInterface
    public void showToast(String msg) {


        //Toast.makeText(mContext ,  "uid [" + msg + "] 설정되었습니다.", Toast.LENGTH_SHORT).show();


    }

    @JavascriptInterface
    public void setInitData(String mem_id, String data) {


//        Log.d(TAG, "mem_id : " + mem_id + " \ndata : " + data);

        if(listener!=null) listener.onProgress();






//        sendMemData(mem_id, data);
    }






//
//    private void sendMemData(String mem_id, String data){
//
////        Log.d(TAG, "ContentLike uid:" + user_id + " id:" + target_id);
//        final Call<JsonElement> call = ApiManager.getInstance().getApiUserService().initUserSet(mem_id, "init", data);
//
//
//        call.enqueue(new Callback<JsonElement>() {
//            @Override
//            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
//                try {
//                    if (response.isSuccessful()) {
//                        JsonElement element = response.body();
//
//                        Log.d(TAG, element.toString());
//
//                        if (element.getAsJsonObject().has("error")) {
//                            String error = JsonUtil.hasJsonAndGetString(element.getAsJsonObject(), "error");
//                            CommonUI.showToast(mContext, error);
//                            return;
//                        }
//
//                        if (element.getAsJsonObject().has("affectedRows")) {
//                            String affectedRows = JsonUtil.hasJsonAndGetString(element.getAsJsonObject(), "affectedRows");
//
//                            if(Integer.valueOf(affectedRows)>0){
////                            CommonUI.showToast(getApplicationContext(), getString(R.string.msg_done_to_remove_rental_car));
//
//                                if(listener!=null) listener.onSuccess(element.toString());
//                            }
//
//                        }
//
//
//                    }else{
//                        Log.e(TAG, response.body().toString());
////                    refreshLayout.setRefreshing(false);
////                        Toast.makeText(getApplication(), "서버와의 통신에 문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
//
//                    }
//                }catch (Exception ex){
//                    Log.e(TAG, ex.toString());
//                    Log.e(TAG, String.valueOf(response.raw().code()));
////                Log.e(TAG, response.toString());
////                Log.e(TAG, response.raw().toString());
////                Log.e(TAG, response.headers().toString());
//
////                Toast.makeText(getContext(), "서버와의 통신에 문제가 생겼습니다.", Toast.LENGTH_SHORT).show();
//
//                    if(listener!=null) listener.onFailed(null);
//
//                }finally {
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonElement> call, Throwable t) {
//
//                if(listener!=null) listener.onFailed(null);
//            }
//        });
//    }



}
