package org.odk.collect.android.utilities;

import android.content.Context;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.testutil.TestResourceHelper;
import org.odk.collect.android.utilities.JsonUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.odk.collect.android.utilities.JsonUtil}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class JsonUtilTest {

  JsonUtil jsonUtil;
  TestResourceHelper resourceHelper;

  @Before
  public void before() {
    jsonUtil = new JsonUtil();
    resourceHelper = new TestResourceHelper();
  }

  @Test
  public void parseBasicXmlAsMap() throws IOException {
    // Dead simple, resource-free, XML form one level deep.
    // These keys and values are taken from res/xml/test_form.xml.
    Map<String, Object> expected = new HashMap<>();
    expected.put("DeviceId", "867979021299992");
    expected.put("Image", "1471488787354.jpg");
    expected.put("Location", "47.65845414 -122.31286083 25.0 12.0");
    expected.put("Description", "My room.");

    String geotaggerXml = getGeoTaggerTestXml();
    Map<String, Object> actual = jsonUtil.getXmlAsMap(geotaggerXml);

    assertThat(actual).isEqualTo(expected);
  }

  private String getGeoTaggerTestXml() throws IOException {
    Context context = RuntimeEnvironment.application;
    int resId = R.xml.test_form;
    String result = resourceHelper.getResourceFileAsString(context, resId);
    return result;
  }

}
