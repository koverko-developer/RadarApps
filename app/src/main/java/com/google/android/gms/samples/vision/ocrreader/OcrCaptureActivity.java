/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.clans.fab.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.ocrreader.model.NotificationFB;
import com.google.android.gms.samples.vision.ocrreader.model.User;
import com.google.android.gms.samples.vision.ocrreader.services.NotificationServices;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.github.clans.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity {

    private User inSelfUser = null;
    List<User> list = new ArrayList<>();
    Prefs prefs;

    Intent mIntent;

    Context context;

    boolean isLight = false;
    RelativeLayout rel;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference users = database.getReference("u/");

    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private String reg_number_str = "";

    FloatingActionButton floatingActionButton1, floatingActionButton2;

    TextRecognizer textRecognizer;
    boolean isShowDialog = false;

    RelativeLayout rel_container, rel_circle;
    LinearLayout top_l;


    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ocr_capture);

        prefs = new Prefs(this);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
        final boolean autoFocus = true;
        final boolean useFlash = isLight;


        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }
        context = this;


        rel_circle = (RelativeLayout) findViewById(R.id.rel_circle);
        rel_container = (RelativeLayout) findViewById(R.id.rel_container);
        rel_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHide(rel_container);
            }
        });

        top_l = (LinearLayout) findViewById(R.id.topLayout);
        rel = (RelativeLayout) findViewById(R.id.rel);

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // если фонарик включен, то выключаем, меняем иконку

                isLight = !isLight;
                createCameraSource(true, isLight);
                startCameraSource();

                if(isLight) {

                    floatingActionButton1.setImageResource(R.drawable.ic_lightbulb_outline_black_24dp);

                }else {
                    floatingActionButton1.setImageResource(R.drawable.ic_highlight_black_24dp);
                }

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                swowDialogEditRegNumber();

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && rel_circle != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OcrCaptureActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 199);
        }

        chechAuth();
        mIntent = new Intent(this, NotificationServices.class);
        startService(mIntent);

    }

    private void chechAuth() {

        if(prefs.getKey().equals("0")){
            addValueEventLisnener();
            rel_circle.setEnabled(false);
            showDialogAuth();
        }else addCurrentUserListener();
    }

    private void addCurrentUserListener() {

        users.child(prefs.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                if(user != null) inSelfUser = user;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showDialogAuth() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialogEdit = new Dialog(OcrCaptureActivity.this);
                //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogEdit.setContentView(R.layout.dialog_auth);

                final EditText et_login = (EditText) dialogEdit.findViewById(R.id.et_login);
                final EditText et_password = (EditText) dialogEdit.findViewById(R.id.et_password);


                final Button btn = (Button) dialogEdit.findViewById(R.id.btn_auth);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String login_s = et_login.getText().toString();
                        String pass_s = et_password.getText().toString();
                        pass_s = md5(pass_s);


                        if(list != null){
                            for (User u: list
                                 ) {

                                if(u.getEmail().equals(login_s) &&
                                        u.getPassword().equals(pass_s)){

                                    prefs.setKey(u.getKey());
                                    rel_circle.setEnabled(true);
                                    dialogEdit.dismiss();
                                    addCurrentUserListener();
                                    return;

                                }

                            }

                            Toast.makeText(OcrCaptureActivity.this,
                                    "Ytdthysq логин или пароль...", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                TextView tv_reg = (TextView) dialogEdit.findViewById(R.id.tv_reg);
                tv_reg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                        showDialogReg();
                    }
                });

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialogEdit.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogEdit.setCancelable(false);
                dialogEdit.show();
                dialogEdit.getWindow().setAttributes(lp);
            }
        });

    }

    private void showDialogReg() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialogEdit = new Dialog(OcrCaptureActivity.this);
                //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogEdit.setContentView(R.layout.dialog_reg);

                final EditText et_login = (EditText) dialogEdit.findViewById(R.id.et_login);
                final EditText et_password = (EditText) dialogEdit.findViewById(R.id.et_pass);
                final EditText et_fio = (EditText) dialogEdit.findViewById(R.id.et_fio);
                final EditText et_phone = (EditText) dialogEdit.findViewById(R.id.et_phone);

                final Button btn = (Button) dialogEdit.findViewById(R.id.btn_reg);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String login_s = et_login.getText().toString();
                        String pass_s = et_password.getText().toString();
                        String fio_s = et_fio.getText().toString();
                        String phone_s = et_phone.getText().toString();

                        if(login_s.isEmpty()) {
                            Toast.makeText(OcrCaptureActivity.this,
                                    "Введите логин", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(pass_s.isEmpty()) {
                            Toast.makeText(OcrCaptureActivity.this,
                                    "Введите пароль", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(fio_s.isEmpty()) {
                            Toast.makeText(OcrCaptureActivity.this,
                                    "Введите ФИО", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(phone_s.isEmpty()) {
                            Toast.makeText(OcrCaptureActivity.this,
                                    "Введите телефон", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(list != null ){

                            for (User us:list
                                 ) {

                                if(us.getEmail().equals(login_s)) {
                                    Toast.makeText(OcrCaptureActivity.this,
                                            "Логин уже существует...", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            }

                        }

                        User user = new User();
                        user.setEmail(login_s);
                        user.setFio(fio_s);
                        user.setPhone(phone_s);
                        user.setPassword(md5(pass_s));

                        String key = users.push().getKey();
                        user.setKey(key);
                        btn.setEnabled(false);
                        users.child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(OcrCaptureActivity.this,
                                            "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                    dialogEdit.dismiss();
                                    showDialogAuth();

                                }else {
                                    Toast.makeText(OcrCaptureActivity.this,
                                            "Ошибка регистрации...", Toast.LENGTH_SHORT).show();
                                }
                                btn.setEnabled(true);
                            }

                        });
                    }
                });

                TextView tv_auth = (TextView) dialogEdit.findViewById(R.id.tv_auth);

                tv_auth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                        chechAuth();
                    }
                });

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialogEdit.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogEdit.setCancelable(false);
                dialogEdit.show();
                dialogEdit.getWindow().setAttributes(lp);
            }
        });

    }

    public void swowDialogEditRegNumber() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialogEdit = new Dialog(OcrCaptureActivity.this);
                //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogEdit.setContentView(R.layout.dialog_edit);

                TextView tv_cancel = (TextView) dialogEdit.findViewById(R.id.tv_close);
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });



                final EditText text = (EditText) dialogEdit.findViewById(R.id.tv_number);
                text.setText(reg_number_str);

                TextView tv_ok = (TextView) dialogEdit.findViewById(R.id.tv_ok);
                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reg_number_str = text.getText().toString();
                        dialogEdit.dismiss();
                        startSendAct(null);

                    }
                });

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialogEdit.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogEdit.setCancelable(false);
                dialogEdit.show();
                dialogEdit.getWindow().setAttributes(lp);
            }
        });

    }

    public void showDialogInfo(){

        if(inSelfUser != null){
            inSelfUser.setBase_64("");
            users.child(prefs.getKey()).setValue(inSelfUser);
        }

        isShowDialog = true;
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCameraSource.isRec = true;
                    //final Bitmap b = null;
                    Bitmap b = mCameraSource.getBitmap();

                    int rotation = getWindowManager().getDefaultDisplay().getRotation();

                    if(rotation == 0) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        b = Bitmap.createBitmap(b, 0,0, b.getWidth(),
                                b.getHeight(), matrix, true);
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    final String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

                    final Dialog dialogInfo = new Dialog(OcrCaptureActivity.this);
                    //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    dialogInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogInfo.setContentView(R.layout.dialog_info);

                    TextView tv_cancel = (TextView) dialogInfo.findViewById(R.id.tv_close);
                    tv_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isShowDialog = false;
                            dialogInfo.dismiss();
                        }
                    });

                    TextView tv_ok = (TextView) dialogInfo.findViewById(R.id.tv_ok);
                    tv_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            startSendAct(encoded);
                            dialogInfo.dismiss();
                            isShowDialog = false;
                        }
                    });

                    TextView tv_edit = (TextView) dialogInfo.findViewById(R.id.tv_edit);
                    tv_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogInfo.dismiss();
                            isShowDialog = false;
                            swowDialogEditRegNumber();
                        }
                    });

                    TextView tv_reg_n = (TextView) dialogInfo.findViewById(R.id.tv_reg_number);
                    tv_reg_n.setText(reg_number_str);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialogInfo.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    dialogInfo.setCancelable(false);
                    dialogInfo.show();
                    dialogInfo.getWindow().setAttributes(lp);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkDialogInfo(){

         return isShowDialog;
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }


        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay, OcrCaptureActivity.this));

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .build();
    }
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, " --------- destroy intent");
        if (mPreview != null) {
            mPreview.release();
        }
        if(mIntent != null){
            Intent broadcast = new Intent("RestartServices");
            sendBroadcast(broadcast);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
    private void startCameraSource() throws SecurityException {

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Intent data = new Intent();
                data.putExtra(TextBlockObject, text.getValue());
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    public void setRegNumber(String str){
        this.reg_number_str = str;
    }

    public void animHide(final View v){

        AlphaAnimation scaleAnimation = null;

        scaleAnimation = new AlphaAnimation(1.0f, 0.0f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(500);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(scaleAnimation);
    }

    private void startSendAct(String bitmap){
        try {
            Intent intent = new Intent(OcrCaptureActivity.this, SendActivity.class);
            intent.putExtra("reg_n", reg_number_str);
            //intent.putExtra("img", encode);
            //intent.putExtra("bitmap", bitmap);
            if(inSelfUser != null) {
                inSelfUser.setBase_64(bitmap);
                users.child(prefs.getKey()).setValue(inSelfUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) Log.e("image", "success");
                    }
                });
            }
            if(!reg_number_str.isEmpty())startActivity(intent);
            else Toast.makeText(OcrCaptureActivity.this, "Гос. номер не может быть пустым...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmap(final View top_l_){

        Bitmap b = null;
//        top_l_.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
//                RelativeLayout.LayoutParams.MATCH_PARENT));
//        top_l_.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        top_l_.layout(0,0 ,top_l_.getMeasuredWidth(), top_l_.getMeasuredHeight());
//        b = Bitmap.createBitmap(800,600,
//                Bitmap.Config.ARGB_8888);
//        Canvas c = new Canvas(b);
//        top_l_.layout(top_l_.getLeft(), top_l_.getTop(), top_l_.getRight(), top_l_.getBottom());
//        top_l_.draw(c);



        return b;
    }

    public void addValueEventLisnener(){

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot dt: dataSnapshot.getChildren()
                     ) {

                    User user = dt.getValue(User.class);
                    if(user != null) list.add(user);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static final String md5(final String s){
        final String MD5 = "MD5";
        try{

            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDiest[] = digest.digest();

            StringBuilder hasString = new StringBuilder();

            for(byte aMessageDiest : messageDiest){

                String h = Integer.toHexString(0xff & aMessageDiest);

                while (h.length() < 2)
                    h = "0" + h;
                hasString.append(h);
            }

            return hasString.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

}
