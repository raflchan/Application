package cf.rafl.app.core.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EnableButtonWatcher implements TextWatcher
{

    List<TextView> textViews;
    List<Button> buttons;

    public EnableButtonWatcher(List<TextView> textViews, List<Button> buttons)
    {
        this.textViews = textViews;
        this.buttons = buttons;
    }

    public EnableButtonWatcher(List<TextView> textViews, Button button)
    {
        this.textViews = textViews;
        this.buttons = new ArrayList<>();
        buttons.add(button);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        boolean enabled = true;
        for (TextView textView : textViews)
            enabled = enabled && !textView.getText().toString().isEmpty();

        for (Button button : buttons)
            button.setEnabled(enabled);
    }
}
