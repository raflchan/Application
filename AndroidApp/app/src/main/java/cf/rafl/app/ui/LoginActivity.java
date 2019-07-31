package cf.rafl.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import cf.rafl.app.R;
import cf.rafl.app.core.login.LoginListener;
import cf.rafl.app.core.util.EnableButtonWatcher;

public class LoginActivity extends AppCompatActivity
{

    Button loginButton, registerButton;
    EditText usernameField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginLoginButton);
        registerButton = findViewById(R.id.loginRegisterButton);
        usernameField = findViewById(R.id.loginUsernameField);
        passwordField = findViewById(R.id.loginPasswordField);


        loginButton.setEnabled(false);
        EnableButtonWatcher watcher = new EnableButtonWatcher(
                new ArrayList<TextView>(Arrays.asList(usernameField, passwordField)),
                loginButton);
        usernameField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);

        loginButton.setOnClickListener(new LoginListener(this));
        registerButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(getBaseContext(), RegisterActivity.class);
                        intent.putExtra("USERNAME", usernameField.getText().toString());
                        startActivity(intent);
                    }
                }
        );

    }
}
