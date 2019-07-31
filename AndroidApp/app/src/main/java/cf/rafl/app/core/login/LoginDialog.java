package cf.rafl.app.core.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.fragment.app.DialogFragment;


public class LoginDialog extends DialogFragment
{
    public static LoginDialog newInstance(String title, String message) {
        LoginDialog frag = new LoginDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");


        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = getView();
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        RelativeLayout layout = new RelativeLayout(getContext());
        layout.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);
        return v;
    }
}
