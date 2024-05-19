package ru.articus.artab;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    String URL = "";
    WebView webView;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        webView = findViewById(R.id.webView);
        SharedPreferences sPrefU = getSharedPreferences("url", MODE_PRIVATE);
        String _url = sPrefU.getString(SettingsActivity.APP_PREFERENCES_url, "");

        SharedPreferences sPrefT = getSharedPreferences("Token", MODE_PRIVATE);
        String _token = sPrefT.getString(SettingsActivity.APP_PREFERENCES_token, "");

        SharedPreferences sPrefO = getSharedPreferences("orientation", MODE_PRIVATE);
        Integer _orientation = sPrefO.getInt(SettingsActivity.APP_PREFERENCES_orientation, 0);

        switch (_orientation) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
        URL = _url;
        if (URL.trim().length() != 0) {
            if (_url.substring(_url.length() - 1).equals("/")) {
                URL = _url + "?token=" + _token;
            } else {
                URL = _url + "/?token=" + _token;
            }
        }

        webView.addJavascriptInterface(new JavaScriptInterface(this), "webView_Storage");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);

        if (!URL.equals("")) {
            webView.loadUrl(URL);
        } else {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Отсутствует URL", Toast.LENGTH_SHORT).show();
        }

        WebViewClient webViewClient = new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(getStateDataChanged())
                    deleteToken();
            }
        };
        webView.setWebViewClient(webViewClient);


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            openQuitDialog();
        }
    }

    public boolean getStateDataChanged(){
        SharedPreferences sPrefNR = getSharedPreferences("needReLogin",MODE_PRIVATE);
        return sPrefNR.getBoolean(SettingsActivity.APP_PREFERENCES_NeedReLogin, false);
    }

    public void deleteToken(){

        webView.loadUrl("javascript:localStorage.removeItem(\"token\");");
        JavaScriptInterface ji = new JavaScriptInterface(this);
        ji.removeItem("token");

        SharedPreferences sPrefNeedReLogin;
        sPrefNeedReLogin = getSharedPreferences("needReLogin", MODE_PRIVATE);
        SharedPreferences.Editor editorNR = sPrefNeedReLogin.edit();
        editorNR.putBoolean(SettingsActivity.APP_PREFERENCES_NeedReLogin, false);
        editorNR.commit();

    }

    private void openQuitDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из приложения?")
                .setNegativeButton("Нет", null)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    public class JavaScriptInterface {
        Context mContext;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Cursor cr;
        final char kv = (char) 34;
        Boolean isOld;

        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getAuthData() {
            final char kv = (char) 34;

            SharedPreferences sPrefAuthPass = getSharedPreferences("authPass",MODE_PRIVATE);
            String AuthPass = sPrefAuthPass.getString(SettingsActivity.APP_PREFERENCES_AuthPass, "");

            SharedPreferences sPrefAuthLogin = getSharedPreferences("authLogin",MODE_PRIVATE);
            String AuthLogin = sPrefAuthLogin.getString(SettingsActivity.APP_PREFERENCES_AuthLogin, "");

            return "{"+ kv +"login"+ kv +":" + kv + AuthLogin + kv +","+ kv +"password"+ kv +":" + kv + AuthPass + kv + "}";
        }

        @JavascriptInterface
        public void saveJson(String getJson, String nameKey) {
            cv.put(DBHelper.KEY_DATA, getJson);
            cv.put(DBHelper.KEY_NAME, nameKey);
            try {
                cr = db.rawQuery("SELECT * FROM mainTable WHERE name = " + kv  + nameKey + kv, null);
                isOld = cr.getCount() > 0;
            } catch (Exception e) {
                Log.e("SQLiteRawQuery", e.toString());
            }

            if (!isOld) {
                db.insert(DBHelper.MAIN_TABLE, null, cv);
            } else {
                db.update(DBHelper.MAIN_TABLE, cv, "name = " + kv + nameKey + kv, null);
            }
            cr.close();
        }

        @JavascriptInterface
        public String loadJson(String nameKey) {
            cr = db.query(DBHelper.MAIN_TABLE, null, "name = " + kv + nameKey + kv, null, null, null, null);
            cr.moveToFirst();
            if(cr.getCount() != 0) {
                return cr.getString(1);
            }
            else{return null;}

        }

        @JavascriptInterface
        public String loadString(String nameKey) {
            return loadJson(nameKey);
        }

        @JavascriptInterface
        public void saveString(String getString, String nameKey) {
            saveJson(getString, nameKey);
        }

        @JavascriptInterface
        public void saveBool(Boolean getBool ,String nameKey){
            if(getBool){
                saveJson("1", nameKey);
            }else {
                saveJson("0", nameKey);
            }
        }

        @JavascriptInterface
        public Boolean loadBool(String nameKey){
            String b =  loadJson(nameKey);
            if(b.equals("1")){
                return true;
            }else if (b.equals("0")){
                return false;
            }
            return null;
        }

        @JavascriptInterface
        public void removeItem(String item){
            if(item != null)
                db.delete(DBHelper.MAIN_TABLE, "name = " + kv + item + kv, null);
        }
    }
}