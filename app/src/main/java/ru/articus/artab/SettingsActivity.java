package ru.articus.artab;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener
{
    String password = "";
    public SharedPreferences sPrefURL;
    public SharedPreferences sPrefPass;
    public SharedPreferences sPrefToken;
    public SharedPreferences sPrefOrientation;
    public SharedPreferences sPrefAuthPass;
    public SharedPreferences sPrefAuthLogin;
    public SharedPreferences sPrefNeedReLogin;
    EditText edURL;
    EditText edToken;
    EditText edAuthPass;
    EditText edAuthLogin;
    Spinner listOrientation;
    public static final String APP_PREFERENCES_url = "URL_set";
    public static final String APP_PREFERENCES_pass = "Password_set";
    public static final String APP_PREFERENCES_token = "Token_set";
    public static final String APP_PREFERENCES_orientation = "Ori_set";
    public static final String APP_PREFERENCES_AuthPass = "AuthPass_set";
    public static final String APP_PREFERENCES_AuthLogin = "AuthLogin_set";
    public static final String APP_PREFERENCES_NeedReLogin = "NeedReLogin_set";
    Button btnSave;
    Button btnCngPass;
    Button btnAuthConnect;
    Button btnOpenAuthConnect;
    DBHelper dbHelper;

    private String authLogin;
    private String authPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        edURL = findViewById(R.id.URLRes);
        edToken = findViewById(R.id.TokenRes);

        btnSave = (Button) findViewById(R.id.btnSaveSett);
        btnSave.setOnClickListener(this);

        btnAuthConnect = (Button) findViewById(R.id.btnAuthConnect);
        btnAuthConnect.setOnClickListener(this);

//        btnOpenAuthConnect = (Button) findViewById(R.id.btnOpenAuthConnect);
//        btnOpenAuthConnect.setOnClickListener(this);

        listOrientation = (Spinner) findViewById(R.id.ListOrientation);

        btnCngPass = (Button) findViewById(R.id.btnChgPass);
        btnCngPass.setOnClickListener(this);

//        edAuthLogin = findViewById(R.id.authLogin);
//        edAuthPass = findViewById(R.id.authPass);

        dbHelper = new DBHelper(this);

        LoadSettings();
        if(!password.equals(""))
        {
            DialogPass();
        }
    }

    public void SaveSettings()
    {
        try {
            sPrefURL = getSharedPreferences("url",MODE_PRIVATE);
            SharedPreferences.Editor editor = sPrefURL.edit();
            editor.putString(APP_PREFERENCES_url, edURL.getText().toString());
            editor.commit();

            sPrefOrientation = getSharedPreferences("orientation",MODE_PRIVATE);
            SharedPreferences.Editor editorO = sPrefOrientation.edit();
            editorO.putInt(APP_PREFERENCES_orientation, listOrientation.getSelectedItemPosition());
            editorO.commit();

            sPrefToken = getSharedPreferences("Token",MODE_PRIVATE);
            SharedPreferences.Editor editorT = sPrefToken.edit();
            editorT.putString(APP_PREFERENCES_token, edToken.getText().toString());
            editorT.commit();

            sPrefPass = getSharedPreferences("pass",MODE_PRIVATE);
            SharedPreferences.Editor editorU = sPrefPass.edit();
            editorU.putString(APP_PREFERENCES_pass, password);
            editorU.commit();

            sPrefNeedReLogin = getSharedPreferences("needReLogin",MODE_PRIVATE);
            SharedPreferences.Editor editorNR = sPrefNeedReLogin.edit();
            editorNR.putBoolean(APP_PREFERENCES_NeedReLogin, true);
            editorNR.commit();

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении данных: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void saveAuth(String login, String password){
        try {
            sPrefAuthLogin = getSharedPreferences("authLogin", MODE_PRIVATE);
            SharedPreferences.Editor editorAL = sPrefAuthLogin.edit();
            editorAL.putString(APP_PREFERENCES_AuthLogin, login);
            editorAL.commit();

            sPrefAuthPass = getSharedPreferences("authPass", MODE_PRIVATE);
            SharedPreferences.Editor editorAP = sPrefAuthPass.edit();
            editorAP.putString(APP_PREFERENCES_AuthPass, password);
            editorAP.commit();

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении данных: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void LoadAuth(){
        try {
            sPrefAuthLogin = getSharedPreferences("authLogin",MODE_PRIVATE);
            this.authLogin = sPrefAuthLogin.getString(APP_PREFERENCES_AuthLogin, "");

            sPrefAuthPass = getSharedPreferences("authPass",MODE_PRIVATE);
            this.authPassword = sPrefAuthPass.getString(APP_PREFERENCES_AuthPass, "");
        } catch (Exception e){
            Toast.makeText(this, "Ошибка при загрузке данных: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void LoadSettings()
    {
        try
        {
            sPrefURL = getSharedPreferences("url",MODE_PRIVATE);
            String saveTextUrl = sPrefURL.getString(APP_PREFERENCES_url, "");

            sPrefToken = getSharedPreferences("Token",MODE_PRIVATE);
            String saveTextToken = sPrefToken.getString(APP_PREFERENCES_token, "");

            sPrefPass = getSharedPreferences("pass",MODE_PRIVATE);
            String saveTextPass = sPrefPass.getString(APP_PREFERENCES_pass, "");

            sPrefOrientation = getSharedPreferences("orientation",MODE_PRIVATE);
            int orientation = sPrefOrientation.getInt(APP_PREFERENCES_orientation, 0);

            listOrientation.setSelection(orientation);
            edToken.setText(saveTextToken);
            edURL.setText(saveTextUrl);
//            edAuthLogin.setText(authLogin);
//            edAuthPass.setText(authPass);
            password = saveTextPass;
        } catch (Exception e)
        {
            Toast.makeText(this, "Ошибка при загрузке данных: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnAuthConnect:
                DialogAuth();
                break;
//            case R.id.btnOpenAuthConnect:
//                DialogAuth();
//                break;
            case R.id.btnSaveSett:
                SaveSettings();
                break;
            case R.id.btnChgPass:
                DialogChangePassword();
                break;
            default:
                break;
        }
    }

    public void DialogChangePassword()
    {
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText edPass = (EditText) view.findViewById(R.id.edNewPass);
        EditText edPassCheck = (EditText) view.findViewById(R.id.edNewPassCheck);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Смена пароля для входа в настройки");
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (edPass.getText().toString().equals(edPassCheck.getText().toString()))
                {
                    password = edPass.getText().toString();
                    Toast.makeText(SettingsActivity.this, "Сохраните, для того, чтобы изменения вступили в силу", Toast.LENGTH_SHORT).show();
                }else
                    {
                        Toast.makeText(SettingsActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                    }
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
               dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void DialogPass()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите пароль:");
        builder.setCancelable(false);
        final EditText input = new EditText(SettingsActivity.this);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        input.setText("");
        builder.setView(input);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String pass = input.getText().toString();
                if(pass.equals(password))
                {
                    dialog.dismiss();
                }else
                    {
                        DialogPass();
                        Toast.makeText(SettingsActivity.this, "Не верный пароль!", Toast.LENGTH_SHORT).show();
                    }
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String baseUrl()  {
        sPrefURL = getSharedPreferences("url",MODE_PRIVATE);
        String url = sPrefURL.getString(APP_PREFERENCES_url, "");

        String result = "";
        try {
            URL aUrl = new URL(url);
            result = aUrl.getProtocol() + "://" + aUrl.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void authConnect() {
        if(baseUrl().isEmpty()){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Необходимо сохранить URL!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(!checkInternet(this)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(authTryConnectByToken()){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Авторизирован!", Toast.LENGTH_SHORT);
            toast.show();
        }else{
            DialogAuth();
        }

    }

    public void DialogAuth()
    {
        LoadAuth();
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_auth, null);
        EditText edAuth = (EditText) view.findViewById(R.id.edAuthLogin);
        EditText edPass = (EditText) view.findViewById(R.id.edAuthPass);

        edAuth.setText(this.authLogin);
        edPass.setText(this.authPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Аутентификация");
        builder.setCancelable(false);
        builder.setView(view);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                saveAuth(edAuth.getText().toString(), edPass.getText().toString());
                //authTryConnect(edAuth.getText().toString(), edPass.getText().toString());
            }
        }).setNegativeButton("Закрыть", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean authTryConnectByToken(){
        if(getAuthToken() == null) return false;

        DefaultHttpClient hc = new DefaultHttpClient();
        ResponseHandler response = new BasicResponseHandler();
        HttpGet http = new HttpGet(baseUrl() + "/lk/api/artab/get_auth_by_token?token=" + getAuthToken());
        final Boolean[] result = {false};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    String responseString = (String) hc.execute(http, response);
                    JSONObject jObj = new JSONObject(responseString);
                    result[0] = jObj.getBoolean("data");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    private void authTryConnect(String login, String pass){
        DefaultHttpClient hc = new DefaultHttpClient();
        ResponseHandler response = new BasicResponseHandler();
        HttpPost http = new HttpPost(baseUrl() + "/lk/api/tokenPass");

        final String[] statusAuth = {""};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("login", login));
                    nameValuePairs.add(new BasicNameValuePair("password", pass));
                    nameValuePairs.add(new BasicNameValuePair("remember", "false"));
                    nameValuePairs.add(new BasicNameValuePair("captcha", ""));
                    http.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    String responseString = (String) hc.execute(http, response);
                    Log.d("ResponseText", responseString);

                    JSONObject responseJSON = new JSONObject(responseString);
                    JSONObject dataJSON = responseJSON.getJSONObject("data");

                    //Проверяем есть ли ошибка
                    if(dataJSON.has("errors")){
                        JSONObject errorsJSON = dataJSON.getJSONObject("errors");
                        statusAuth[0] = errorsJSON.getString("auth");
                    }else{
                        setAuthToken(dataJSON.getString("token"));
                        statusAuth[0] = "Авторизирован!";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast toast;
        if(!statusAuth[0].isEmpty()) {
            toast = Toast.makeText(getApplicationContext(),
                    statusAuth[0], Toast.LENGTH_SHORT);
        }else{
            toast = Toast.makeText(getApplicationContext(),
                    "Неизвестная ошибка", Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    private static boolean checkInternet(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {return true;}
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {return true;}
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {return true;}
        return false;
    }

    private String getAuthToken(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cr;
        final char kv = (char) 34;
        cr = db.query(DBHelper.MAIN_TABLE, null, "name = " + kv + "authToken" + kv, null, null, null, null);
        cr.moveToFirst();
        if(cr.getCount() != 0) {
            return cr.getString(1);
        }
        else{return null;}
    }

    private void setAuthToken(String token){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Cursor cr = null;
        final char kv = (char) 34;
        boolean isOld = false;
        final String nameKey = "authToken";

        cv.put(DBHelper.KEY_DATA, token);
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
        assert cr != null;
        cr.close();
    }
}