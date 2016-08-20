package org.mamasdelrio.android.utilities;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mamasdelrio.android.BuildConfig;
import org.mamasdelrio.android.testutil.ResourceProvider;
import org.mamasdelrio.android.testutil.TestResourceHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.mamasdelrio.android.utilities.JsonUtil}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class JsonUtilTest {

  JsonUtil jsonUtil;
  TestResourceHelper resourceHelper;
  Context context;

  @Before
  public void before() {
    jsonUtil = new JsonUtil();
    resourceHelper = new TestResourceHelper();
    context = RuntimeEnvironment.application;
  }

  @Test
  public void convertMapToJsonCorrectForBasicCase() {
    Map<String, String> map = new HashMap<>();
    map.put("foo", "bar");
    map.put("name", "Dr. Seuss");

    // This is potentially flaky, as the order of the output in the map is not
    // specified. I.e. we might get foo or name first in the result depending
    // on the platform. We may have to adjust this.
    String expected = "{" +
        "\"name\":\"Dr. Seuss\"," +
        "\"foo\":\"bar\"" +
        "}";
    String actual = jsonUtil.convertMapToJson(map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void parseGeotaggerAsMap() throws Exception {
    // Dead simple, resource-free, XML form one level deep.
    // These keys and values are taken from res/raw/form_geotagger.xml.
    Map<String, String> expected = new HashMap<>();
    expected.put("DeviceId", "867979021299992");
    expected.put("Image", "1471488787354.jpg");
    expected.put("Location", "47.65845414 -122.31286083 25.0 12.0");
    expected.put("Description", "My room.");

    String geotaggerXml = ResourceProvider.getGeoTaggerXml(context);
    Map<String, String> actual = jsonUtil.getXmlAsMap(geotaggerXml);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void parseWidgetsAsXml() throws Exception {
    // This is an attempt to get more types of prompt answers from the XML.
    // They are taken from res/raw/form_widgets.xml.
    Map<String, Object> expected = new HashMap<>();
    expected.put("start", "2016-08-18T19:18:46.922-07");
    expected.put("deviceid", "867979021299992");
    expected.put("string", "string value");
    expected.put("int", "9");
    expected.put("decimal", "18.31");
    expected.put("date", "2018-06-15");
    expected.put("select", "a d");
    expected.put("select1", "3");
    expected.put("regex", "test@test.com");
    expected.put("leftblank", "");
    expected.put("geopoint", "47.65830366 -122.31297098 121.0 16.0");

    String widgetXml = ResourceProvider.getWidgetsXml(context);
    Map<String, String> actual = jsonUtil.getXmlAsMap(widgetXml);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getRootElementWorksForGeopoint() throws Exception {
    String xml = ResourceProvider.getGeoTaggerXml(context);
    String expected = "geotagger";
    String actual = jsonUtil.getRootElement(xml);
    assertThat(actual).isEqualTo(expected);
  }
}
