package cn.edu.scnu.ljh.chinesepoetry;

import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    //测试搜索
    @Test
    public void testSearchViewAndClick() throws InterruptedException {
        onView(withId(R.id.action_search)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.searchTextView)).perform(replaceText("静夜思"));
        Thread.sleep(2000);
        onData(is(instanceOf(String.class)))
                .inAdapterView(withId(R.id.suggestion_list))
                .atPosition(0)
                .perform(click());
    }

    //测试页面切换
    @Test
    public void testNavigationView() {
        onView(withId(R.id.fg_poem_title)).check(doesNotExist());
        onView(withId(R.id.bt_nav)).perform(click());
        onView(withId(R.id.navigation)).perform(NavigationViewActions.navigateTo(R.id.menu_item_poem));
        onView(withId(R.id.fg_poem_title)).check(matches(isDisplayed()));
        onView(withId(R.id.bt_nav)).perform(click());
        onView(withId(R.id.navigation)).perform(NavigationViewActions.navigateTo(R.id.menu_item_setting));
        onView(withText("显示设置")).check(matches(isDisplayed()));
        onView(withId(R.id.bt_back)).perform(click());
    }

    //测试收藏点击
    @Test
    public void testStashClick() throws InterruptedException {
        onView(withId(R.id.bt_nav)).perform(click());
        onView(withId(R.id.navigation)).perform(NavigationViewActions.navigateTo(R.id.menu_item_star));
        onData(anything())
                .inAdapterView(withId(R.id.fg_star_lv))
                .atPosition(1)
                .perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.fg_poetry_title)).check(matches(isDisplayed()));
        onView(withId(R.id.img_poetry)).check(matches(isDisplayed()));
    }

    //测试登陆和退出
    @Test
    public void testLogin(){
        onView(withId(R.id.bt_nav)).perform(click());
        onView(withText("点击头像登陆")).check(matches(isDisplayed()));
        onView(withId(R.id.header_touxiang)).perform(click());
        onView(withId(R.id.login_username)).perform(replaceText("板鸭"));
        onView(withId(R.id.login_password)).perform(replaceText("123456"));
        onView(withId(R.id.login_bt)).perform(click());
        onView(withId(R.id.header_user_text)).check(matches(withText("板鸭")));
        onView(withId(R.id.navigation)).perform(NavigationViewActions.navigateTo(R.id.menu_item_logout));
        onView(withId(R.id.header_user_text)).check(matches(withText("点击头像登陆")));
    }
}
