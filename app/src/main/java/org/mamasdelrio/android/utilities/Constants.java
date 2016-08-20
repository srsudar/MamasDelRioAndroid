package org.mamasdelrio.android.utilities;

import org.mamasdelrio.android.BuildConfig;

/**
 * Constant values shared across the app.
 */
public class Constants {
  public static final String WHATSAPP_PACKAGE = "com.whatsapp";
  public static final String TEXT_MESSAGE_MIME_TYPE = "text/plain";
  public static final String UTF8 = "UTF-8";

  /** The version identifying the app. */
  public static final int VERSION = BuildConfig.VERSION_CODE;

  public static class RequestCodes {
    public static final int SEND_TO_WHATSAPP = 101;
  }
}
