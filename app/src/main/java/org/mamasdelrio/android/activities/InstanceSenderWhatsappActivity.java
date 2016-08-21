/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.mamasdelrio.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.mamasdelrio.android.R;
import org.mamasdelrio.android.application.Collect;
import org.mamasdelrio.android.listeners.ParseInstanceFileListener;
import org.mamasdelrio.android.logic.WhatsappSender;
import org.mamasdelrio.android.preferences.PreferencesActivity;
import org.mamasdelrio.android.tasks.ParseInstanceFileTask;
import org.mamasdelrio.android.utilities.JsonUtil;
import org.mamasdelrio.android.logic.MessageFormatter;
import org.mamasdelrio.android.utilities.WebUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity to send forms via Whatsapp.
 */
public class InstanceSenderWhatsappActivity extends Activity implements
    ParseInstanceFileListener {
  private final static String t =
      InstanceSenderWhatsappActivity.class.getSimpleName();
  private final static int PROGRESS_DIALOG = 1;
  private final static int AUTH_DIALOG = 2;

  private final static String AUTH_URI = "auth";
  private static final String ALERT_MSG = "alertmsg";
  private static final String ALERT_SHOWING = "alertshowing";
  private static final String TO_SEND = "tosend";

  private ProgressDialog mProgressDialog;
  private AlertDialog mAlertDialog;

  private String mAlertMsg;
  private boolean mAlertShowing;

  private ParseInstanceFileTask mParseTask;

  // maintain a list of what we've yet to send, in case we're interrupted by auth requests
  private Long[] mInstancesToSend;

  // maintain a list of what we've sent, in case we're interrupted by auth requests
  private HashMap<String, String> mUploadedInstances;
  private String mUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(t, "onCreate: " + ((savedInstanceState == null) ? "creating" : "re-initializing"));

    mAlertMsg = getString(R.string.please_wait);
    mAlertShowing = false;

    mUploadedInstances = new HashMap<String, String>();

    setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

    // get any simple saved state...
    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(ALERT_MSG)) {
        mAlertMsg = savedInstanceState.getString(ALERT_MSG);
      }
      if (savedInstanceState.containsKey(ALERT_SHOWING)) {
        mAlertShowing = savedInstanceState.getBoolean(ALERT_SHOWING, false);
      }

      mUrl = savedInstanceState.getString(AUTH_URI);
    }

    // and if we are resuming, use the TO_SEND list of not-yet-sent submissions
    // Otherwise, construct the list from the incoming intent value
    long[] selectedInstanceIDs = null;
    if (savedInstanceState != null && savedInstanceState.containsKey(TO_SEND)) {
      selectedInstanceIDs = savedInstanceState.getLongArray(TO_SEND);
    } else {
      // get instances to upload...
      Intent intent = getIntent();
      selectedInstanceIDs = intent.getLongArrayExtra(FormEntryActivity.KEY_INSTANCES);
    }

    mInstancesToSend = new Long[(selectedInstanceIDs == null) ? 0 : selectedInstanceIDs.length];
    if (selectedInstanceIDs != null) {
      for (int i = 0; i < selectedInstanceIDs.length; ++i) {
        mInstancesToSend[i] = selectedInstanceIDs[i];
      }
    }

    // at this point, we don't expect this to be empty...
    if (mInstancesToSend.length == 0) {
      Log.e(t, "onCreate: No instances to upload!");
      // drop through -- everything will process through OK
    } else {
      Log.i(t, "onCreate: Beginning upload of " + mInstancesToSend.length + " instances!");
    }

    // get the task if we've changed orientations. If it's null it's a new upload.
    mParseTask = (ParseInstanceFileTask) getLastNonConfigurationInstance();
    if (mParseTask == null) {
      // setup dialog and upload task
      showDialog(PROGRESS_DIALOG);
      mParseTask = new ParseInstanceFileTask();

      // register this activity with the new uploader task
      mParseTask.setListener(InstanceSenderWhatsappActivity.this);

      mParseTask.execute(mInstancesToSend);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Collect.getInstance().getActivityLogger().logOnStart(this);
  }

  @Override
  protected void onResume() {
    Log.i(t, "onResume: Resuming upload of " + mInstancesToSend.length + " instances!");
    if (mParseTask != null) {
      mParseTask.setListener(this);
    }
    if (mAlertShowing) {
      createAlertDialog(mAlertMsg);
    }
    super.onResume();
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ALERT_MSG, mAlertMsg);
    outState.putBoolean(ALERT_SHOWING, mAlertShowing);
    outState.putString(AUTH_URI, mUrl);

    long[] toSend = new long[mInstancesToSend.length];
    for (int i = 0; i < mInstancesToSend.length; ++i) {
      toSend[i] = mInstancesToSend[i];
    }
    outState.putLongArray(TO_SEND, toSend);
  }


  @Override
  public Object onRetainNonConfigurationInstance() {
    return mParseTask;
  }

  @Override
  protected void onPause() {
    Log.i(t, "onPause: Pausing upload of " + mInstancesToSend.length + " instances!");
    super.onPause();
    if (mAlertDialog != null && mAlertDialog.isShowing()) {
      mAlertDialog.dismiss();
    }
  }


  @Override
  protected void onStop() {
    Collect.getInstance().getActivityLogger().logOnStop(this);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    if (mParseTask != null) {
      mParseTask.setListener(null);
    }
    super.onDestroy();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case PROGRESS_DIALOG:
        Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.PROGRESS_DIALOG", "show");

        mProgressDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener loadingButtonListener =
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.PROGRESS_DIALOG", "cancel");
                dialog.dismiss();
                mParseTask.cancel(true);
                mParseTask.setListener(null);
                finish();
              }
            };
        mProgressDialog.setTitle(getString(R.string.uploading_data));
        mProgressDialog.setMessage(mAlertMsg);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
        return mProgressDialog;
      case AUTH_DIALOG:
        Log.i(t, "onCreateDialog(AUTH_DIALOG): for upload of " + mInstancesToSend.length + " instances!");
        Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.AUTH_DIALOG", "show");
        AlertDialog.Builder b = new AlertDialog.Builder(this);

        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.server_auth_dialog, null);

        // Get the server, username, and password from the settings
        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String server = mUrl;
        if (server == null) {
          Log.e(t, "onCreateDialog(AUTH_DIALOG): No failing mUrl specified for upload of " + mInstancesToSend.length + " instances!");
          // if the bundle is null, we're looking for a formlist
          String submissionUrl = getString(R.string.default_odk_submission);
          server =
              settings.getString(PreferencesActivity.KEY_SERVER_URL,
                  getString(R.string.default_server_url))
                  + settings.getString(PreferencesActivity.KEY_SUBMISSION_URL, submissionUrl);
        }

        final String url = server;

        Log.i(t, "Trying connecting to: " + url);

        EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        username.setText(storedUsername);

        EditText password = (EditText) dialogView.findViewById(R.id.password_edit);
        String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
        password.setText(storedPassword);

        b.setTitle(getString(R.string.server_requires_auth));
        b.setMessage(getString(R.string.server_auth_credentials, url));
        b.setView(dialogView);
        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.AUTH_DIALOG", "OK");
            EditText username = (EditText) dialogView.findViewById(R.id.username_edit);
            EditText password = (EditText) dialogView.findViewById(R.id.password_edit);

            Uri u = Uri.parse(url);
            WebUtils.addCredentials(username.getText().toString(), password.getText()
                .toString(), u.getHost());

            showDialog(PROGRESS_DIALOG);
            mParseTask = new ParseInstanceFileTask();

            // register this activity with the new uploader task
            mParseTask.setListener(InstanceSenderWhatsappActivity.this);

            mParseTask.execute(mInstancesToSend);
          }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            Collect.getInstance().getActivityLogger().logAction(this, "onCreateDialog.AUTH_DIALOG", "cancel");
            finish();
          }
        });

        b.setCancelable(false);
        return b.create();
    }
    return null;
  }

  private void createAlertDialog(String message) {
    Collect.getInstance().getActivityLogger().logAction(this, "createAlertDialog", "show");

    mAlertDialog = new AlertDialog.Builder(this).create();
    mAlertDialog.setTitle(getString(R.string.upload_results));
    mAlertDialog.setMessage(message);
    DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int i) {
        switch (i) {
          case DialogInterface.BUTTON_POSITIVE: // ok
            Collect.getInstance().getActivityLogger().logAction(this, "createAlertDialog", "OK");
            // always exit this activity since it has no interface
            mAlertShowing = false;
            finish();
            break;
        }
      }
    };
    mAlertDialog.setCancelable(false);
    mAlertDialog.setButton(getString(R.string.ok), quitListener);
    mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
    mAlertShowing = true;
    mAlertMsg = message;
    mAlertDialog.show();
  }

  /**
   * "Safely" dismiss the dialog.
   */
  private void dismissDialogSafely() {
    // Copying this from one of Carl's activities. I'm assuming this Exception
    // is some nonsense about Android's now deprecated way of managing dialogs
    // this way. Leaving the Exception swallowing just because I don't know
    // what else to catch here.
    try {
      dismissDialog(PROGRESS_DIALOG);
    } catch (Exception e) {
      // tried to close a dialog not open. don't care.
    }
  }

  @Override
  public void parsedFile(Uri toUpdate, ContentValues contentValues,
      Map<String, String> xmlContent) {
    WhatsappSender sender = new WhatsappSender();
    JsonUtil jsonUtil = new JsonUtil();

    MessageFormatter formatter = new MessageFormatter();
    String userFriendlyMessage = getString(R.string.default_user_message);
    String jsonStr  = jsonUtil.convertMapToJson(xmlContent);
    String finalMessage = formatter.createFinalMessage(userFriendlyMessage,
        jsonStr);

    sender.sendMessage(this, finalMessage);

    if (xmlContent != null) {
      // Assume we parsed and sent the data correctly.
      // Update the data layer to show that we've submitted the form. Unlike the
      // normal Collect process, we can't receive anything from Whatsapp to say
      // whether or not it was sent successfully, so we'll just always mark it as
      // successfully sent if we parsed the file and sent the message.
//      Collect.getInstance().getContentResolver()
//          .update(toUpdate, contentValues, null, null);
    }
    this.finish();
  }
}
