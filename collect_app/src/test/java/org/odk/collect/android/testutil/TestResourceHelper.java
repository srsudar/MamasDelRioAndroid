package org.odk.collect.android.testutil;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper for dealing with test resources.
 */
public class TestResourceHelper {
  /**
   * Get the body of the file described by resId as a String.
   * <p>
   * Taken from:
   * https://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
   * @param context
   * @param resId
   * @return
   */
  public String getResourceFileAsString(Context context, int resId) throws
      IOException {
    InputStream inputStream = context.getResources().openRawResource(resId);

    InputStreamReader inputReader = new InputStreamReader(inputStream);
    BufferedReader buffReader = new BufferedReader(inputReader);
    String line;
    StringBuilder text = new StringBuilder();

    try {
      while (( line = buffReader.readLine()) != null) {
        text.append(line);
        text.append('\n');
      }
    } catch (IOException e) {
      return null;
    }
    return text.toString();
  }
}
