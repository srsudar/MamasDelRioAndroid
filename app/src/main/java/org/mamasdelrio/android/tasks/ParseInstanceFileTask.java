package org.mamasdelrio.android.tasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.mamasdelrio.android.application.Collect;
import org.mamasdelrio.android.listeners.ParseInstanceFileListener;
import org.mamasdelrio.android.provider.InstanceProviderAPI;
import org.mamasdelrio.android.provider.InstanceProviderAPI.InstanceColumns;
import org.mamasdelrio.android.utilities.Constants;
import org.mamasdelrio.android.utilities.JsonUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Background task for parsing a file from XML. This really only needs to be a
 * task to keep disk reads off the UI thread. It doesn't do much.
 */
public class ParseInstanceFileTask extends AsyncTask<Long, Integer,
    ParseInstanceFileTask.Outcome> {
  private static final String TAG = ParseInstanceFileTask.class.getSimpleName();

  private ParseInstanceFileListener listener;

  public static class Outcome {
    /** The Uri to update via contentResolver.update. */
    public Uri toUpdate;
    /** The ContentValues to udpate the Uri with. */
    public ContentValues contentValues;
    /** The result of the read--the XML values as a map. */
    public Map<String, String> map;
  }

  public void setListener(ParseInstanceFileListener listener) {
    this.listener = listener;
  }

  /**
   * Sends one message via Whatsapp.
   */
  private boolean readMapFromFile(String instanceFilePath, Uri toUpdate,
      Outcome outcome) {
    Collect.getInstance().getActivityLogger().logAction(this,
        "sending via whatsapp", instanceFilePath);

    File instanceFile = new File(instanceFilePath);
    ContentValues cv = new ContentValues();

    boolean result = false;
    try {
      InputStream inputStream = new FileInputStream(instanceFile);
      Reader reader = new InputStreamReader(inputStream);
      InputSource inputSource = new InputSource(reader);
      inputSource.setEncoding(Constants.UTF8);

      JsonUtil jsonUtil = new JsonUtil();
      Map<String, String> map = jsonUtil.parse(inputSource);

      cv.put(InstanceColumns.STATUS,
          InstanceProviderAPI.STATUS_SUBMITTED);
      outcome.contentValues = cv;
      outcome.toUpdate = toUpdate;
      outcome.map = map;

      result = true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  protected Outcome doInBackground(Long... values) {
    Outcome outcome = new Outcome();
    // I should be refactoring this rather than just duplicating the code
    // in InstanceUploaderTask, but not going to bother for now as then I'd
    // just want to write tests for it and bring it out of both places.

    String selection = InstanceColumns._ID + "=?";
    String[] selectionArgs = new String[(values == null) ? 0 : values.length];
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        if (i != values.length - 1) {
          selection += " or " + InstanceColumns._ID + "=?";
        }
        selectionArgs[i] = values[i].toString();
      }
    }

    Cursor c = null;
    try {
      c = Collect.getInstance().getContentResolver()
          .query(InstanceColumns.CONTENT_URI, null, selection,
              selectionArgs, null);

      if (c.getCount() > 0) {
        c.moveToPosition(-1);
        while (c.moveToNext()) {
          if (isCancelled()) {
            return outcome;
          }
          publishProgress(c.getPosition() + 1, c.getCount());
          String instance = c.getString(
              c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
          String id = c.getString(c.getColumnIndex(InstanceColumns._ID));
          Uri toUpdate = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);

          if (!readMapFromFile(instance, toUpdate, outcome)) {
            return outcome;
          }
        }
      }
    } finally {
      if (c != null) {
        c.close();
      }
    }

    return outcome;
  }

  @Override
  public void onPostExecute(Outcome outcome) {
    if (this.listener != null) {
      this.listener.parsedFile(outcome.toUpdate, outcome.contentValues,
          outcome.map);
    } else {
      Log.w(TAG, "parsed file but did listener not attached, doing nothing");
    }
  }
}
