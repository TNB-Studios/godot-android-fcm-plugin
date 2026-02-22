using Godot;

public partial class FcmExample : Node
{
#if GODOT_ANDROID
	private GodotObject _fcmPlugin = null;
#endif
	private string _deviceToken = "";

	public override void _Ready()
	{
		InitFcm();
	}

	private void InitFcm()
	{
#if GODOT_ANDROID
		if (!Engine.HasSingleton("FCMPlugin"))
		{
			GD.Print("FCMPlugin singleton not found - not running on Android or plugin not registered");
			return;
		}

		_fcmPlugin = Engine.GetSingleton("FCMPlugin");
		_fcmPlugin.Connect("token_received", Callable.From<string>((token) => {
			GD.Print($"FCM token received: {token}");
			_deviceToken = token;
			// Send this token to your backend server for push notification targeting
		}));

		string cachedToken = (string)_fcmPlugin.Call("getToken");
		if (!string.IsNullOrEmpty(cachedToken))
		{
			GD.Print($"Got cached FCM token: {cachedToken}");
			_deviceToken = cachedToken;
		}
		else
		{
			GD.Print("No cached token yet - waiting for token_received signal");
		}
#else
		GD.Print("FCMPlugin is only available on Android");
#endif
	}
}
