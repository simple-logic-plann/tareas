package com.damon.tareas;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class SendNotitications {

  DatabaseReference refTokens;

  RetrofietService retrofietService;
  SendNotitications( DatabaseReference refTokens){
    this.refTokens = refTokens;
    retrofietService = RetrofitClient.getClient().create(RetrofietService.class);
  }


  boolean sendNotification(String idUser){
    TaskCompletionSource<Boolean> task = new TaskCompletionSource<>();


    try {

      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          // Instantiate the RequestQueue.
          String url = "https://fcm.googleapis.com/fcm/send";
          Map<String, String> headers = new HashMap<>();
          headers.put("Authorization","key=AAAALJxquO0:APA91bFMAebAom8NTgK4DTJFf5dI7DIdYe7NZ8jzk2VzDWOwdrQvbbmtI8Wa49zgs1EtuHtJ1PAr32Zk8hNnvdShSPFaYA4oZB9pDT_NdbMATGKbCPG2jhqRdfFqpQ2dOpwJP5BWtPvb");
          headers.put("Content-Type","application/json");

          Map<String, Object> body =new HashMap<>();
          Map<String, Object> notification = new HashMap<>();
          notification.put("title","Viaje cancelado");
          notification.put("body","Lo sentimos el viaje se a cancelado");
          notification.put("icon","@mipmap/ic_launcher");
          body.put("notification",notification);

          refTokens.child(idUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> t) {
              if(t.isSuccessful()){
                List tokens = (List)  t.getResult().child("cloudMessagingTokens").getValue();
                for (Object tok : tokens){
                  String to = (String) tok;
                  body.put("to",to);
                  boolean r = _sendNotification(body,headers,url);
                }
                task.setResult(true);
              }else {
                task.setResult(false);

              }
            }
          }).addOnFailureListener(runnable -> {
            task.setResult(false);
          });
        }
      });
      return Tasks.await(task.getTask());
    } catch (ExecutionException e) {
      e.printStackTrace();
      return  false;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return  false;
    }
  }

  boolean _sendNotification(Map body,Map headers,String url){
    try {
      Call<Map> r = retrofietService.sendNotification(body,headers);
      r.enqueue(new Callback<Map>() {
        @Override
        public void onResponse(@NonNull Call<Map> call,@NonNull Response<Map> response) {
       Map a =  response.body();
        }

        @Override
        public void onFailure(@NonNull Call<Map> call,@NonNull Throwable t) {
          Log.i("INFO",t.getMessage());
        }
      });
      return true;
    } catch (Exception e){
      return false;
    }
  }

}
