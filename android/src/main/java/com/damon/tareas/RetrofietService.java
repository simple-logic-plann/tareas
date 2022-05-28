package com.damon.tareas;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

interface RetrofietService {

  @POST("send")
  Call<Map> sendNotification (@Body Map body, @HeaderMap Map<String, String> headers);
}
