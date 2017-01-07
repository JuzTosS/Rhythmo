package com.juztoss.rhythmo;

import android.app.Activity;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.NumberPicker;

import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.views.activities.SettingsActivity;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Created by JuzTosS on 7/29/2016.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsTest
{
    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<>(SettingsActivity.class);

    @Test
    public void TestAutoShiftChanged() throws Exception
    {
        Activity activity = mActivityRule.getActivity();
        RhythmoApp app = ((RhythmoApp) activity.getApplicationContext());


        int initialBPMShift = app.getBPMFilterAdditionWindowSize();
        for (int i = 0; i <= (int) RhythmoApp.MAX_BPM_SHIFT; i += 15)
        {
            if (i >= (int) RhythmoApp.MAX_BPM_SHIFT)
                i = (int) RhythmoApp.MAX_BPM_SHIFT;

            //Open the picker dialog
            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).perform(click());

            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.dialog_cancel))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.dialog_ok))).check(matches(isDisplayed()));

            //Set a value
            onView(withClassName(Matchers.equalTo(NumberPicker.class.getName()))).perform(setValue(i));

            onView(withText(activity.getString(R.string.dialog_cancel))).perform(click());

            assertEquals(initialBPMShift, app.getBPMFilterAdditionWindowSize());
        }

        for (int i = 0; i <= (int) RhythmoApp.MAX_BPM_SHIFT; i += 7)
        {
            if (i >= (int) RhythmoApp.MAX_BPM_SHIFT)
                i = (int) RhythmoApp.MAX_BPM_SHIFT;

            //Open the picker dialog
            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).perform(click());

            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.dialog_cancel))).check(matches(isDisplayed()));
            onView(withText(activity.getString(R.string.dialog_ok))).check(matches(isDisplayed()));

            //Set a value
            onView(withClassName(Matchers.equalTo(NumberPicker.class.getName()))).perform(setValue(i));

            onView(withText(activity.getString(R.string.dialog_ok))).perform(click());

            assertEquals(i, app.getBPMFilterAdditionWindowSize());
        }

        //Set initial value again

        //Open the picker dialog
        onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).perform(click());

        onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_cancel))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_ok))).check(matches(isDisplayed()));

        //Set a value
        onView(withClassName(Matchers.equalTo(NumberPicker.class.getName()))).perform(setValue(initialBPMShift));

        onView(withText(activity.getString(R.string.dialog_ok))).perform(click());

        assertEquals(initialBPMShift, app.getBPMFilterAdditionWindowSize());
        onView(withText(activity.getString(R.string.pref_bpm_auto_shift_range))).check(matches(isDisplayed()));


    }

    public static ViewAction setValue(final int value)
    {
        return new ViewAction()
        {
            @Override
            public void perform(UiController uiController, View view)
            {
                NumberPicker tp = (NumberPicker) view;
                tp.setValue(value);
            }

            @Override
            public String getDescription()
            {
                return "Set the passed time into the number picker";
            }

            @Override
            public Matcher<View> getConstraints()
            {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

}
