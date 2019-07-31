package cf.rafl.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import cf.rafl.app.R;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login();

    }

    private void login()
    {
        startActivityForResult(new Intent(
                MainActivity.this, LoginActivity.class),
                getResources().getInteger(R.integer.login)
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == resultCode)
        {
            String sData = data.getDataString();
            if (requestCode == getResources().getInteger(R.integer.login))
            {
                TextView textView = findViewById(R.id.mainText);
                textView.setText(sData);
            }
        }
    }
}
