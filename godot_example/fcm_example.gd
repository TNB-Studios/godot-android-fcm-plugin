extends Node

var fcm_plugin = null
var device_token: String = ""

func _ready():
	_init_fcm()

func _init_fcm():
	if not Engine.has_singleton("FCMPlugin"):
		print("FCMPlugin singleton not found - not running on Android or plugin not registered")
		return

	fcm_plugin = Engine.get_singleton("FCMPlugin")
	fcm_plugin.connect("token_received", _on_token_received)

	# Try to get a cached token (may already be available)
	var cached_token = fcm_plugin.call("getToken")
	if cached_token and cached_token != "":
		print("Got cached FCM token: ", cached_token)
		device_token = cached_token
	else:
		print("No cached token yet - waiting for token_received signal")

func _on_token_received(token: String):
	print("FCM token received: ", token)
	device_token = token
	# Send this token to your backend server for push notification targeting
