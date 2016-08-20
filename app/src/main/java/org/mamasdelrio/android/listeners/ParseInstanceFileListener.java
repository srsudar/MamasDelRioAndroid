package org.odk.collect.android.listeners;

import android.content.ContentValues;
import android.net.Uri;

import java.util.Map;

/**
 * Created by sudars on 8/19/16.
 */
public interface ParseInstanceFileListener {
  void parsedFile(Uri toUpdate, ContentValues contentValues,
      Map<String, String> xmlContent);
}
