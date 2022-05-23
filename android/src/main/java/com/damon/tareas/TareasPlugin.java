package com.damon.tareas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** TareasPlugin */
public class TareasPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static final String TAG = "INFO";
    /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tareas");
    channel.setMethodCallHandler(this);
  }

  @SuppressLint("RestrictedApi")
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("initialize")){
       try {
           Constraints constraints = new Constraints.Builder()
                       .setRequiredNetworkType(NetworkType.CONNECTED)
                       .build();

           Data.Builder data = new Data.Builder();

           HashMap<String,Object> datos = (HashMap<String, Object>)call.arguments;
           data.put("viajeId",datos.get("viajeId"));
           data.put("tiempo",datos.get("tiempo"));
           data.put("minutosMulta",datos.get("minutosMulta"));
           data.put("multa",datos.get("multa").toString());
           data.put("minutosAnticipacion",datos.get("minutosAnticipacion"));

           SharedPreferences sharedPreferences = activity.getSharedPreferences("conductor", Context.MODE_PRIVATE);
           SharedPreferences.Editor editor = sharedPreferences.edit();
           editor.putString("viajeId",(String) datos.get("viajeId"));
           editor.putLong("tiempo",(long)datos.get("tiempo"));
           editor.putString("multa",datos.get("multa").toString());
           editor.putInt("minutosMulta",(int)datos.get("minutosMulta"));
           editor.putInt("minutosAnticipacion",(int)(datos.get("minutosAnticipacion")));
           editor.apply();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(Cancelaciones.class,15, TimeUnit.MINUTES
                   )
                   .addTag("ConductorCancelacion")
                   .setConstraints(constraints)
                   .setInputData(data.build())
                .setBackoffCriteria(BackoffPolicy.LINEAR,1,TimeUnit.MINUTES)
                   .setInitialDelay(30,TimeUnit.SECONDS)
                   .build();
           WorkManager.getInstance(activity).enqueueUniquePeriodicWork("ConductorCancelacion", ExistingPeriodicWorkPolicy.REPLACE,workRequest);

//
//
//        WorkRequest
//                workRequest = new OneTimeWorkRequest.Builder(Cancelaciones.class)
//                .addTag("ConductorCancelacion")
//                .setConstraints(constraints)
//                .setInputData(data.build())
//                .build();
//
//        WorkManager.getInstance(activity).enqueue(workRequest);

           Log.i(TAG, "INITIALIZE ");
           result.success(true);
       }catch (Exception e){
           Log.i(TAG, "INITIALIZE ERROR");

           System.out.println(e);
           result.error("500",e.getMessage(),"Error al inicializar");
       }
    }else if(call.method.equals("stop")){
        try {
            WorkManager manager = WorkManager.getInstance(activity);

            manager.cancelUniqueWork("ConductorCancelacion");
            manager.pruneWork();
            Log.i(TAG, "STOP");

            result.success(true);
        }catch (Exception e){
            Log.i(TAG, "STOP ERROR");

            System.out.println(e);
            result.error("500",e.getMessage(),"Errror al cancelar");
        }
    }else {
          result.notImplemented();

      }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {

    }
}
