package org.mamasdelrio.android.logic;

/**/

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mamasdelrio.android.BuildConfig;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InstanceDataProcessor}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class InstanceDataProcessorTest {
  InstanceDataProcessor processor;

  @Before
  public void before() {
    // Make sure we don't share state.
    processor = null;
  }

  private void setup(Map<String, String> map) {
    processor = new InstanceDataProcessor(map);
  }

  @Test
  public void filterForSendCorrect() {
    // We should remove everything beginning with the private node prefix.
    Map<String, String> map = new HashMap<>();
    map.put("string", "hello there");
    map.put("another_value", "123");
    map.put("ignored_false", "should be present");

    Map<String, String> expected = new HashMap<>();
    expected.putAll(map);

    map.put("ign_messageTemplate", "hello ${name}");
    map.put("ign_phoneNumber", "2065551234");
    map.put("ign_", "hellow");

    setup(map);

    Map<String, String> actual = processor.filterForSend();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void filterForSendNullSafe() {
    setup(null);
    Map<String, String> expected = new HashMap<>();
    Map<String, String> actual = processor.filterForSend();

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getMessageTemplateFindsMessage() {
    Map<String, String> map = new HashMap<>();
    map.put("ignoreme", "");
    String expected = "I am ${name} and I have ${info}!";
    map.put(InstanceDataProcessor.NODE_MSG_TEMPLATE, expected);
    setup(map);

    String actual = processor.getMessageTemplate("should not be me");
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void getMessageTemplateReturnsDefault() {
    Map<String, String> map = new HashMap<>();
    map.put("name", "David");
    setup(map);

    String expected = "I have information.";
    String actual = processor.getMessageTemplate(expected);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void nodeShouldNotBeSentRespectsPrefix() {
    setup(new HashMap<String, String>());

    String[] trueArr = new String[] {
        "ign_",
        "ign_message",
        "ign_messageTemplate",
        "ign_some_value"
    };

    String[] falseArr = new String[] {
        "name",
        "another_value",
        "ign",
        "ignoremenot"
    };

    for (int i = 0; i < trueArr.length; i++) {
      boolean actual = processor.nodeShouldNotBeSent(trueArr[i]);
      assertThat(actual).isTrue();
    }

    for (int i = 0; i < falseArr.length; i++) {
      boolean actual = processor.nodeShouldNotBeSent(falseArr[i]);
      assertThat(actual).isFalse();
    }
  }
}
