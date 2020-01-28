package com.calliek.kalman;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class Permissions {
    //массив строк с нужными разрешениями которые нужно запрашивать у пользователя
    private static final String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    //метод проверки есть ли все разрешения из массива разрешений
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                //если разрешение не получено приложением вернуть false
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //метод который спрашивает разрешения
    //Пример вызова из Activity
    //Permissions.requestPermissions(this);
    public static void requestPermissions(Activity activity) {
        if(!hasPermissions(activity,PERMISSIONS)) ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
    }
}
