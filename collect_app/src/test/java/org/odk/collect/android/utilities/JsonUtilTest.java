package org.odk.collect.android.utilities;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.utilities.JsonUtil;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.odk.collect.android.utilities.JsonUtil}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class JsonUtilTest {

  JsonUtil jsonUtil;

  @Before
  public void before() {
    jsonUtil = new JsonUtil();
  }

}
