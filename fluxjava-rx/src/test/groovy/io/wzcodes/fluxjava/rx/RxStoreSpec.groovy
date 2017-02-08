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

import io.wzcodes.fluxjava.FluxAction
import io.wzcodes.fluxjava.FluxContext
import io.wzcodes.fluxjava.IFluxAction
import rx.functions.Action1
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import spock.lang.Specification

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RxStoreSpec extends Specification {

    private static class StubAddEvent implements FluxContext.StoreChangeEvent {}

    private static class StubRemoveEvent implements FluxContext.StoreChangeEvent {}

    private static class StubAction extends FluxAction<String, String> {
        StubAction(String inType, String inData) {
            super(inType, inData)
        }
    }

    private static class StubRxStore extends RxStore<Object> {
        private Map<Long, ?> mActionMap
        private List<?>  mErrorList
        private Class<?> mActionType
        private Executor mExecutor

        StubRxStore() {
            super(null)
        }

        StubRxStore(Class<?> inActionType, Map<Long, ?> inActions, List<?> inErrors) {
            super(null);
            this.mActionType = inActionType
            this.mActionMap = inActions
            this.mErrorList = inErrors
        }

        void setThreadPool(final int inCount) {
            if (inCount > 0) {
                this.mExecutor = Executors.newFixedThreadPool(inCount);
            } else {
                this.mExecutor = null
            }
        }

        @Override
        void emitChange(final FluxContext.StoreChangeEvent inEvent) {
            super.emitChange(inEvent)
        }

        @Override
        protected Class<? extends IFluxAction> getActionType() {
            return this.mActionType
        }

        @Override
        protected Executor getExecutor() {
            if (this.mExecutor != null) {
                return this.mExecutor
            } else {
                super.getExecutor()
            }
        }

        @Override
        protected <TAction extends IFluxAction> void onAction(TAction inAction) {
            this.mActionMap.put(Thread.currentThread().id, inAction)
        }

        @Override
        protected void onError(Throwable inThrowable) {
            this.mErrorList.add(inThrowable)
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

    def "Test register"() {
        given:
        def expected = 0
        def view = Mock(IRxDataChange)
        def expectedEvent = new StubAddEvent()
        def target = new StubRxStore()

        expect: "when view is null and nothing happen"
        target.register(null);

        when: "view is not implement IRxDataChange"
        target.register(new Object())

        then: "get an exception"
        thrown(IllegalArgumentException)

        when: "register a view and emmitChange"
        target.register(view);
        target.emitChange(expectedEvent);

        then: "view get the event"
        1 * view.onDataChange(expectedEvent)

        when: "unregister specific view and emmitChange"
        target.unregister(view);
        target.emitChange(expectedEvent);

        then: "view will not get any event"
        0 * view.onDataChange(_)

        when: "register a view from toObservable and emmitChange"
        target.toObservable(StubAddEvent.class)
                .subscribe(
                new Action1<StubAddEvent>() {
                    @Override
                    public void call(StubAddEvent inEvent) {
                        if (inEvent == expectedEvent) {
                            expected++
                        }
                    }
                });
        target.emitChange(expectedEvent);
        target.emitChange(new StubRemoveEvent());

        then: "view get the event and only the event"
        expected == 1
    }

    def "Test getKeys"() {
        given:
        def target = new StubRxStore()

        expect:
        target.getKeys() == null
    }

    def "Test onDispatch"() {
        given:
        def expectedActions = new HashMap<Long, ?>()
        def expectedErrors = new ArrayList<>()
        def bus
        def target

        when:
        bus = new SerializedSubject<>(PublishSubject.create())
        target = new StubRxStore(StubAction.class, expectedActions, expectedErrors)
        target.onDispatch(bus)
        bus.onNext(new Object());
        bus.onNext(Mock(IFluxAction));
        bus.onNext(new StubAction("", null));
        bus.onNext(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "RxStore only get the event which type is specified action type"
        expectedActions.size() == 2
        expectedErrors.size() == 0

        when: "using thread pool with one thread"
        expectedActions.clear()
        target.setThreadPool(1)
        bus.onNext(new StubAction("", null));
        bus.onNext(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "only one thread will be created"
        expectedActions.size() == 1
        expectedErrors.size() == 0

        when: "using thread pool with 3 threads"
        expectedActions.clear()
        target.setThreadPool(3)
        bus.onNext(new StubAction("", null));
        bus.onNext(new StubAction("", null));
        bus.onNext(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "3 actions will run in their own thread"
        expectedActions.size() == 3
        expectedErrors.size() == 0

        when: "something go wrong inside onDispatch"
        expectedActions.clear()
        bus = new SerializedSubject<>(PublishSubject.create())
        target = new StubRxStore(null, expectedActions, expectedErrors)
        target.onDispatch(bus)
        bus.onNext(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "ReStore get the error"
        expectedActions.size() == 0
        expectedErrors.size() == 1
    }

    def "Test onDispatch with keys"() {
        given:
        def target = new StubRxStore();

        expect:
        target.onDispatch(null, null) == null
    }

}