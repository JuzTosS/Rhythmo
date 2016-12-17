package com.juztoss.rhythmo.views.activities;


import android.support.annotation.NonNull;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.juztoss.rhythmo.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.TestSuite.SONG1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class PlayerActivityTest
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityTestRule = new ActivityTestRule<>(PlayerActivity.class);

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher)
    {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view)
            {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null)
                {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    protected void checkScreen(int songsInList, String headerTitle, String headerDescription, String headerBpm, int playingSongIndex, boolean isPlayButtonActive)
    {
        onView(allOf(withId(R.id.listView), isDisplayed())).check(new RecyclerViewItemCountAssertion(songsInList + 1));//+1 because we have last empty element
        onView(allOf(withId(R.id.bpm_header_label))).check(matches(withText(headerBpm)));
        if (headerBpm.length() > 0)
            onView(allOf(withId(R.id.bpm_header_label))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.first_header_line))).check(matches(withText(headerTitle)));
        if (headerTitle.length() > 0)
            onView(allOf(withId(R.id.first_header_line))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.second_header_line))).check(matches(withText(headerDescription)));
        if (headerDescription.length() > 0)
            onView(allOf(withId(R.id.second_header_line))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.play_button), isDisplayed())).check(matches(!isPlayButtonActive ? not(isSelected()) : isSelected()));
        if (playingSongIndex >= 0)
            onView(allOf(withId(R.id.listView))).check(matches(atPosition(playingSongIndex, allOf(hasDescendant(allOf(withId(R.id.playing_state), isDisplayed()))))));
        else
        {
            onView(allOf(withId(R.id.listView))).check(matches(atPosition(0, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
            onView(allOf(withId(R.id.listView))).check(matches(atPosition(1, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
            onView(allOf(withId(R.id.listView))).check(matches(atPosition(2, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
        }
    }

    @Test
    public void playerActivityTest()
    {
        checkScreen(3, "", "", "", -1, true);

        onView(allOf(withId(R.id.listView), isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        checkScreen(3, SONG1, "RhythmoTestTemp", "0.0", 0, false);

    }

    public class RecyclerViewItemCountAssertion implements ViewAssertion
    {
        private final int expectedCount;

        public RecyclerViewItemCountAssertion(int expectedCount)
        {
            this.expectedCount = expectedCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException)
        {
            if (noViewFoundException != null)
            {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(expectedCount));
        }
    }
}
