package org.odk.collect.android.utilities;

import android.util.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by sudars on 8/17/16.
 */
public class JsonUtil {
  private static final String TAG = JsonUtil.class.getSimpleName();

  private DomDriver domDriver;
  private DocumentBuilderFactory builderFactory;
  private DocumentBuilder builder;

  public JsonUtil() {
    this.domDriver = new DomDriver();
    builderFactory =
        DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      Log.e(TAG, "parser config exception", e);
    }
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

    String rootElement = this.getRootElement(xml);

    xStream.alias(rootElement, java.util.Map.class);
    xStream.registerConverter(new MapEntryConverter());
    Map<String, Object> result = (Map<String,Object>) xStream.fromXML(xml);
    return result;
  }

  public String getRootElement(String xml) {
    InputSource inputSource = new InputSource();
    inputSource.setCharacterStream(new StringReader(xml));
    String result = null;
    try {
      Document xmlDoc = builder.parse(inputSource);
      result = xmlDoc.getDocumentElement().getNodeName();
    } catch (IOException e) {
      e.printStackTrace();
      Log.e(TAG, "IOException getting root element", e);
    } catch (SAXException e) {
      e.printStackTrace();
      Log.e(TAG, "SAXException getting root element", e);
    }
    return result;
  }

  /**
   * Taken from:
   * https://stackoverflow.com/questions/1537207/how-to-convert-xml-to-java-util-map-and-vice-versa
   */
  public static class MapEntryConverter implements Converter {
    public boolean canConvert(Class clazz) {
      return AbstractMap.class.isAssignableFrom(clazz);
    }

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
      AbstractMap map = (AbstractMap) value;
      for (Object obj : map.entrySet()) {
        Map.Entry entry = (Map.Entry) obj;
        writer.startNode(entry.getKey().toString());
        Object val = entry.getValue();
        if (null != val) {
          writer.setValue(val.toString());
        }
        writer.endNode();
      }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
      Map<String, String> map = new HashMap<String, String>();
      while (reader.hasMoreChildren()) {
        reader.moveDown();

        String key = reader.getNodeName(); // nodeName aka element's name
        String value = reader.getValue();
        map.put(key, value);

        reader.moveUp();
      }
      return map;
    }
  }
}
