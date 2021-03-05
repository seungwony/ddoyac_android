package com.nexysquare.ddoyac.api;

import com.google.gson.JsonElement;

import org.xmlpull.v1.XmlPullParser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiCommonService {


    @GET("/1471057/MdcinPrductPrmisnInfoService1/getMdcinPrductItem")
    Call<JsonElement> getDrugDetail(@Query("ServiceKey") String ServiceKey, @Query("item_seq") String item_seq);

    @GET("/1471057/MdcinPrductPrmisnInfoService1/getMdcinPrductItem?ServiceKey=SxFlIFuuam1GbQpirOoR%2FSkLZRJN9Qwf4a3%2FI3QkmE%2Fij6u07joiJ6DgZmToQElFA32HgCgMTZ%2BEaayfFLXthg%3D%3D")
    Call<String> getDrugDetail(@Query("item_seq") String item_seq);
}
