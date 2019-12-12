package com.wezom.kiviremote.presentation.splash;


import android.app.Instrumentation;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.wezom.kiviremote.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import timber.log.Timber;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SelectVideoContainerTest {

    private UiDevice mDevice;

    @Before
    public void setUp() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
    }

//    @Rule
//    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void selectVideoContainerTest_caseA_pressDeny() {
        navigateToVideoContainer();
        if (!denyPermission()) {
            onView(ViewMatchers.withId(R.id.gallery_container)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Timber.e(e, "Thread sleep was interrupted");
            }
//            assertThat(((SlidingUpPanelLayout) mActivityTestRule.getActivity().findViewById(R.id.sliding_layout)).getPanelState(), is(SlidingUpPanelLayout.PanelState.EXPANDED));
        }
    }

    @Test
    public void selectVideoContainerTest_caseB_pressAllow() {
        navigateToVideoContainer();
        if (!allowPermissionsIfNeeded()) {
            onView(ViewMatchers.withId(R.id.gallery_container)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Timber.e(e, "Thread sleep was interrupted");
            }
//            assertThat(((SlidingUpPanelLayout) mActivityTestRule.getActivity().findViewById(R.id.sliding_layout)).getPanelState(), is(SlidingUpPanelLayout.PanelState.EXPANDED));
        }
    }

    private void navigateToVideoContainer() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.device_container), withText("Подключиться"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.activity_home_container),
                                        0),
                                11),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.main_tab_layout),
                                0),
                        3),
                        isDisplayed()));
        tabView.perform(click());

        ViewInteraction noSwipeViewPager = onView(
                allOf(withId(R.id.main_viewPager),
                        childAtPosition(
                                allOf(withId(R.id.main_container),
                                        childAtPosition(
                                                withId(R.id.activity_home_container),
                                                0)),
                                2),
                        isDisplayed()));
        noSwipeViewPager.perform(swipeLeft());

        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.media_container),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1)))
                .atPosition(0);
        constraintLayout.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    private boolean allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(new UiSelector().text("РАЗРЕШИТЬ"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                    return true;
                } catch (UiObjectNotFoundException e) {
                    Timber.e(e, "There is no permissions dialog to interact with ");
                }
            }
        }
        return false;
    }

    private boolean denyPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject denyPermissions = mDevice.findObject(new UiSelector().text("ОТКЛОНИТЬ"));
            if (denyPermissions.exists()) {
                try {
                    denyPermissions.click();
                    return true;
                } catch (UiObjectNotFoundException e) {
                    Timber.e(e, "There is no permissions dialog to interact with ");
                }
            }
        }

        return false;
    }
}
