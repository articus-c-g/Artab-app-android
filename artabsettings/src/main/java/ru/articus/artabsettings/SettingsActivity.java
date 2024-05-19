package ru.articus.artabsettings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener
{
    String password = "123";
    public SharedPreferences sPref;
    EditText edURL;
    public static final String APP_PREFERENCES = "Settings";
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        DialogPass();
        edURL = findViewById(R.id.URLRes);
        btnSave = (Button) findViewById(R.id.btnSaveSett);
        btnSave.setOnClickListener(this);
        LoadSettings();
    }

    public void SaveSettings()
    {
        try {
            sPref = getSharedPreferences("sett",MODE_PRIVATE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putString(APP_PREFERENCES, edURL.getText().toString());
            editor.commit();
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
        } catch (Exception e)
        {
            Toast.makeText(this, "Ошибка при сохранении данных: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void LoadSettings()
    {
        try
        {
            sPref = getPreferences(MODE_PRIVATE);
            String saveText = sPref.getString(APP_PREFERENCES, "");
            edURL.setText(saveText);
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
            case R.id.btnSaveSett:
                SaveSettings();
                break;
            default:
                break;
        }
    }
    public void DialogPass()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите пароль:");
        builder.setCancelable(false);
        final EditText input = new EditText(SettingsActivity.this);
        input.setInputType(2);
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
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}