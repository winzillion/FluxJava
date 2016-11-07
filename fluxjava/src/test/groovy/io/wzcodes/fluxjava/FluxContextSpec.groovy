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
package io.wzcodes.fluxjava

import io.wzcodes.fluxjava.ActionCreator
import io.wzcodes.fluxjava.FluxContext
import io.wzcodes.fluxjava.FluxStore
import io.wzcodes.fluxjava.IActionHelper
import io.wzcodes.fluxjava.IFluxBus
import io.wzcodes.fluxjava.IFluxStore
import spock.lang.Specification

import java.lang.reflect.Field

class FluxContextSpec extends Specification {

    private static class StubActionCreator extends ActionCreator {
        StubActionCreator(IFluxBus inDispatcher) {
            super(inDispatcher)
        }
    }

    private static abstract class StubAbsStore extends FluxStore<Object> {
        StubAbsStore(IFluxBus inBus) {
            super(inBus)
        }

        @Override
        Object getItem(int inIndex) {
            return null
        }

        @Override
        int findItem(Object inO) {
            return 0
        }

        @Override
        int getCount() {
            return 0
        }
    }

    private static class StubErrorStore extends StubAbsStore {
        StubErrorStore(IFluxBus inBus) {
            super(inBus)
            throw new Exception()
        }
    }

    static class StubStore extends StubAbsStore {
        StubStore(IFluxBus inBus) {
            super(inBus)
        }
    }

    def setup() {
        Field field = FluxContext.class.getDeclaredField("sInstance");

        field.setAccessible(true);
        // Reset the instance of FluxContext
        field.set(null, null);
    }

    def "Test getInstance"() {
        expect:
        FluxContext.getInstance() == null
    }

    def "Test getBuilder"() {
        given:
        def target = FluxContext.getBuilder()

        target.bus = Mock(IFluxBus)
        if (loop < 2) {
            target.actionCreator = new StubActionCreator(target.bus)
        }
        if (loop > 0) {
            target.actionHelper = Mock(IActionHelper)
        }
        target.storeMap = [0:String.class]
        target.keepStore = true

        expect:
        target.build() != null
        FluxContext.getInstance() != null

        where:
        loop | _
        0    | _
        1    | _
        2    | _
    }

    def "Test getBuilder with error"() {
        given:
        def target

        when: "missing bus"
        target = FluxContext.getBuilder()
        target.actionHelper = Mock(IActionHelper)
        target.storeMap = [0:String.class]
        target.keepStore = true
        target.build()

        then:
        thrown(IllegalArgumentException)

        when: "missing ActionCreator"
        target = FluxContext.getBuilder()
        target.bus = Mock(IFluxBus)
        target.storeMap = [0:String.class]
        target.keepStore = true
        target.build()

        then:
        thrown(IllegalArgumentException)

        when: "missing StoreMap"
        target = FluxContext.getBuilder()
        target.bus = Mock(IFluxBus)
        target.actionHelper = Mock(IActionHelper)
        target.keepStore = true
        target.build()

        then:
        thrown(IllegalArgumentException)
    }

    def "Test getBus"() {
        given:
        def builder = FluxContext.getBuilder()
        def bus = Mock(IFluxBus)

        builder.bus = bus
        builder.actionHelper = Mock(IActionHelper)
        builder.storeMap = [0:String.class]
        builder.keepStore = true

        when:
        builder.build()

        then:
        FluxContext.getInstance().getBus() == bus
    }

    def "Test getStore"() {
        given:
        def storeKeyFail = new Object()
        def storeKeyPass = new Object()
        def expectedTag = new Object()
        def expectedView = new Object()
        def builder = FluxContext.getBuilder()
        def bus = Mock(IFluxBus)
        def actionHelper = Mock(IActionHelper)
        def storeExtra = Mock(IFluxStore)
        def target
        def actual
        def preActual

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [(storeKeyFail):Object.class]
        target = builder.build()

        when: "get the wrong store type"
        actual = target.getStore(storeKeyFail, null, null)

        then: "return null"
        actual == null

        when: "get the right store type"
        target.storeMap = [(storeKeyPass):StubStore.class, (storeKeyFail):Object.class]
        actual = target.getStore(storeKeyPass, null, null)

        then: "return instance of StubStore with null tag"
        actual instanceof StubStore
        actual.tag == null

        when: "get the right store type"
        actual = target.getStore(storeKeyPass, expectedTag, null)

        then: "return instance of StubStore with null tag"
        actual instanceof StubStore
        actual.tag == null

        when: "get the right store type and keepStore"
        target.setKeepStore(true);
        actual = target.getStore(storeKeyPass, null, null);

        then: "return instance of StubStore with null tag"
        actual instanceof StubStore
        actual.tag == null

        when: "get the right store type and keepStore"
        actual = target.getStore(storeKeyPass, expectedTag, null);

        then: "return instance of StubStore with expected tag"
        actual instanceof StubStore
        actual.tag == expectedTag

        when: "keepStore and get store again without tag"
        preActual = actual;
        actual = target.getStore(storeKeyPass, null, null);

        then: "get the same store instance"
        0 * bus.register(_)
        actual == preActual

        when: "keepStore and get store with different key without tag"
        target.storeMap = [(storeKeyPass):StubStore.class, (storeKeyFail):storeExtra.getClass()]
        target.mStoreKeepList.put(new Object(), storeExtra)
        actual = target.getStore(storeKeyFail, null, null)

        then: "get the another store instance"
        actual != preActual

        when: "keepStore and get store again with view passed"
        actual = target.getStore(storeKeyPass, null, expectedView);

        then: "get the same store instance and bus.register is called"
        1 * bus.register(expectedView)
        actual == preActual

        when: "keepStore and get store with different tag"
        actual = target.getStore(storeKeyPass, new Object(), null);

        then: "get different store instance"
        actual != preActual

        when: "keepStore and get store with same tag"
        actual = target.getStore(storeKeyPass, expectedTag, null);

        then: "get the same store instance"
        0 * bus.register(expectedView)
        actual == preActual

        when: "keepStore and get store with same tag and view passed"
        actual = target.getStore(storeKeyPass, expectedTag, expectedView);

        then: "get the same store instance"
        1 * bus.register(expectedView)
        actual == preActual

        when: "keepStore and after clear all stores"
        target.clearAllStore();
        actual = target.getStore(storeKeyPass, expectedTag, null);

        then: "get the different store instance"
        actual != preActual
    }

    def "Test getStore with error"() {
        given:
        def builder = FluxContext.getBuilder()
        def bus = Mock(IFluxBus)
        def actionHelper = Mock(IActionHelper)
        def target
        def actual

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [0:Object.class,
                            1:StubAbsStore.class,
                            2:StubErrorStore.class]
        target = builder.build()

        when:
        actual = target.getStore(loop, null, null)

        then:
        actual == null

        where:
        loop | _
        0    | _
        1    | _
        2    | _
    }

    def "Test registerStore"() {
        given:
        def expectedView = new Object()
        def store = Mock(IFluxStore)
        def bus = Mock(IFluxBus)
        def builder = FluxContext.getBuilder()
        def actionHelper = Mock(IActionHelper)
        def target

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [0:StubStore.class]
        target = builder.build()

        when: "register store without view"
        target.registerStore(store, null);

        then: "only register of bus is called"
        1 * bus.register(store)
        0 * store.register(_)

        when: "register store with view"
        target.registerStore(store, expectedView);

        then: "both register of bus and store are called"
        1 * bus.register(store)
        1 * store.register(expectedView)
    }

    def "Test unregisterStore"() {
        given:
        def expectedView = new Object()
        def store = Mock(IFluxStore)
        def bus = Mock(IFluxBus)
        def builder = FluxContext.getBuilder()
        def actionHelper = Mock(IActionHelper)
        def target

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [0:StubStore.class]
        target = builder.build()

        when: "unregister store without view"
        target.unregisterStore(store, null);

        then: "only unregister of bus is called"
        1 * bus.unregister(store)
        0 * store.unregister(_)

        when: "unregister store with view"
        target.unregisterStore(store, expectedView);

        then: "both unregister of bus and store are called"
        1 * bus.unregister(store)
        1 * store.unregister(expectedView)
    }

}