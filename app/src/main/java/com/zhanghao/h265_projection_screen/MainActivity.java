package com.zhanghao.h265_projection_screen;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.zhanghao.h265_projection_screen.databinding.ActivityMainBinding;

/**
 * 此为推流端
 */
public class MainActivity extends AppCompatActivity {

    public static Context context;
    private ActivityMainBinding binding;

    private MediaProjectionManager mediaProjectionManager;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent serviceIntent = new Intent(MainActivity.this, ScreenRecordService.class);
                serviceIntent.putExtra("activityResult", result);
                startService(serviceIntent);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        activityResultLauncher.launch(screenCaptureIntent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ScreenRecordService.class));
    }
}