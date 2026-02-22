package com.example.godotfcm;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessaging;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;
import java.util.Set;
import java.util.HashSet;

public class FCMPlugin extends GodotPlugin {
    private static final String TAG = "FCMPlugin";
    private static FCMPlugin instance = null;
    private static String cachedToken = null;

    public FCMPlugin(Godot godot) {
        super(godot);
        instance = this;
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
        return signals;
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
