package com.example.godotfcm;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;
import java.util.Set;
import java.util.HashSet;

public class FCMPlugin extends GodotPlugin {
    private static final String TAG = "FCMPlugin";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;
    private static FCMPlugin instance = null;
    private static String cachedToken = null;

    public FCMPlugin(Godot godot) {
        super(godot);
        instance = this;
        requestNotificationPermission();
        fetchToken();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "FCMPlugin";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("token_received", String.class));
        signals.add(new SignalInfo("permission_granted"));
        signals.add(new SignalInfo("permission_denied"));
        return signals;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Pre-Android 13: no runtime permission needed, notifications allowed by default
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            Log.w(TAG, "Activity not available, cannot request notification permission");
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Notification permission already granted");
            return;
        }

        Log.d(TAG, "Requesting POST_NOTIFICATIONS permission");
        ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.POST_NOTIFICATIONS }, NOTIFICATION_PERMISSION_REQUEST_CODE);
    }

    @UsedByGodot
    public void requestPermission() {
        requestNotificationPermission();
    }

    @Override
    public void onMainRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                emitSignal("permission_granted");
            } else {
                Log.d(TAG, "Notification permission denied");
                emitSignal("permission_denied");
            }
        }
    }

    public static void setToken(String token) {
        cachedToken = token;
        if (instance != null) {
            instance.emitSignal("token_received", token);
        }
    }

    @UsedByGodot
    public String getToken() {
        return cachedToken;
    }

    @UsedByGodot
    public void fetchToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.d(TAG, "FCM Token: " + token);
                setToken(token);
            });
    }
}
