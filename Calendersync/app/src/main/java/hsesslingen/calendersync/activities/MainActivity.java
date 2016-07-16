package hsesslingen.calendersync.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import hsesslingen.calendersync.R;
import hsesslingen.calendersync.backend.CalendarViewModelFactory;
import hsesslingen.calendersync.managers.PermissionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "DEBUG onCreate()");
        CalendarViewModelFactory.setActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "DEBUG onStart()");

        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "DEBUG onResume()");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }
}
