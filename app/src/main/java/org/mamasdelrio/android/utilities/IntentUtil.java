package org.mamasdelrio.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.mamasdelrio.android.activities.FormEntryActivity;

public class IntentUtil {
  /**
   * Returns an {@link Intent} to edit the given form URI using this
   * application's {@link org.mamasdelrio.android.activities.FormEntryActivity}.
   * This is a precaution to prevent the form being edited by Collect, as most
   * of the activities here are started by URI and action, not by class, giving
   * users the opportunity to choose the application to edit the form.
   * @param formUri
   * @return
   */
  public Intent getFormEntryIntent(Context applicationContext, Uri formUri) {
    Intent result = new Intent(Intent.ACTION_EDIT, formUri);
    result.setClass(applicationContext, FormEntryActivity.class);
    return result;
  }
}
