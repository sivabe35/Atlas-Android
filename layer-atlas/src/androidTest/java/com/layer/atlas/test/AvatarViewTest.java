package com.layer.atlas.test;


import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.support.test.rule.ActivityTestRule;

import com.layer.atlas.view.AvatarActivityTestView;
import com.layer.ui.R;

import org.junit.Rule;
import org.junit.Test;

public class AvatarViewTest {

    @Rule
    public ActivityTestRule<AvatarActivityTestView> mNotesActivityTestRule =
            new ActivityTestRule<>(AvatarActivityTestView.class);

    @Test
    public void testThatAvatarColorChangeWhenSpinnerIsChanged() {
        String selectionText = "AWAY";

        onView(withId(R.id.test_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
        onView(withId(R.id.test_spinner)).check(matches(withSpinnerText(containsString(selectionText))));

    }

}
