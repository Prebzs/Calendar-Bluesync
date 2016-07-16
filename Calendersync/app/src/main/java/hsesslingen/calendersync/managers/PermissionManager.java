package hsesslingen.calendersync.managers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

public class PermissionManager {

    public interface OnRequestPermissionsResultCallback {
        void onRequestPermissionsResult();
    }

    private static final String TAG = "PermissionManager";

    private static int requestCode = 0;
    private static SparseArray<OnRequestPermissionsResultCallback> requestPermissionCallbacks = new SparseArray<>();

    public static boolean hasPermission(Activity activity, String permission)
    {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, String permission, OnRequestPermissionsResultCallback callback)
    {
        ActivityCompat.requestPermissions(activity, new String[] {permission}, requestCode);
        requestPermissionCallbacks.append(requestCode++, callback);
    }

    public static void onRequestPermissionsResult(int requestCode, int[] grantResults)
    {

        OnRequestPermissionsResultCallback callback = requestPermissionCallbacks.get(requestCode);
        if(callback == null)
        {

            Log.e(TAG, "callback not found");
            return;
        }
        requestPermissionCallbacks.remove(requestCode);

        for(int result : grantResults)
        {
            if(result != PackageManager.PERMISSION_GRANTED)
            {
                Log.e(TAG, "permission was not granted");
                return;
            }
        }

        Log.d(TAG, "permission was granted, moving on");
        callback.onRequestPermissionsResult();
    }
}
