package org.odk.collect.android.utilities;

import org.odk.collect.android.BuildConfig;

/**
 * Constant values shared across the app.
 */
public class Constants {
  public static final String WHATSAPP_PACKAGE = "com.whatsapp";
  public static final String TEXT_MESSAGE_MIME_TYPE = "text/plain";
  public static final String UTF8 = "UTF-8";

  public static final int NUM_MONTH_IN_PREGNANCY = 9;

  public static final int DEFAULT_BIRTH_YEAR = 2006;

  /**
   * The delimiter between the user friendly and JSON portions of the
   * message.
   */
  public static final String MSG_DELIMITER = "\n\nNo modificar:\n-----\n";

  /** The version identifying the app. */
  public static final int VERSION = BuildConfig.VERSION_CODE;

  public static class RequestCodes {
    public static final int GET_LOCATION = 100;
    public static final int SEND_TO_WHATSAPP = 101;
  }
}
