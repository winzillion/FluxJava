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
package com.example.fluxjava.rx

import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import io.wzcodes.fluxjava.rx.RxBus
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog
import org.robospock.GradleRoboSpecification

@Config(constants = BuildConfig, sdk = 21, application = StubAppConfig)
class MainActivitySpec extends GradleRoboSpecification {

    def "Test activity lifecycle"() {
        given:
        def controller = Robolectric.buildActivity(MainActivity).create().start().resume().visible()
        def target = controller.get()
        Spinner spinner = target.findViewById(R.id.spinner)
        RecyclerView recyclerView = target.findViewById(R.id.recyclerView)

        // workaround robolectric recyclerView issue
        // this will fix recyclerView won't refresh after get notifyDataSetChanged from adapter
        recyclerView.measure(0, 0)
        recyclerView.layout(0, 0, 100, 1000)

        expect: "init state"
        target != null
        target.getTitle() == "Demo"
        spinner != null
        spinner.getAdapter().count == 2
        recyclerView != null
        RxBus.getDefault().mSubscriptionManager.size() == 2
        recyclerView.getAdapter().getItemCount() == 4
        recyclerView.getLayoutManager().getChildCount() > 0

        when: "todo mark as done"
        def checkbox = (CheckBox)recyclerView
                .findViewHolderForAdapterPosition(2)
                .itemView
                .findViewById(R.id.closed)
        def title = (TextView)recyclerView
                .findViewHolderForAdapterPosition(2)
                .itemView
                .findViewById(R.id.title)
        checkbox.performClick()
        // force ViewHolder to rebind data
        // in order to check if view is changed by code
        recyclerView.getAdapter().onBindViewHolder(
                recyclerView.findViewHolderForAdapterPosition(2),
                2)

        then: "strike through on title text"
        title.text == "Test title 2"
        (title.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0

        when: "menu add clicked"
        def nullItem = new RoboMenuItem()
        def addItem = new RoboMenuItem() {
            public int getItemId() {
                return R.id.add;
            }
        }

        target.onOptionsItemSelected(nullItem)
        target.onOptionsItemSelected(addItem)

        then: "show dialog"
        target.getSupportFragmentManager().findFragmentByTag("Add") != null
        ShadowDialog.latestDialog != null

        when: "activity shutdown"
        controller.stop()

        then: "resources were released"
        RxBus.getDefault().mSubscriptionManager.size() == 0
    }

}