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
package io.wzcodes.fluxjava.rx

import rx.Observable
import rx.Subscription
import rx.functions.Action1
import spock.lang.Specification

class RxBusSpec extends Specification {

    def mTarget = RxBus.getDefault()

    def "Test constructor"() {
        given:
        this.mTarget = new RxBus()

        expect:
        this.mTarget.toObservable(Object.class) != null
    }

    def "Test toObservable"() {
        expect:
        this.mTarget.toObservable(Object.class) != null
    }

    def "Test register"() {
        given:
        def keys = new ArrayList<>()
        def store = Mock(IRxDispatch)
        def subscription = Mock(Subscription)

        store.getKeys() >>> [null, keys]

        when: "register a wrong type"
        this.mTarget.register(new Object())

        then: "get an exception"
        thrown(IllegalArgumentException)

        when: "Store with single subscription"
        this.mTarget.register(store)

        then: "Store get the bus"
        1 * store.onDispatch(_ as Observable)

        when: "Store with multiple subscriptions"
        keys.add(new Object())
        keys.add(new Object())
        store.onDispatch(_ as Observable) >> subscription
        this.mTarget.register(store)

        then: "Store get the bus several times"
        1 * store.onDispatch(_ as Observable)
        2 * store.onDispatch(_, _ as Observable)
    }

    def "Test unregister"() {
        given:
        def keys = new ArrayList<>()
        def store = Mock(IRxDispatch)
        def subscription = Mock(Subscription)
        def view = new Object()
        
        store.getKeys() >>> [null, keys, keys, null, null, keys]
        store.onDispatch(_ as Observable) >> subscription
        store.onDispatch(_, _ as Observable) >> subscription
        subscription.isUnsubscribed() >>> [false, true, false]

        when: "Store with single subscription"
        this.mTarget.register(store)

        and: "unregister store"
        keys.add(new Object())
        this.mTarget.unregister(store)

        then: "Store is unsubscribed"
        1 * subscription.unsubscribe()

        when: "Store with single subscription"
        this.mTarget.register(store)

        and: "unregister store with unsubscribed"
        this.mTarget.unregister(store)

        then: "Store is not unsubscribed"
        0 * subscription.unsubscribe()

        when: "Store with multiple subscriptions"
        this.mTarget.register(store)

        and: "unregister store"
        this.mTarget.unregister(store)

        then: "Store is unsubscribed several times"
        2 * subscription.unsubscribe()

        when: "register a non-IRxDispatch object"
        this.mTarget.addSubscription(view, subscription)

        and: "unregister it"
        this.mTarget.unregister(view)

        then: "it is unsubscribed"
        1 * subscription.unsubscribe()
    }

    def "Test post"() {
        given:
        def expected = new ArrayList<>()
        def expectedEvent = new Object()
        def store = new IRxDispatch() {
            @Override
            public List<Object> getKeys() {
                return null
            }

            @Override
            public Subscription onDispatch(Object inKey, Observable<?> inObservable) {
                return null
            }

            @Override
            public Subscription onDispatch(Observable<?> inObservable) {
                return inObservable.subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object inEvent) {
                        expected.add(inEvent)
                    }
                })
            }
        }

        this.mTarget.post(expectedEvent)
        this.mTarget.register(store)

        when:
        this.mTarget.post(expectedEvent)

        then:
        expected.size() == 1
        expected.get(0) == expectedEvent
    }

    def "Test addSubscription"() {
        given:
        def keys = new ArrayList<>()
        def store = Mock(IRxDispatch)
        def oldSubscription = Mock(Subscription)
        def newSubscription = Mock(Subscription)

        keys.add(new Object())
        store.getKeys() >> keys
        store.onDispatch(_ as Observable) >> oldSubscription
        store.onDispatch(_, _ as Observable) >> oldSubscription
        oldSubscription.isUnsubscribed() >> false
        newSubscription.isUnsubscribed() >> false
        this.mTarget.register(store)

        when: "Subscribe a null key"
        this.mTarget.addSubscription(null, null)

        then:
        0 * oldSubscription.unsubscribe()

        when: "Subscribe a null subscription"
        this.mTarget.addSubscription(new Object(), null)

        then:
        0 * oldSubscription.unsubscribe()

        when: "Subscribe a different key"
        this.mTarget.addSubscription(new Object(), newSubscription)

        then:
        0 * oldSubscription.unsubscribe()

        when: "Subscribe a exist key"
        this.mTarget.addSubscription(keys.get(0), newSubscription)

        then:
        1 * oldSubscription.unsubscribe()
    }

    def "Test removeSubscription"() {
        given:
        this.mTarget.removeSubscription(null)
    }

}