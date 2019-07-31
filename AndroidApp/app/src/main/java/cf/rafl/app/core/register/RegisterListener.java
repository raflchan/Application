package cf.rafl.app.core.register;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cf.rafl.app.R;
import cf.rafl.app.core.login.LoginDialog;
import cf.rafl.app.core.util.TimeLimitedTask;
import cf.rafl.http.client.HttpClient;
import cf.rafl.http.core.HttpResponse;

public class RegisterListener implements View.OnClickListener
{
    private final AppCompatActivity app;


    public RegisterListener(AppCompatActivity appCompatActivity)
    {
        this.app = appCompatActivity;
    }

    @Override
    public void onClick(View view)
    {
        new RegisterListener.AsyncRegisterTask(this.app).execute();
    }

    private class AsyncRegisterTask extends AsyncTask<Void, Void, HttpResponse>
    {

        private AppCompatActivity app;
        private String txMessage;

        private boolean timedOut = false;

        private LoginDialog dialog;

        private AsyncRegisterTask(AppCompatActivity app)
        {
            this.app = app;
        }

        @Override
        protected void onPreExecute()
        {

            //  show dialog
            dialog = LoginDialog.newInstance("Sign-up", "Signing up...");
            dialog.setCancelable(false);
            dialog.show(app.getSupportFragmentManager(), "signup dialog");

            String username = ((TextView) app.findViewById(R.id.loginUsernameField)).getText().toString();
            String password = ((TextView) app.findViewById(R.id.loginPasswordField)).getText().toString();

            txMessage = "username=" + username + "\n" +
                    "password=" + password;
        }

        @Override
        protected HttpResponse doInBackground(Void ... params)
        {
            TimeLimitedTask task = new TimeLimitedTask(new Callable()
            {
                @Override
                public Object call() throws Exception
                {
                    HttpClient client = new HttpClient.Builder("app.rafl.cf").useSSL().build();
                    return client.POST("/register", txMessage);
                }
            });

            try
            {
                return (HttpResponse) task.execute(3, TimeUnit.SECONDS);
            } catch (TimeoutException e)
            {
                timedOut = true;
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(HttpResponse response)
        {
            String title, message;

            if(timedOut)
            {
                title = "Sign-up Failed!";
                message = "Couldn't establish connection to server!";

            } else if (response == null)
            {
                title = "Sign-up Failed!";
                message = "Error in connection to server!\nCheck console";

            } else
            {
                if(response.getStatusCode() != 200)
                {
                    title = "Sign-up Failed!";
                    message = response.getContent();
                }
                else
                {
                    dialog.dismiss();
                    String sessionToken = response.getContent();
                    Intent data = new Intent();
                    data.setData(Uri.parse(sessionToken));
                    app.setResult(app.getResources().getInteger(R.integer.login), data);
                    app.setContentView(R.layout.activity_main);
                    app.finish();
                    return;
                }
            }

            dialog.dismiss();

            new AlertDialog.Builder(app)
                    .setTitle(title)
                    .setMessage(message)
                    .create()
                    .show();

            app = null;
        }
    }
}
