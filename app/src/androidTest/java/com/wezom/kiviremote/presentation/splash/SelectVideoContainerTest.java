package com.wezom.kiviremote.presentation.splash;


import android.app.Instrumentation;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.HomeActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

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
            assertThat(((SlidingUpPanelLayout) mActivityTestRule.getActivity().findViewById(R.id.sliding_layout)).getPanelState(), is(SlidingUpPanelLayout.PanelState.EXPANDED));
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
            assertThat(((SlidingUpPanelLayout) mActivityTestRule.getActivity().findViewById(R.id.sliding_layout)).getPanelState(), is(SlidingUpPanelLayout.PanelState.EXPANDED));
        }
    }

    private void navigateToVideoContainer() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.single_device_connect), withText("Подключиться"),
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
