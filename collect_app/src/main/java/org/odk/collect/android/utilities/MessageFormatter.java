package org.odk.collect.android.utilities;

import java.util.Map;

/**
 * Format messages for sending via Whatsapp.
 */
public class MessageFormatter {
  /**
   * The maximum number of teams we allow a single variable to be interpolated
   * in a message. This is mostly here as a sanity check to avoid recursion,
   * e.g. if you keep replacing ${var_name} with ${var_name}. Can be increased
   * if necessary.
   */
  private static final int MAX_REPEAT_VARIABLE = 20;

  /**
   * Interpolate a String similar to XLSForm label interpolation. Variables are
   * referenced using ${var_name} syntax, like XLSForm labels. var_name must be
   * a key in the map. The whole "${ }" construct will be replaced by the value
   * mapping to var_name.
   * <p>
   * For consider, "Hello ${name}. You are ${age} years old." and the map:
   * {
   *   name: "David",
   *   age: "95"
   * }
   * <p>
   * This will be interpolated to "Hello David. You are 95 years old."
   * Interpolation tries to be forgiving. Missing key value pairs are left
   * intact in the string, and syntax errors will return the original
   * rawMsg string.
   * <p>
   * Replacing a value with another variable reference (e.g. replacing ${foo}
   * with the string ${bar}) is safe but the output is undefined.
   * @param rawMsg
   * @return
   */
  public String interpolateMessage(String rawMsg, Map<String, String> map) {
    // We're going to go about this slightly backwards. Rather than bother
    // parsing the raw message we're going to create parse targets from the map
    // keys and replace them.
    String result = rawMsg;
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String varRepresentation = getVariableRepresentation(entry.getKey());
      String replaceWith = entry.getValue();
      if (replaceWith == null) {
        replaceWith = "";
      }

      int iteration = 0;
      int indexOfInsert = result.indexOf(varRepresentation);
      while (indexOfInsert >= 0 && iteration < MAX_REPEAT_VARIABLE) {
        iteration++;
        result = result.replace(varRepresentation, replaceWith);
        indexOfInsert = result.indexOf(varRepresentation);
      }
    }
    return result;
  }

  /**
   * Get varName as expected to be referenced in a message.
   * @param varName
   * @return
   */
  private String getVariableRepresentation(String varName) {
    return "${" + varName + "}";
  }
}
