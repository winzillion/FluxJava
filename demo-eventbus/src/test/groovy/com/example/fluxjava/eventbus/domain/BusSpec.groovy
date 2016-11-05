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
package com.example.fluxjava.eventbus.domain

import org.greenrobot.eventbus.Subscribe
import spock.lang.Specification

class BusSpec extends Specification {

    public static class Subscriber {
        Object actualEvent;

        @Subscribe
        public onEvent(String inEvent) {
            this.actualEvent = inEvent;
        }
    }

    def "Test register"() {
        given:
        def target = new Bus()
        def expected = "Test"
        def subscriber = new Subscriber()
        def constants = new Constants()

        target.register(subscriber)

        when: "post an event with unexpected type"
        target.post(0)

        then: "will not get the event"
        subscriber.actualEvent == null
        constants != null

        when: "post an event with expected type"
        target.post(expected)

        then: "get the event"
        subscriber.actualEvent == expected
    }

    def "Test unregister"() {
        given:
        def target = new Bus()
        def expected = "Test"
        def subscriber = new Subscriber()

        target.register(subscriber)

        when: "post an event"
        target.post(expected)

        then: "get the event"
        subscriber.actualEvent == expected

        when: "unregister"
        subscriber.actualEvent = null
        target.unregister(subscriber)
        target.post(expected)

        then: "will not get any event"
        subscriber.actualEvent == null
    }

}