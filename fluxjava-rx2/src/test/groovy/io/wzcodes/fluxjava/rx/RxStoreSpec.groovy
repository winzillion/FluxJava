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

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.functions.Consumer
import io.wzcodes.fluxjava.FluxAction
import io.wzcodes.fluxjava.FluxContext
import io.wzcodes.fluxjava.IFluxAction
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
        private boolean mFailed

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
                this.mExecutor = Executors.newFixedThreadPool(inCount)
            } else {
                this.mExecutor = null
            }
        }

        void mockExecutor(final Executor inExecutor) {
            this.mExecutor = inExecutor
        }

        void setFailedOnAction() {
            this.mFailed = true
        }

        void clearFailedOnAction() {
            this.mFailed = false
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
            if (this.mFailed) {
                throw new RuntimeException("onActionError")
            } else {
                this.mActionMap.put(Thread.currentThread().id, inAction)
            }
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

    def "Test register with IRxDispatch"() {
        given:
        def view = Mock(IRxDispatch)
        def target = new StubRxStore()

        expect: "when view is null and nothing happen"
        target.register(null)

        when: "register a view implements IRxDispatch"
        target.register(view)

        then: "registered by using bus"
        1 * view.getKeys() >> null
        1 * view.onDispatch(_)
        0 * view.onDispatch(_, _)
    }

    def "Test register with IRxDataChange"() {
        given:
        def expected = 0
        def view = Mock(IRxDataChange)
        def expectedEvent = new StubAddEvent()
        def target = new StubRxStore()

        expect: "when view is null and nothing happen"
        target.register(null)

        when: "view is not implement IRxDataChange"
        target.register(new Object())

        then: "get an exception"
        thrown(IllegalArgumentException)

        when: "register a view and emmitChange"
        target.register(view)
        target.emitChange(expectedEvent);

        then: "view get the event"
        1 * view.onDataChange(expectedEvent)
        0 * view.onDataError(_)

        when: "emmitChange and throw exception during execution"
        target.emitChange(expectedEvent)

        then: "view get the event and error"
        1 * view.onDataChange(expectedEvent) >> { throw new RuntimeException() }
        1 * view.onDataError(_ as RuntimeException)

        when: "unregister specific view and emmitChange"
        target.unregister(view)
        target.emitChange(expectedEvent)

        then: "view will not get any event"
        0 * view.onDataChange(_)
        0 * view.onDataError(_)

        when: "register a view from toObservable and emmitChange"
        target.toObservable(StubAddEvent.class)
                .subscribe(
                new Consumer<StubAddEvent>() {
                    @Override
                    void accept(StubAddEvent inEvent) throws Exception {
                        if (inEvent == expectedEvent) {
                            expected++
                        }
                    }
                });
        target.emitChange(expectedEvent);
        target.emitChange(new StubRemoveEvent());

        then: "view get the event and only the event"
        expected == 1

        when: "register a view from toFlowable and emmitChange"
        expected = 0
        target = new StubRxStore()
        target.toFlowable(StubAddEvent.class, BackpressureStrategy.BUFFER)
                .subscribe(
                new Consumer<StubAddEvent>() {
                    @Override
                    void accept(StubAddEvent inEvent) throws Exception {
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
        def executor = Mock(Executor)
        def target

        when:
        bus = PublishRelay.create().toSerialized()
        target = new StubRxStore(StubAction.class, expectedActions, expectedErrors)
        target.onDispatch(bus)
        bus.accept(new Object());
        bus.accept(Mock(IFluxAction));
        bus.accept(new StubAction("", null));
        bus.accept(new StubAction("", null));
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
        bus.accept(new StubAction("", null));
        bus.accept(new StubAction("", null));
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
        bus.accept(new StubAction("", null));
        bus.accept(new StubAction("", null));
        bus.accept(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "3 actions will run in their own thread"
        expectedActions.size() == 3
        expectedErrors.size() == 0

        when: "something go wrong inside onDispatch before onAction"
        expectedActions.clear()
        bus = PublishRelay.create().toSerialized()
        target = new StubRxStore(null, expectedActions, expectedErrors)
        target.onDispatch(bus)
        bus.accept(new StubAction("", null));

        then: "RxStore get the error"
        thrown NullPointerException

        when: "something go wrong with executor submit a task"
        expectedActions.clear()
        expectedErrors.clear()
        bus = PublishRelay.create().toSerialized()
        target = new StubRxStore(StubAction.class, expectedActions, expectedErrors)
        target.mockExecutor(executor)
        executor.execute(_) >> { throw new RuntimeException("ExecutorError") }
        target.onDispatch(bus)
        bus.accept(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "RxStore get the error"
        expectedActions.size() == 0
        expectedErrors.size() == 1
        expectedErrors.get(0) instanceof RuntimeException
        expectedErrors.get(0).message == "ExecutorError"

        when: "something go wrong inside onAction by using thread pool"
        expectedActions.clear()
        expectedErrors.clear()
        bus = PublishRelay.create().toSerialized()
        target = new StubRxStore(StubAction.class, expectedActions, expectedErrors)
        target.setThreadPool(1)
        target.setFailedOnAction()
        target.onDispatch(bus)
        bus.accept(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "RxStore get the error"
        expectedActions.size() == 0
        expectedErrors.size() == 1
        expectedErrors.get(0) instanceof RuntimeException
        expectedErrors.get(0).message == "onActionError"

        when: "after RxStore get an error then send new action"
        expectedActions.clear()
        expectedErrors.clear()
        target.clearFailedOnAction()
        bus.accept(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "RxStore get the action"
        expectedActions.size() == 1
        expectedErrors.size() == 0

        when: "something go wrong inside onAction by using new thread"
        expectedActions.clear()
        expectedErrors.clear()
        bus = PublishRelay.create().toSerialized()
        target = new StubRxStore(StubAction.class, expectedActions, expectedErrors)
        target.setFailedOnAction()
        target.onDispatch(bus)
        bus.accept(new StubAction("", null));
        try {
            // wait for onDispatch complete
            Thread.sleep(100);
        } catch (InterruptedException exInterrupted) {
            fail("Test was interrupted for " + exInterrupted.toString());
        }

        then: "RxStore get the error"
        expectedActions.size() == 0
        expectedErrors.size() == 1
        expectedErrors.get(0) instanceof RuntimeException
        expectedErrors.get(0).message == "onActionError"
    }

    def "Test onDispatch with keys"() {
        given:
        def target = new StubRxStore();

        expect:
        target.onDispatch(null, null) == null
    }

}