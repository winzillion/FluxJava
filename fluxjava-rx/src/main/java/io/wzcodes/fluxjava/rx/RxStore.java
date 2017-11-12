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
package io.wzcodes.fluxjava.rx;

import java.util.List;
import java.util.concurrent.Executor;

import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.FluxStore;
import io.wzcodes.fluxjava.IFluxAction;
import io.wzcodes.fluxjava.IFluxBus;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Use RxJava to extends {@link FluxStore} and use {@link RxBus} to pass data change events.
 *
 * @author WZ
 * @version 20171110
 */
public abstract class RxStore<TEntity> extends FluxStore<TEntity> implements IRxDispatch {

    private final RxBus mRxBus = new RxBus();

    /**
     * Constructor.
     *
     * @param inBus {@link IFluxBus} object.
     * @since 2016/7/5
     */
    protected RxStore(final IFluxBus inBus) {
        // Keep the compatibility with FluxContext to get ths instance of store
        super(inBus);
        // Use new instance of RxBus to replace and make every RxStore has it's own bus.
        // So each RxStore can send event individually and decrease the complexity of debug.
        super.changeBus(this.mRxBus);
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void register(final Object inView) {
        // This method is preserved to keep compatibility with FluxContext
        // Use IRxDataChange interface can get rid of RxJava detail
        // Implement IRxDispatch or call toObservable can get the advantage from RxJava
        if (inView != null) {
            if (inView instanceof IRxDataChange) {
                final Subscription subscription = this.mRxBus
                        .toObservable(FluxContext.StoreChangeEvent.class)
                        .subscribe(
                                new Action1<FluxContext.StoreChangeEvent>() {
                                    @Override
                                    public void call(final FluxContext.StoreChangeEvent inEvent) {
                                        ((IRxDataChange)inView).onDataChange(inEvent);
                                    }
                                },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(final Throwable inThrowable) {
                                        ((IRxDataChange)inView).onDataError(inThrowable);
                                    }
                                });

                this.mRxBus.addSubscription(inView, subscription);
            } else if (inView instanceof IRxDispatch) {
                this.mRxBus.register(inView);
            } else {
                throw new IllegalArgumentException("Must implement IRxDataChange or IRxDispatch.");
            }
        }
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void unregister(final Object inView) {
        this.mRxBus.unregister(inView);
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public List<Object> getKeys() {
        return null;
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public Subscription onDispatch(final Object inKey, final Observable<?> inObservable) {
        return null;
    }

    /**
     * @since 2017/11/10
     */
    @Override
    public Subscription onDispatch(final Observable<?> inObservable) {
        // In order to reduce process time of main thread, every action should be sent by using
        // computation thread and therefore .subscribeOn(Schedulers.computation()) is not called here.
        // But computation of RxJava limits the number of threads by the number of CPU core,
        // To maximize, check the link to get more detail:
        // http://tomstechnicalblog.blogspot.com/2016/02/rxjava-maximizing-parallelization.html
        return inObservable
                // Make sure every action can be handled
                .onBackpressureBuffer()
                // Only send the action that store wants
                .ofType(this.getActionType())
                .subscribe(
                        new Action1<IFluxAction>() {
                            @Override
                            public void call(final IFluxAction inAction) {
                                // Due to store could access network or storage,
                                // each action using their own thread to execute.
                                final Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            RxStore.this.onAction(inAction);
                                        } catch (Exception exGeneral) {
                                            RxStore.this.onError(exGeneral);
                                        }
                                    }
                                };
                                final Executor executor = RxStore.this.getExecutor();

                                if (executor != null) {
                                    executor.execute(runnable);
                                } else {
                                    final Thread thread = new Thread(runnable);

                                    thread.start();
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(final Throwable inThrowable) {
                                RxStore.this.onError(inThrowable);
                            }
                        });
    }

    /**
     * Get an instance of {@link Observable}.
     *
     * @param inEventType The type of event want to receive.
     * @param <TEvent> The type of event.
     * @return The instance of {@link Observable}.
     * @since 2016/7/5
     */
    public <TEvent extends FluxContext.StoreChangeEvent> Observable<TEvent> toObservable(final Class<TEvent> inEventType) {
        // Base on input type return the observer that receive specific event
        return this.mRxBus.toObservable(inEventType);
    }

    /**
     * Get a thread pool that will be used in dispatch actions.
     *
     * @return The instance of {@link Executor}.
     * @since 2017/2/8
     */
    protected Executor getExecutor() {
        return null;
    }

    /**
     * Called by ths dispatcher, it can help the dispatcher to filter action for specific store.
     * The class type of action has to implement  {@link IFluxAction}.
     *
     *
     * @return The class type of action that want to filter in bus.
     * @since 2016/7/5
     */
    abstract protected Class<? extends IFluxAction> getActionType();

    /**
     * Handle the request from the dispatcher.
     *
     * @param inAction The action object.
     * @param <TAction> The class type of action.
     * @since 2016/7/5
     */
    abstract protected <TAction extends IFluxAction> void onAction(TAction inAction);

    /**
     * Handle the error from the dispatcher.
     *
     * @param inThrowable The error object.
     * @since 2016/7/5
     */
    abstract protected void onError(Throwable inThrowable);

}
