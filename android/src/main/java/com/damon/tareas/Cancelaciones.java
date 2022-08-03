package com.damon.tareas;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Cancelaciones extends Worker {

  private static final String TAG = "INFO";
  private final NotificationManager mNotificationManager;
  DatabaseReference reference, userRef, plannHistoricos,refTokens;
  private SendNotitications sendNotification;

  public Cancelaciones(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
    FirebaseApp.initializeApp(getApplicationContext());
    reference = FirebaseDatabase.getInstance().getReference().child("projects/proj_ukivJz2aMTBYFtUytqDsTV/data/viajesBD2");
    userRef = FirebaseDatabase.getInstance().getReference().child("projects/proj_ukivJz2aMTBYFtUytqDsTV/data/usuariosBD");
    plannHistoricos = FirebaseDatabase.getInstance().getReference().child("projects/proj_ukivJz2aMTBYFtUytqDsTV/data/plannHistoricos");
    refTokens = FirebaseDatabase.getInstance().getReference().child("projects/proj_ukivJz2aMTBYFtUytqDsTV/apps/app_tvAqgYPuc5qypUtdcscas7/members");
    mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
    sendNotification = new SendNotitications(refTokens);
  }

  @NonNull
  @Override
  public Result doWork() {


    return getDataViaje();
  }

  Result getDataViaje() {
    try {

      SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("conductor", Context.MODE_PRIVATE);
      String idViaje = sharedPreferences.getString("viajeId", "11sdad");
      long tiempo = sharedPreferences.getLong("tiempo", 100000000);
      int minutosMulta = sharedPreferences.getInt("minutosMulta", 16);
      int minutosAnticipacion = sharedPreferences.getInt("minutosAnticipacion", 30);
      double multa = Double.parseDouble(sharedPreferences.getString("multa", "1"));
      Date hoy = new Date();
      Date fechaViaje = new Date();
      fechaViaje.setTime(tiempo);
      Date m = new Date();
      m.setTime(minutosMulta * 60 * 1000);
      Log.i(TAG, "MINUTOS "+ m.getMinutes() );
      Log.i(TAG, "RESTANTE "+((tiempo - hoy.getTime()) <0) );

              if ((tiempo - hoy.getTime()) <= ((long) minutosAnticipacion * 60 * 1000)
              && (tiempo - hoy.getTime()) >0) {
                showNotification("No olvides tu viaje", "Falta 30 minutos para empezar" +
                        " tu viaje no olvides nada ", "Plann momentos que marcan");
              } else if ((tiempo - hoy.getTime()) <0) {
                if((hoy.getTime()-tiempo )  >=((long) minutosMulta * 60 * 1000) ){
                  verInfo(idViaje, tiempo,multa);
                }
              }
//        verInfo(idViaje, tiempo, multa);



      Log.i(TAG, "SHOW NOTIFICATION ");

      return Result.success();
    } catch (Exception e) {
      Log.i(TAG, "ERROR " + e.getLocalizedMessage());

      showNotification("Error ", e.getMessage(), "TRY CATCH");
      return Result.retry();
    }
  }

  void verInfo(String viajeId, long tiempo, double multa) throws ExecutionException, InterruptedException {

    Map r = getViaje(viajeId);


    String statusViaje = (String)r.get("statusViaje");
    if(statusViaje.equals("publicado")){
      List pasajeros = (List) r.get("pasajeros");
      String conductorId =(String)r.get("conductorId");

      double comision = Double.parseDouble(r.get("comision").toString());

      if(!pasajeros.isEmpty()){
        int pagados = 0;
        for (Object o : pasajeros){
          Map p = (Map) o;
          String statusPago = (String) p.get("statusPago");
          if(statusPago.equals("pagado")
                  ||statusPago.equals("efectivo")){
            pagados++;
          }
          cancelarViaje(pasajeros,conductorId,viajeId,pagados,multa,comision);
        }
      }
    }

    Log.i(TAG, "SI ENTRO ");



  }


  void cancelarViaje(List pasajeros, String conductorId, String viajeId, int pagados, double multa,double comision) throws ExecutionException, InterruptedException {
    List p = new ArrayList();
    p.addAll(pasajeros);



    for (int i = 0; i < pasajeros.size(); i++) {
      Map pasajero = (Map) pasajeros.get(i);
      if (!((String) pasajero.get("pasajeroId")).equals("null")) {
        Log.i(TAG, String.valueOf(!esElMimsoUsuario(i, pasajeros)));
        if (!esElMimsoUsuario(i, pasajeros)) {
          String statusPago = (String) pasajero.get("statusPago");
          String pasajeroId = (String) pasajero.get("pasajeroId");
          Map user = getDataUser(pasajeroId);
          Map conductorInfo = getDataUser(conductorId);
          if (statusPago.equals("pagado")) {
            double costoPuesto = (double) Double.parseDouble(pasajero.get("costoPuesto").toString());
            int nroPuesto = Integer.parseInt(pasajero.get("nroPuesto").toString());
            double total = costoPuesto * nroPuesto;
            double comisionPlann = comision;
            double valor = total - (nroPuesto * comisionPlann);
            double saldoUser = Double.parseDouble(user.get("saldo").toString());
            Map params = new HashMap();
            params.put("pago", pasajero.get("pago"));
            params.put("saldo", saldoUser);
            params.put("valor", valor);
            double saldo = saldoDevolver(params);
            double feeActual = 0;
            if (user.get("fee") != null) {
              feeActual = Double.parseDouble(user.get("fee").toString());
            }
            Log.i(TAG,pasajeroId);

            double diferenciaTarjeta = 0;
            Map objetoPago = (Map) pasajero.get("pago");
            if (objetoPago.get("diferenciaTarjeta") != null) {
              diferenciaTarjeta = Double.parseDouble(objetoPago.get("diferenciaTarjeta").toString());
            }

            double fee = calcularFeeTarjeta(total) +
                    diferenciaTarjeta;
            BigDecimal bd = new BigDecimal(feeActual + fee).setScale(2, RoundingMode.HALF_UP);
          double feeNuevo =  bd.doubleValue();
          BigDecimal bSaldo = new BigDecimal(saldo).setScale(2, RoundingMode.HALF_UP);
          double nuevoSaldo =  bSaldo.doubleValue();

            Map nuevaData = new HashMap();
            nuevaData.put("fee", feeNuevo);
            nuevaData.put("saldo", nuevoSaldo);
            boolean result = updateDataUser(pasajeroId, nuevaData);

            double saldoC = Double.parseDouble(conductorInfo.get("saldo").toString());

            double totalSinComision = costoPuesto - (costoPuesto * (1 - 0.955) + 024 + comisionPlann) * nroPuesto;

            double devolver = saldoC - totalSinComision;

            BigDecimal dDevolver = new BigDecimal(devolver).setScale(2, RoundingMode.HALF_UP);
            double nuevoDevolver =  dDevolver.doubleValue();
            Map dataConduct = new HashMap();
            dataConduct.put("saldo", nuevoDevolver);
            boolean resultC = updateDataUser(conductorId, dataConduct);
          } else if (statusPago.equals("efectivo")) {
            double comisionPlann = comision;

            double costoPuesto = (double) Double.parseDouble(pasajero.get("costoPuesto").toString());
            int nroPuesto = Integer.parseInt(pasajero.get("nroPuesto").toString());
            double comisionP = costoPuesto - (((costoPuesto * 0.955) - 0.24) - comisionPlann);
            double saldoC = Double.parseDouble(conductorInfo.get("saldo").toString());

            double devolver = (nroPuesto * comisionP) + saldoC;
            Map dataConduct = new HashMap();
            BigDecimal dDevolver = new BigDecimal(devolver).setScale(2, RoundingMode.HALF_UP);
            double nuevoDevolver =  dDevolver.doubleValue();
            dataConduct.put("saldo", nuevoDevolver);

            boolean r = updateDataUser(conductorId, dataConduct);
          }
        }
      }

    }

    Log.i(TAG,"SALIO DEL PRIMER FOR");


    for (int i = 0; i < pasajeros.size(); i++) {
      Map pasajero = (Map) pasajeros.get(i);
      String pasajeroId = (String) pasajero.get("pasajeroId");
      if (!pasajero.equals("null")) {

        String statusPago = (String) pasajero.get("statusPago");
        if ((statusPago.equals("esperando") || statusPago.equals("comprobando"))
                || (statusPago.equals("esperandoM") || statusPago.equals("comprobandoM"))
                || statusPago.equals("null")) {
          Map dataUser = new HashMap();
          dataUser.put("viajeId", "0");
          boolean r = updateDataUser(pasajeroId, dataUser);
        }
      }
    }

    List pasajerosNuevo = reordenarPasajeros(pasajeros);
    Map viaje = new HashMap();
    viaje.put("pasajeros", pasajerosNuevo);
    viaje.put("statusViaje", "cancelado");

    boolean rViaje = updateViajesBd(viajeId, viaje);

    List detallesViajeC = new ArrayList();
    Map d = new HashMap();
    d.put("aire", "0");
    d.put("fumar", "0");
    d.put("maleta", "0");
    d.put("mascota", "0");
    d.put("metodosPago", "0");
    d.put("ninos", "0");
    detallesViajeC.add(d);

    Map condutorActualizar = new HashMap();
    condutorActualizar.put("viajeId", "0");
    condutorActualizar.put("pap", false);
    condutorActualizar.put("detallesViajeConductor", detallesViajeC);

    double valorMUlta = multa * pagados;

    Map conductorInfo = getDataUser(conductorId);

    double saldoC = Double.parseDouble(conductorInfo.get("saldo").toString());
    saldoC=saldoC-valorMUlta;
    BigDecimal dSaldo = new BigDecimal(saldoC).setScale(2, RoundingMode.HALF_UP);
    double nuevoSaldo =  dSaldo.doubleValue();

    condutorActualizar.put("saldo", nuevoSaldo);

    boolean r = updateDataUser(conductorId, condutorActualizar);

    Map plann = new HashMap();

    Date tiempoCan = new Date();
    plann.put("fechaCancelado", tiempoCan.getTime());
    plann.put("viaje", getViaje(viajeId));
    plann.put("idPlann", viajeId);
    plann.put("status", "conductorCancelo");
    plann.put("esConductor", true);
    plann.put("detallesViajeC", detallesViajeC);
    plann.put("conductorInfo", conductorInfo);

    boolean rPla = updatePlannHis(conductorId, viajeId, plann);




    Log.i(TAG,"FINAL " + String.valueOf(rPla));
    showNotification("Se cancelo tu viaje",
            "Devido que pasaron mas de 16 minutos y " +
                    " no iniciaste el viaje se procedio a cancelarlo y realizar " +
                    " la debolucion a los pasajaeros abordo y se " +
                    " aplico una multa hacia tu persona ", "MULTA");
    try {
      for (int i =0 ; i < pasajeros.size();i++){
        Map pa = (Map) pasajeros.get(i);
        String id =(String) pa.get("pasajeroId");
        if(!id.equals("null")){
          boolean result = sendNotification. sendNotification(id);
          Log.i(TAG,"SEND NOTIFICATION "+result);
        }
      }
    }catch (Exception e){
      Log.i(TAG,"ERRO NOTIFICA "+e.getMessage());

    }finally {
      WorkManager manager = WorkManager.getInstance(getApplicationContext());
      manager.cancelUniqueWork("ConductorCancelacion");
      manager.pruneWork();
    }

  }


  Map getViaje(String viajeId) throws ExecutionException, InterruptedException {
    TaskCompletionSource<Map> completionSource = new TaskCompletionSource<>();
    new Handler(Looper.getMainLooper()).post(() -> {
     reference.child(viajeId).get().addOnCompleteListener(runnable -> {
       completionSource.setResult((Map)runnable.getResult().getValue());
     }).addOnFailureListener(runnable -> {
       completionSource.setException(runnable);
     });
    });

    return Tasks.await(completionSource.getTask());
  }

  List reordenarPasajeros(List pasajeros) {
    List p = new ArrayList();
    for (Object pas : pasajeros) {
      Map pasajeroF = (Map) pas;
      pasajeroF.put("statusPuesto", "canceladoC");
      p.add(pasajeroF);
    }
    return p;
  }

  double calcularFeeTarjeta(double valor) {
    if (valor > 0) {
      return (valor * (1 - 0.955) + 0.24);
    } else {
      return 0;
    }
  }

  boolean updatePlannHis(String conductorId, String viajeId, Map data) throws ExecutionException, InterruptedException {
    TaskCompletionSource<Boolean> completionSource = new TaskCompletionSource<>();

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        plannHistoricos.child(conductorId).child("viajes")
                .child(viajeId)
                .updateChildren(data)
                .addOnCompleteListener(runnable -> {
                  completionSource.setResult(runnable.isSuccessful());
                })
                .addOnFailureListener(runnable -> {
                  completionSource.setException(runnable);
                });
      }
    });

    return Tasks.await(completionSource.getTask());
  }

  boolean updateViajesBd(String idViaje, Map data) throws ExecutionException, InterruptedException {
    TaskCompletionSource<Boolean> completionSource = new TaskCompletionSource<>();

    new Handler(Looper.getMainLooper()).post(() -> {
      reference.child(idViaje).updateChildren(data).addOnCompleteListener(runnable -> {
        completionSource.setResult(runnable.isSuccessful());
      }).addOnFailureListener(runnable -> {
        completionSource.setException(runnable);
      });
    });

    return Tasks.await(completionSource.getTask());
  }

  boolean updateDataUser(String idUser, Map data) throws ExecutionException, InterruptedException {
    TaskCompletionSource<Boolean> completionSource = new TaskCompletionSource<>();
    new Handler(Looper.getMainLooper()).post(() -> {
      userRef.child(idUser).updateChildren(data).addOnCompleteListener(runnable -> {
        completionSource.setResult(runnable.isSuccessful());
      }).addOnFailureListener(runnable -> {
        completionSource.setException(runnable);
      });
    });
    return Tasks.await(completionSource.getTask());
  }

  Map getDataUser(String idUser) throws ExecutionException, InterruptedException {
    TaskCompletionSource<Map> completionSource = new TaskCompletionSource<>();
    new Handler(Looper.getMainLooper())
            .post(new Runnable() {
              @Override
              public void run() {
                userRef.child(idUser).get().
                        addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                              completionSource.setResult((Map) task.getResult().getValue());
                            }
                          }
                        }).addOnFailureListener(runnable -> {
                          completionSource.setException(runnable);
                        });
              }
            });
    return Tasks.await(completionSource.getTask());
  }

  double saldoDevolver(Map params) {
    Map objetoPago = (Map) params.get("pago");
    double saldo = 0;
    double pagoComisionSaldo = Double.parseDouble(objetoPago.get("pagoComisionSaldo").toString());
    double valor = Double.parseDouble(params.get("valor").toString());
    double pagoTotal = Double.parseDouble(objetoPago.get("pagoTotal").toString());
    double pago = Double.parseDouble(objetoPago.get("pago").toString());
    double diferenciaTarjeta = 0;
    if (objetoPago.get("diferenciaTarjeta") != null) {
      diferenciaTarjeta = Double.parseDouble(objetoPago.get("diferenciaTarjeta").toString());
    }

    saldo = Double.parseDouble(params.get("saldo").toString())
            + (valor - pago) + pagoComisionSaldo + (pagoTotal) + diferenciaTarjeta;


    return saldo;
  }

  boolean esElMimsoUsuario(int index, List pasajeros) {
    boolean esMismo = false;

    if (index < pasajeros.size()) {
      String idPasajero = (String) ((Map) pasajeros.get(index)).get("pasajeroId");
      for (int i = index; i < pasajeros.size(); i++) {
        if (index != i) {
          String idP = (String) ((Map) pasajeros.get(i)).get("pasajeroId");
          if (idPasajero.equals(idP)) {
            esMismo = true;
          }
        }
      }
    }

    return esMismo;
  }



  void showNotification(String title, String message, String ticker) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Plann";
      // Create the channel for the notification
      NotificationChannel mChannel =
              new NotificationChannel("channel_01", name, NotificationManager.IMPORTANCE_DEFAULT);

      // Set the SendNotitications Channel for the SendNotitications Manager.
      mNotificationManager.createNotificationChannel(mChannel);
    }
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"channel_01")
//                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
//                        activityPendingIntent)

            .setContentText(message)
            .setContentTitle(title)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(ticker)
            .setSilent(false)
            .setWhen(System.currentTimeMillis());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      builder.setChannelId("channel_01"); // Channel ID
    }
    Notification notification = builder.build();
    mNotificationManager.notify(556, notification);
  }
}
