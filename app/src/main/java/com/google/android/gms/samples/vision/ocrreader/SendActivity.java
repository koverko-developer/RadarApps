package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.samples.vision.ocrreader.contract.SendContract;
import com.google.android.gms.samples.vision.ocrreader.model.NotificationFB;
import com.google.android.gms.samples.vision.ocrreader.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SendActivity extends AppCompatActivity implements SendContract.View {

    ImageView photo;
    Spinner _sp;
    TextView tv_reg_n, tv_date, tv_time, tv_place;
    Button btn;

    Calendar dateAndTime;
    SimpleDateFormat df;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference notify;
    DatabaseReference user;
    String base_64;
    Bitmap bitmap;

    double lt;
    double lg;

    Prefs prefs;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notify = database.getReference("notify/value/");

        Intent intent = getIntent();
        String n = intent.getStringExtra("date");
        base_64 = intent.getStringExtra("img");
        bitmap = (Bitmap) intent.getParcelableExtra("bitmap");
        prefs = new Prefs(this);

        initUI();
        checkPermission();
        addUserListener();
        if (n == null) generateData();
        else getData();

    }

    private void addUserListener(){
        user = database.getReference("u/");
        user.child(prefs.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null){
                    if(user.getBase_64() != null){
                        byte[] decoded = Base64.decode(user.getBase_64(), Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        photo.setImageBitmap(b);
                        photo.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("WrongConstant")
    @Override
    public void initUI() {
        photo = (ImageView) findViewById(R.id.img_photo);
        _sp = (Spinner) findViewById(R.id._sp);

        String[] arr = getResources().getStringArray(R.array.arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinne, arr);
        _sp.setAdapter(adapter);
        _sp.setSelection(0);

        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_reg_n = (TextView) findViewById(R.id.tv_reg_number);
        tv_place = (TextView) findViewById(R.id.tv_place);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        if(bitmap != null) {
            byte[] decoded = Base64.decode(base_64, Base64.DEFAULT);
            Bitmap b = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            photo.setImageBitmap(bitmap);
        }
        else photo.setVisibility(View.GONE);
    }

    @Override
    public void sendData() {

        NotificationFB notificationFB = new NotificationFB();
        notificationFB.setAddress(tv_place.getText().toString());
        notificationFB.setDate(tv_date.getText().toString());
        notificationFB.setTime(tv_time.getText().toString());
        notificationFB.setType(_sp.getSelectedItemPosition());
        notificationFB.setText_type(_sp.getSelectedItem().toString());
        notificationFB.setLg(lg);
        notificationFB.setLt(lt);
        notificationFB.setReg_number(tv_reg_n.getText().toString());
        notificationFB.setFrom_user(prefs.getKey());
        if(base_64 != null) notificationFB.setBase64(base_64);

        notify.setValue(notificationFB).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),
                        "Отправлено!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void getData() {

        btn.setVisibility(View.GONE);
        Log.e("Send ", "get date");

        notify.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                NotificationFB fb = dataSnapshot.getValue(NotificationFB.class);

                if(fb != null){

                    if(fb.getDate() != null) tv_date.setText(fb.getDate());
                    if(fb.getTime() != null) tv_time.setText(fb.getTime());
                    if (fb.getReg_number() != null) tv_reg_n.setText(fb.getReg_number());
                    if(fb.getAddress() != null) tv_place.setText(fb.getAddress());
                    _sp.setSelection(fb.getType());
                    if(fb.getBase64() != null){
                        photo.setVisibility(View.VISIBLE);
                        byte[] decoded = Base64.decode(fb.getBase64(), Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        photo.setImageBitmap(b);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void generateData() {
        Intent intent = getIntent();
        final String reg_n = intent.getStringExtra("reg_n");

        dateAndTime = Calendar.getInstance();
        long t = dateAndTime.getTimeInMillis();
        df = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault());
        tv_date.setText(df.format(t).split(" ")[0]);
        tv_time.setText(df.format(t).split(" ")[1]);
        if (reg_n != null) tv_reg_n.setText(reg_n);

        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

                        addresses = gcd.getFromLocation(location.getLatitude(),
                                location.getLongitude(), 1);
                        if(addresses.size() > 0){

                            Log.e("MY AddressLine1 = ", addresses.get(0).getFeatureName());

                            Log.e("MY Country = ", addresses.get(0).getCountryName());

                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();

                            String result = "";

                            if(knownName != null) result = result + knownName + ", ";
                            if(postalCode != null) result = result + postalCode + ", ";
                            if(state != null) result = result + state + ", ";
                            if(city != null) result = result + city + ", ";

                            tv_place.setText(result);
                        }

                    }catch (Exception e){

                    }

                    Log.e("LOCATION = ", location.toString());
                }


            }
        }, null);

    }

    @Override
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && photo != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SendActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 199);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if( id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
