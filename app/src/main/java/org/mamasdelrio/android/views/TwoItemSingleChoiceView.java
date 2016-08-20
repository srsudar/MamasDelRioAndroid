package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import org.odk.collect.android.R;

/**
 * Created by sudars on 8/19/16.
 */
public class TwoItemSingleChoiceView extends RelativeLayout implements
    Checkable {
  public TwoItemSingleChoiceView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }


  public TwoItemSingleChoiceView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }


  public TwoItemSingleChoiceView(Context context) {
    super(context);
  }


  @Override
  public boolean isChecked() {
    RadioButton c = (RadioButton) findViewById(R.id.radiobutton);
    return c.isChecked();
  }


  @Override
  public void setChecked(boolean checked) {
    RadioButton c = (RadioButton) findViewById(R.id.radiobutton);
    c.setChecked(checked);
  }


  @Override
  public void toggle() {
    RadioButton c = (RadioButton) findViewById(R.id.radiobutton);
    c.setChecked(!c.isChecked());
  }
}
