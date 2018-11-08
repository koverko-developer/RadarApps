package com.google.android.gms.samples.vision.ocrreader.parse;

import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseNumberStr {

    public static boolean isValid(String str){

        ////  -----   УКРАИНА   -------   BH 2569 EO  -----
        ////  -----   СЛОВАКИЯ  -------   KA-999AA    -----
        ////  -----   ПОЛЬША  ---------   XYZ 1J34    -----
        ////  -----   ВЕНГРИЯ  --------   KKD-006     -----


        boolean b = false;

        // убераем все лишние символы и оставляем только голую строку
        str = str.replaceAll("-", "");
        str = str.replaceAll(" ", "");

        /*     из примеров видим, что min дина строки должна быть 6 символов
               а максимальная - 8
               делаеи проверку
        */

        try{

            if(str.length() > 8 || str.length()  < 6  ) return false;

            if(isUA(str)) return true;
            else if(isSK(str)) return true;
            else if(isPL(str)) return true;
            else if(isH(str)) return true;
            else return false;
//
//            Pattern r_1 = Pattern.compile("[A-Z0-9]");
//            Matcher m_1 = r_1.matcher(str);
//
//            if(!m_1.find()) return false;
//            else return true;

        }catch (Exception e){
            return false;
        }

    }

    public static boolean isUA(String str){

        try{

            Pattern r = Pattern.compile("[A-Z]{2}[0-9]{4}[A-Z]{2}");
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "UA");
                return true;
            }



            /*

            // проверка на первых два буквенных символва и с большой буквы
            String radar_1 = str.substring(0,2);
            Pattern r_1 = Pattern.compile("[A-Z]{2}");
            Matcher m_1 = r_1.matcher(radar_1);
            if(!m_1.find() || radar_1.length() != 2) return false;

            // проверка на следующих 4 сивмола цифровых
            String radar_2 = str.substring(2,6);
            Pattern r_2 = Pattern.compile("[0-9]{4}");
            Matcher m_2 = r_2.matcher(radar_2);

            if(!m_2.find() || radar_2.length() != 4) return false;

            // проверка на последних два буквенных символва и с большой буквы
            String radar_3 = str.substring(6,8);
            Pattern r_3 = Pattern.compile("[A-Z]{2}");
            Matcher m_3 = r_3.matcher(radar_3);
            if(!m_3.find() || radar_3.length() != 2) return false;

            Log.e("IS UA radar_1 = " , radar_2);
            return true;

            */
        }catch (Exception e){
            return false;
        }

    }

    public static boolean isSK(String str){

        try{

            Pattern r = Pattern.compile("[A-Z]{2}[0-9]{3}[A-Z]{2}");
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "SK");
                return true;
            }
            /*
            // проверка на первых два буквенных символва и с большой буквы

            String radar_1 = str.substring(0,2);
            Pattern r_1 = Pattern.compile("[A-Z]{2}");
            Matcher m_1 = r_1.matcher(radar_1);
            Log.e("IS SK radar_1 = " , radar_1);
            if(!m_1.find() || radar_1.length() != 2) return false;

            // проверка на следующих 3 сивмола цифровых
            String radar_2 = str.substring(2,5);
            Pattern r_2 = Pattern.compile("[0-9]{3}");
            Matcher m_2 = r_2.matcher(radar_2);
            Log.e("IS SK radar_1 = " , radar_2);
            if(!m_2.find() || radar_2.length() != 4) return false;

            // проверка на последних два буквенных символва и с большой буквы
            String radar_3 = str.substring(5,7);
            Pattern r_3 = Pattern.compile("[A-Z]{2}");
            Matcher m_3 = r_3.matcher(radar_3);
            if(!m_3.find() || radar_3.length() != 2) return false;
            Log.e("IS SK radar_1 = " , radar_1 + radar_2+ radar_3);

            return true;
            */

        }catch (Exception e){
            return false;
        }

    }

    public static boolean isPL(String str){

        try{

            Pattern r = Pattern.compile("[A-Z]{2,3}[0-9A-Z]{4,5}");
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "PL");
                return true;
            }


            // проверка на первых два буквенных символва и с большой буквы

            /*String radar_1 = str.substring(0,2);
            Pattern r_1 = Pattern.compile("[A-Z]{2,3}[A-Z0-9]{4,5}");
            Matcher m_1 = r_1.matcher(radar_1);
            Log.e("IS SK radar_1 = " , radar_1);
            if(!m_1.find() || radar_1.length() != 2) return false;

            // проверка на следующих 3 сивмола цифровых
            String radar_2 = str.substring(2,5);
            Pattern r_2 = Pattern.compile("[0-9]{3}");
            Matcher m_2 = r_2.matcher(radar_2);
            Log.e("IS SK radar_1 = " , radar_2);
            if(!m_2.find() || radar_2.length() != 4) return false;

            // проверка на последних два буквенных символва и с большой буквы
            String radar_3 = str.substring(5,7);
            Pattern r_3 = Pattern.compile("[A-Z]{2}");
            Matcher m_3 = r_3.matcher(radar_3);
            if(!m_3.find() || radar_3.length() != 2) return false;
            Log.e("IS SK radar_1 = " , radar_1 + radar_2+ radar_3);

            return true;
            */

        }catch (Exception e){
            return false;
        }

    }

    public static boolean isH(String str){

        try{

            Pattern r = Pattern.compile("[A-Z]{3}[0-9]{3}");
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "H");
                return true;
            }




        }catch (Exception e){
            return false;
        }

    }

}


//    String[] arr = item.getValue().split(" ");
//
//
//
//    String radar_1 = arr[0];
//    Pattern r_1 = Pattern.compile("[A-Z]");
//    Matcher m_1 = r_1.matcher(radar_1);
//
//                    if(!m_1.find() || radar_1.length() != 2) {return;}
//
//
//                            String radar_2 = arr[1];
//                            Pattern r_2 = Pattern.compile("[0-9]");
//                            Matcher m_2 = r_2.matcher(radar_2);
//
//                            if(!m_2.find() || radar_2.length() != 4) {return;}
//
//
//
//                            String radar_3 = arr[2];
//                            Pattern r_3 = Pattern.compile("[A-Z]");
//                            Matcher m_3 = r_3.matcher(radar_3);
//
//                            if(!m_3.find() || radar_3.length() != 2) {return;}