package com.google.android.gms.samples.vision.ocrreader.contract;

public class SendContract {

    public interface View{

        void initUI();
        void sendData();
        void getData();
        void generateData();
        void checkPermission();

    }

}
