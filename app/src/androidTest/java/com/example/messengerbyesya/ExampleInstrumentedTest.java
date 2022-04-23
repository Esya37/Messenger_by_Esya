package com.example.messengerbyesya;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityRule = new ActivityScenarioRule <>(
            MainActivity.class);

    @Test
    public void registrationTestWithData1() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("Aa123456"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("Aa123456"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData2() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("Aa123456"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("Aa123456789"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData3() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("123"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("123"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData4() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("ЫЫЫЫЫЫЫ"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("ЫЫЫЫЫЫЫ"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData5() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("qwerty"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("qwerty"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData6() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("QWERTY"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("QWERTY"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData7() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("123456"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("123456"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData8() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("@_@_@_@"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("@_@_@_@"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).check(matches(hasErrorText("Введенные данные неверны")));
    }

    @Test
    public void registrationTestWithData9() {
        onView(withId(R.id.editTextTextEmailAddressRegistration)).perform(replaceText("qwe@mail.com"));
        onView(withId(R.id.editTextTextPasswordRegistration)).perform(replaceText("Aa123__"));
        onView(withId(R.id.editTextTextPassword2Registration)).perform(replaceText("Aa123__"));
        onView(withId(R.id.registrationSubmitButton)).perform(click());
        onView(withId(R.id.editTextTextPasswordRegistration)).inRoot(isDialog()); //Если появилось окно загрузки, значит, регистрация успешна
    }


}