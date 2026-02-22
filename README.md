# Godot Android FCM Plugin

A lightweight Android plugin for [Godot Engine](https://godotengine.org/) (4.x) that retrieves Firebase Cloud Messaging (FCM) device tokens. This allows your Godot game to register for push notifications on Android.

## What It Does

- Registers a `FCMPlugin` singleton accessible from GDScript and C#
- Fetches the FCM device token on startup
- Emits a `token_received` signal when a new token is available
- Handles token refresh via `FCMService`

## Prerequisites

- Godot 4.x with Android export templates
- A Firebase project with your Android app registered
- Your `google-services.json` file from the Firebase Console

## Setup

### 1. Copy Java Files

Copy `src/FCMPlugin.java` and `src/FCMService.java` into your Godot project's Android build source directory:

```
your-project/android/build/src/com/yourcompany/yourgame/
```

The path after `src/` must match your Android application's package name with dots replaced by directory separators.

### 2. Change the Package Name

Open both Java files and change the first line from:

```java
package com.example.godotfcm;
```

to your actual package name:

```java
package com.yourcompany.yourgame;
```

### 3. Edit AndroidManifest.xml

Add the following inside your `<application>` tag (see `examples/AndroidManifest_snippet.xml`):

```xml
<service
    android:name="com.yourcompany.yourgame.FCMService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<meta-data
    android:name="org.godotengine.plugin.v2.FCMPlugin"
    android:value="com.yourcompany.yourgame.FCMPlugin" />
```

Replace `com.yourcompany.yourgame` with your actual package name.

### 4. Edit settings.gradle

Add the Google Services plugin version to your `pluginManagement > plugins` block:

```gradle
pluginManagement {
    plugins {
        // ... existing plugins ...
        id 'com.google.gms.google-services' version '4.4.0'
    }
}
```

### 5. Edit build.gradle

Add the Google Services plugin and Firebase Messaging dependency:

```gradle
plugins {
    // ... existing plugins ...
    id 'com.google.gms.google-services'
}

dependencies {
    // ... existing dependencies ...
    implementation "com.google.firebase:firebase-messaging:24.0.0"
}
```

### 6. Add google-services.json

Place your `google-services.json` file (downloaded from the Firebase Console) in:

```
your-project/android/build/
```

## Usage

### GDScript

```gdscript
var fcm_plugin = null
var device_token: String = ""

func _ready():
    if Engine.has_singleton("FCMPlugin"):
        fcm_plugin = Engine.get_singleton("FCMPlugin")
        fcm_plugin.connect("token_received", _on_token_received)

        var cached_token = fcm_plugin.call("getToken")
        if cached_token and cached_token != "":
            device_token = cached_token

func _on_token_received(token: String):
    device_token = token
    # Send token to your backend
```

### C#

```csharp
#if GODOT_ANDROID
private GodotObject _fcmPlugin = null;
#endif
private string _deviceToken = "";

public override void _Ready()
{
#if GODOT_ANDROID
    if (Engine.HasSingleton("FCMPlugin"))
    {
        _fcmPlugin = Engine.GetSingleton("FCMPlugin");
        _fcmPlugin.Connect("token_received", Callable.From<string>((token) => {
            _deviceToken = token;
            // Send token to your backend
        }));

        string cachedToken = (string)_fcmPlugin.Call("getToken");
        if (!string.IsNullOrEmpty(cachedToken))
            _deviceToken = cachedToken;
    }
#endif
}
```

See `godot_example/` for complete examples.

## API Reference

### Singleton: `FCMPlugin`

| Method | Returns | Description |
|---|---|---|
| `getToken()` | `String` | Returns the cached FCM token, or `null` if not yet available |
| `fetchToken()` | `void` | Manually triggers a token fetch (called automatically on init) |

### Signal: `token_received`

| Parameter | Type | Description |
|---|---|---|
| `token` | `String` | The FCM device token |

Emitted whenever a new token is received, either from the initial fetch or a token refresh.

## Optional: BrainCloud Integration

If you're using [BrainCloud](https://getbraincloud.com/) as your backend, you can register the FCM token for push notifications:

```csharp
// After receiving the token
bcWrapper.PushNotificationService.RegisterPushNotificationDeviceToken(
    Platform.GooglePlayAndroid,
    deviceToken,
    onSuccess,
    onFailure
);
```

## How It Works

Godot's Android build compiles Java sources from `android/build/src/`. The `meta-data` entry in the manifest tells Godot to load `FCMPlugin` as a plugin singleton. On initialization, `FCMPlugin` requests the FCM token from Firebase. When the token arrives (or is refreshed via `FCMService`), it's cached and emitted via the `token_received` signal.

## License

MIT License - see [LICENSE](LICENSE) for details.
