package cf.rafl.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import cf.rafl.app.R;
import cf.rafl.app.core.util.EnableButtonWatcher;

public class RegisterActivity extends AppCompatActivity
{

    Button registerButton;
    TextView usernameField, emailField, passwordField, confirmPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = findViewById(R.id.registerRegisterButton);
        usernameField = findViewById(R.id.registerUsernameField);
        emailField = findViewById(R.id.registerEmailField);
        passwordField = findViewById(R.id.registerPasswordField);
        confirmPasswordField = findViewById(R.id.registerConfirmPasswordField);


        // TODO: 29.07.2019 enabling doesn't work 
        registerButton.setEnabled(false);
        EnableButtonWatcher watcher = new EnableButtonWatcher(
                new ArrayList<>(Arrays.asList(usernameField, emailField, passwordField, confirmPasswordField)),
                registerButton
        );
        registerButton.addTextChangedListener(watcher);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        String username = getIntent().getStringExtra("USERNAME");
        if(username != null)
            ((TextView) findViewById(R.id.registerUsernameField)).setText(username);

    }

}
