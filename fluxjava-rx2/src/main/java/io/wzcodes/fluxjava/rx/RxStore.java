package io.wzcodes.fluxjava.rx;

import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.FluxStore;
import io.wzcodes.fluxjava.IFluxAction;
import io.wzcodes.fluxjava.IFluxBus;

/**
 * Use RxJava to extends {@link FluxStore} and use {@link RxBus} to pass data change events.
 *
 * @author WZ
 * @version 20171111
 */
public abstract class RxStore<TEntity> extends FluxStore<TEntity> implements IRxDispatch {

    private final RxBus mRxBus = new RxBus();

    /**
     * Constructor.
     *
     * @param inBus {@link IFluxBus} object.
     * @since 2017/11/11
     */
    protected RxStore(final IFluxBus inBus) {
        // Keep the compatibility with FluxContext to get ths instance of store
        super(inBus);
        // Use new instance of RxBus to replace and make every RxStore has it's own bus.
        // So each RxStore can send event individually and decrease the complexity of debug.
        super.changeBus(this.mRxBus);
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public void register(final Object inView) {
        // This method is preserved to keep compatibility with FluxContext
        // Use IRxDataChange interface can get rid of RxJava detail
        // Implement IRxDispatch or call toObservable can get the advantage from RxJava
        if (inView != null) {
            if (inView instanceof IRxDataChange) {
                final Disposable subscription = this.mRxBus
                        .toObservable(FluxContext.StoreChangeEvent.class)
                        .subscribe(
                                new Consumer<FluxContext.StoreChangeEvent>() {
                                    @Override
                                    public void accept(final FluxContext.StoreChangeEvent inEvent) throws Exception {
                                        ((IRxDataChange) inView).onDataChange(inEvent);
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(final Throwable inThrowable) throws Exception {
                                        ((IRxDataChange) inView).onDataError(inThrowable);
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
     * @since 2017/11/11
     */
    @Override
    public void unregister(final Object inView) {
        this.mRxBus.unregister(inView);
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public List<Object> getKeys() {
        return null;
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public Disposable onDispatch(final Object inKey, final Observable<?> inObservable) {
        return null;
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public Disposable onDispatch(final Observable<?> inObservable) {
        // In order to reduce process time of main thread, every action should be sent by using
        // computation thread and therefore .subscribeOn(Schedulers.computation()) is not called here.
        // But computation of RxJava limits the number of threads by the number of CPU core,
        // To maximize, check the link to get more detail:
        // http://tomstechnicalblog.blogspot.com/2016/02/rxjava-maximizing-parallelization.html
        return inObservable
                // Only send the action that store wants
                .ofType(this.getActionType())
                .subscribe(
                        new Consumer<IFluxAction>() {
                            @Override
                            public void accept(final IFluxAction inAction) throws Exception {
                                // Due to store could access network or storage,
                                // each action using their own thread to execute.
                                // But RxJava use single-thread to send every action
                                // See also:
                                // http://tomstechnicalblog.blogspot.tw/2015/11/rxjava-achieving-parallelization.html
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
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(final Throwable inThrowable) throws Exception {
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
     * @since 2017/11/11
     */
    public <TEvent extends FluxContext.StoreChangeEvent> Observable<TEvent> toObservable(final Class<TEvent> inEventType) {
        // Base on input type return the observer that receive specific event
        return this.mRxBus.toObservable(inEventType);
    }

    /**
     * Get an instance of {@link Flowable}.
     *
     * @param inEventType The type of event want to receive.
     * @param inStrategy Represents the options for applying backpressure to a source sequence.
     * @param <TEvent> The type of event.
     * @return The instance of {@link Flowable}.
     * @since 2017/11/11
     */
    public <TEvent extends FluxContext.StoreChangeEvent> Flowable<TEvent> toFlowable(final Class<TEvent> inEventType, final BackpressureStrategy inStrategy) {
        return this.mRxBus.toFlowable(inEventType, inStrategy);
    }

    /**
     * Get a thread pool that will be used in dispatch actions.
     *
     * @return The instance of {@link Executor}.
     * @since 2017/11/11
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
     * @since 2017/11/11
     */
    abstract protected Class<? extends IFluxAction> getActionType();

    /**
     * Handle the request from the dispatcher.
     *
     * @param inAction The action object.
     * @param <TAction> The class type of action.
     * @since 2017/11/11
     */
    abstract protected <TAction extends IFluxAction> void onAction(TAction inAction);

    /**
     * Handle the error from the dispatcher.
     *
     * @param inThrowable The error object.
     * @since 2017/11/11
     */
    abstract protected void onError(Throwable inThrowable);

}
