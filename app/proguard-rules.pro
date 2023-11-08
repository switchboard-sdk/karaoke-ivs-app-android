# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn okhttp3.Call
-dontwarn okhttp3.Callback
-dontwarn okhttp3.Dispatcher
-dontwarn okhttp3.Headers
-dontwarn okhttp3.HttpUrl
-dontwarn okhttp3.MediaType
-dontwarn okhttp3.OkHttpClient$Builder
-dontwarn okhttp3.OkHttpClient
-dontwarn okhttp3.Protocol
-dontwarn okhttp3.Request$Builder
-dontwarn okhttp3.Request
-dontwarn okhttp3.RequestBody
-dontwarn okhttp3.Response
-dontwarn okhttp3.ResponseBody
-dontwarn org.chromium.net.CronetEngine$Builder
-dontwarn org.chromium.net.CronetEngine
-dontwarn org.chromium.net.CronetException
-dontwarn org.chromium.net.UploadDataProvider
-dontwarn org.chromium.net.UploadDataProviders
-dontwarn org.chromium.net.UrlRequest$Builder
-dontwarn org.chromium.net.UrlRequest$Callback
-dontwarn org.chromium.net.UrlRequest
-dontwarn org.chromium.net.UrlResponseInfo
-dontwarn org.webrtc.Dav1dDecoder