package org.mamasdelrio.android.testutil;

import android.content.Context;

import org.mamasdelrio.android.R;

import java.io.IOException;

/**
 * Gets test resources.
 */
public class ResourceProvider {
  private static TestResourceHelper resourceHelper = new TestResourceHelper();

  public static String getGeoTaggerXml(Context context) {
    return getFileAsStringSwallowException(context, R.raw.form_geotagger);
  }

  public static String getWidgetsXml(Context context){
    return getFileAsStringSwallowException(context, R.raw.form_widgets);
  }

  private static String getFileAsStringSwallowException(
      Context context,
      int resId) {
    String result = null;
    try {
      result = resourceHelper.getResourceFileAsString(context, resId);
    } catch (IOException e) {
      throw new RuntimeException("IO Exception getting resource: " + resId);
    }
    return result;
  }
}
