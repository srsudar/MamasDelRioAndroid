package org.odk.collect.android.utilities;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;

/**
 * Created by sudars on 8/17/16.
 */
public class JsonUtil {
  private DomDriver domDriver;

  public JsonUtil() {
    this.domDriver = new DomDriver();
  }

  /**
   * Get the XML string as a Map.
   * <p>
   * This is based on:
   * https://stackoverflow.com/questions/1537207/how-to-convert-xml-to-java-util-map-and-vice-versa
   * @param xml
   * @return
   */
  public Map<String, Object> getXmlAsMap(String xml) {
    XStream xStream = new XStream(this.domDriver);
    Map<String, Object> result = (Map<String,Object>) xStream.fromXML(xml);
    return result;
  }
}
