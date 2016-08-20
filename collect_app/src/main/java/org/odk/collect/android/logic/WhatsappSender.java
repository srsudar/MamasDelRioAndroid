package org.odk.collect.android.logic;

import android.app.Activity;
import android.content.Intent;

import org.odk.collect.android.utilities.Constants;

/**
 * This class prepares intents for starting Whatsapp.
 */
public class WhatsappSender {
  /**
   * At the moment, it is believed that we are going to want to send messages
   * either to the whole group of agents or just to doctors.
   */
  public enum MessageRecipient {
    GROUP, DOCTORS;
  }
  /**
   * Get the {@link Intent} to share a String message.
   * @param msg the Intent to share.
   * @return
   */
  public Intent getShareIntent(String msg) {
    // Based on these helpful posts:
    // http://stackoverflow.com/questions/8638475/intent-action-send-whatsapp
    // https://www.whatsapp.com/faq/android/28000012
    // Hmm, and can now send BOTH at the same time?
    // http://stackoverflow.com/questions/23077338/share-image-and-text-through-whatsapp-or-facebook
    final Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
    sendIntent.setPackage(Constants.WHATSAPP_PACKAGE);
    sendIntent.setType(Constants.TEXT_MESSAGE_MIME_TYPE);
    return sendIntent;
  }

  public void sendMessage(Activity activity, String message) {
    Intent intent = getShareIntent(message);
    activity.startActivityForResult(intent,
        Constants.RequestCodes.SEND_TO_WHATSAPP);
  }
}
