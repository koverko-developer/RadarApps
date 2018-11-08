package com.google.android.gms.samples.vision.ocrreader.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.samples.vision.ocrreader.Prefs;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.SendActivity;
import com.google.android.gms.samples.vision.ocrreader.model.NotificationFB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class NotificationServices extends IntentService {

    private static final int NOTIF_ID = 1234;
    NotificationManager nm;
    NotificationCompat.Builder builder;
    RemoteViews collapsedView;
    Notification notification;


    private static final String TAG = "NotificationService";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference notify = database.getReference("notify/value/");
    Prefs prefs;

    double lt, lg;

    public NotificationServices() {
        super("notificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "-------    create services");
        addChangeListener();
        addListenerLocation();
        prefs = new Prefs(getApplicationContext());
    }

    private void addListenerLocation() {

        LocationRequest request = new LocationRequest();
        request.setInterval(500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        client.requestLocationUpdates(request, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                if(location != null) {
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> addresses;

                    try{

                        lg = location.getLongitude();
                        lt = location.getLatitude();

                    }catch (Exception e){

                    }

                    Log.e("LOCATION = ", location.toString());
                }


            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        }, null);

    }

    private void addChangeListener() {

        notify.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                NotificationFB fb = dataSnapshot.getValue(NotificationFB.class);

                if(fb != null){

                    if(!prefs.getDate().equals("0")){
                        if(prefs.getTime().equals(fb.getTime())) return;
                        else sendNotify(fb);
                    }else sendNotify(fb);

                    prefs.setDate(fb.getDate());
                    prefs.setTime(fb.getTime());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendNotify(NotificationFB fb) {

        if(fb.getFrom_user().equals(prefs.getKey())) return;

        try{

            Location loc1 = new Location("");
            loc1.setLatitude(lt);
            loc1.setLongitude(lg);

            Location loc2 = new Location("");
            loc2.setLatitude(fb.getLt());
            loc2.setLongitude(fb.getLg());

            float destanceInMeters = loc1.distanceTo(loc2);
            Log.e("services", "destance = " + destanceInMeters);
            if(destanceInMeters > 3000) return;

        }catch (Exception e){
            return;
        }

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        collapsedView = new RemoteViews(getPackageName(), R.layout.nonification_);
        collapsedView.setTextViewText(R.id.n_number, fb.getReg_number());
        collapsedView.setTextViewText(R.id.n_type, fb.getText_type());
        collapsedView.setTextViewText(R.id.n_place, fb.getAddress());



        Log.e(TAG, "create notifi");
        String number = fb.getReg_number();
        Log.e("services", number);

        Intent intentNotif = new Intent(this, SendActivity.class);
        intentNotif.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentNotif.putExtra("date", fb.getDate());
        PendingIntent rightIntent = PendingIntent.getActivity(this, 0, intentNotif, PendingIntent.FLAG_UPDATE_CURRENT);
        //rightIntent.setAction("ok");
        //rightIntent.putExtra("date", fb.getDate());

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder = new NotificationCompat.Builder(this);
//                // these are the three things a NotificationCompat.Builder object requires at a minimum
                builder.setSmallIcon(R.drawable.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Новое нарушение")
                // notification will be dismissed when tapped
                .setAutoCancel(true)
                .setSound(alarmSound)
                // tapping notification will open MainActivity
                .setContentIntent(rightIntent)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setCustomContentView(collapsedView);
                // setting style to DecoratedCustomViewStyle() is necessary for custom views to display
                //.setStyle(new android.support.v7.app.NotificationCompat.DecoratedCustomViewStyle());

        // retrieves android.app.NotificationManager


        //nm.notify(0, builder.build());
        startForeground(NOTIF_ID, builder.build());
        collapsedView.setTextViewText(R.id.n_number, number);
        collapsedView.setTextViewText(R.id.n_type, fb.getText_type());
        collapsedView.setTextViewText(R.id.n_place, fb.getAddress());
        nm.notify(NOTIF_ID, builder.build());

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "-------    start command services");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public IBinder onBind(Intent arg0) {
        return null;
    }

}
