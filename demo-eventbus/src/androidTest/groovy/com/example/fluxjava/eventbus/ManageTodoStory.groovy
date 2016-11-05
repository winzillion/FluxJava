/*
 * Copyright (C) 2016 Bugs will find a way (https://wznote.blogspot.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.fluxjava.eventbus

import android.graphics.Paint
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import org.junit.Rule
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import static android.support.test.espresso.Espresso.onView
import static android.support.test.espresso.action.ViewActions.*
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import static android.support.test.espresso.assertion.ViewAssertions.matches
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import static android.support.test.espresso.matcher.RootMatchers.isDialog
import static android.support.test.espresso.matcher.ViewMatchers.*
import static org.hamcrest.Matchers.allOf

@Title("Manage todo items")
@Narrative("""
As a user
I want to manage todo items
So I can track something to be done
""")
class ManageTodoStory extends Specification {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class)
    private RecyclerView mRecyclerView

    def "Add a todo"() {
        when: "I tap the add menu on main activity"
        this.mRecyclerView = (RecyclerView)this.mActivityRule.activity.findViewById(R.id.recyclerView)
        onView(withId(R.id.add)).perform(click())

        then: "I see the add todo screen"
        onView(withText(R.string.dialog_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withText(R.string.title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withId(R.id.title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withText(R.string.memo))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withId(R.id.memo))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withText(R.string.due_date))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withId(R.id.dueText))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1))
                .inRoot(isDialog())
                .check(matches(withText(R.string.add)))
                .check(matches(isDisplayed()))
        onView(withId(android.R.id.button2))
                .inRoot(isDialog())
                .check(matches(withText(android.R.string.cancel)))
                .check(matches(isDisplayed()))

        when: "I input todo detail and press ADD button"
        onView(withId(R.id.title))
                .perform(typeText("Test title"))
        onView(withId(R.id.memo))
                .perform(typeText("Sample memo"))
        onView(withId(R.id.dueText))
                .perform(typeText("2016/1/1"), closeSoftKeyboard())
        onView(withId(android.R.id.button1)).perform(click())

        then: "I see a new entry in list"
        onView(withText(R.string.dialog_title))
                .check(doesNotExist())
        this.mRecyclerView.getAdapter().itemCount == 5
        onView(withId(R.id.recyclerView)).perform(scrollToPosition(4))
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText("Test title"))))
    }

    def "Add a todo, but cancel the action"() {
        when: "I tap the add menu on main activity"
        this.mRecyclerView = (RecyclerView)this.mActivityRule.activity.findViewById(R.id.recyclerView)
        onView(withId(R.id.add)).perform(click())

        and: "I press cancel button"
        onView(withId(android.R.id.button2)).perform(click())

        then: "Nothing happen"
        this.mRecyclerView.getAdapter().itemCount == 4
    }

    def "Switch user"() {
        when: "I select a different user"
        this.mRecyclerView = (RecyclerView)this.mActivityRule.activity.findViewById(R.id.recyclerView)
        onView(withId(R.id.spinner)).perform(click())
        onView(allOf(withText("User2"), isDisplayed()))
                .perform(click())

        then: "I see the list changed"
        this.mRecyclerView.getAdapter().itemCount == 5
    }

    def "Mark a todo as done"() {
        when: "I mark a todo as done"
        TextView target

        this.mRecyclerView = (RecyclerView)this.mActivityRule.activity.findViewById(R.id.recyclerView)
        onView(withRecyclerView(R.id.recyclerView).atPositionOnView(2, R.id.closed))
                .perform(click())
        target = (TextView)this.mRecyclerView
                .findViewHolderForAdapterPosition(2)
                .itemView
                .findViewById(R.id.title)

        then: "I see the todo has a check mark and strike through on title"
        target.getText().toString().equals("Test title 2")
        (target.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0
    }

    static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId)
    }

}