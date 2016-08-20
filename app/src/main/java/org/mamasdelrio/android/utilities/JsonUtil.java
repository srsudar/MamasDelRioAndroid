package org.odk.collect.android.utilities;

import android.util.Log;

import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by sudars on 8/17/16.
 */
public class JsonUtil {
  private static final String TAG = JsonUtil.class.getSimpleName();

  private DocumentBuilderFactory builderFactory;
  private DocumentBuilder builder;
  private Gson gson;

  public JsonUtil() {
    gson = new Gson();
    builderFactory =
        DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      Log.e(TAG, "parser config exception", e);
    }
  }

  public Map<String, String> parse(InputSource inputSource) throws SAXException,
      IOException, ParserConfigurationException {
    final DataCollector handler = new DataCollector();
    SAXParserFactory.newInstance().newSAXParser().parse(inputSource, handler);
    return handler.result;
  }

  /**
   * Convert map to a JSON object string.
   */
  public String convertMapToJson(Map<String, String> map) {
    String result = gson.toJson(map);
    return result;
  }

  /**
   * Taken from:
   * https://stackoverflow.com/questions/1537207/how-to-convert-xml-to-java-util-map-and-vice-versa
   */
  private static class DataCollector extends DefaultHandler {
    private final StringBuilder buffer = new StringBuilder();
    private final Map<String, String> result = new HashMap<String, String>();
    private boolean foundRoot = false;
    private String rootName = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (!foundRoot) {
        rootName = qName;
        foundRoot = true;
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      final String value = buffer.toString().trim();
      if (!qName.equals(rootName)) {
        result.put(qName, value);
      }
      buffer.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      buffer.append(ch, start, length);
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
  public Map<String, String> getXmlAsMap(String xml) throws ParserConfigurationException, SAXException, IOException {

    return parse(new InputSource(new StringReader(xml)));
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
}
