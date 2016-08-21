package org.mamasdelrio.android.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * In the MDR project we are not using the default send process, but instead
 * sending the data via Whatsapp. By default we get the instance form as a Map
 * and convert this to JSON. This class operates on the raw Map in order to
 * provide a processing step before sending. It provides a way to do things
 * like include a message to include with the Whatsapp message. The idea is to
 * allow this to be included and specified by the form designers rather than in
 * the apk.
 */
public class InstanceDataProcessor {
  private Map<String, String> rawContent;

  public static final String PRIVATE_NODE_PREFIX = "ign_";
  /** The XML node representing the message template we will use. */
  public static final String NODE_MSG_TEMPLATE = PRIVATE_NODE_PREFIX +
      "message";

  /**
   *
   * @param rawContent null safe--will be treated as an empty map.
   */
  public InstanceDataProcessor(Map<String, String> rawContent) {
    if (rawContent == null) {
      rawContent = new HashMap<>();
    }
    this.rawContent = rawContent;
  }

  /**
   * Filter the map and return the values that we want to include in the JSON
   * object we send.
   * @return
   */
  public Map<String, String> filterForSend() {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, String> entry: rawContent.entrySet()) {
      if (!nodeShouldNotBeSent(entry.getKey())) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  /**
   * Return the message template that is stored in the map. If absent, returns
   * defaultValue.
   */
  public String getMessageTemplate(String defaultValue) {
    if (rawContent.containsKey(NODE_MSG_TEMPLATE)) {
      return rawContent.get(NODE_MSG_TEMPLATE);
    } else {
      return defaultValue;
    }
  }

  /**
   * Returns true if the key is considered private and should not be included in
   * the output.
   */
  protected boolean nodeShouldNotBeSent(String key) {
    return key.startsWith(PRIVATE_NODE_PREFIX);
  }
}
