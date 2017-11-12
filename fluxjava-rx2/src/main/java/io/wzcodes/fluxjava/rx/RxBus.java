package io.wzcodes.fluxjava.rx;


import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.wzcodes.fluxjava.IFluxBus;

/**
 * Use RxJava to implement {@link IFluxBus}.
 *
 * @author WZ
 * @version 20171111
 */
public class RxBus implements IFluxBus {

    private static volatile RxBus sInstance;
    private final Relay<Object> mBus;
    private final Map<Object, Disposable> mSubscriptionManager = new HashMap<>();

    /**
     * Constructor.
     *
     * @since 2017/11/11
     */
    public RxBus() {
        // Subject that, once an Observer has subscribed,
        // emits all subsequently observed items to the subscriber.
        // PublishRelay is not thread-safe,
        // PublishRelay need to be converted into SerializedSubject
        this.mBus = PublishRelay.create().toSerialized();
    }

    /**
     * Get the default instance of {@link RxBus}.
     *
     * @return The instance of {@link RxBus}.
     * @since 2017/11/11
     */
    public static RxBus getDefault() {
        if (RxBus.sInstance == null) {
            synchronized (RxBus.class) {
                if (RxBus.sInstance == null) {
                    RxBus.sInstance = new RxBus();
                }
            }
        }
        return RxBus.sInstance ;
    }

    /**
     * Get an {@link Observable} object. It can be used to receive events from {@link RxBus}.
     *
     * @param inEventType Filter events by specific type.
     * @param <TEvent> Class type of specific event.
     * @return The instance of {@link Observable}.
     * @since 2017/11/11
     */
    public <TEvent> Observable<TEvent> toObservable(final Class<TEvent> inEventType) {
        return this.mBus.ofType(inEventType);
    }

    /**
     * Get an {@link Flowable} object. It can be used to receive events from {@link RxBus}.
     *
     * @param inEventType Filter events by specific type.
     * @param inStrategy Represents the options for applying backpressure to a source sequence.
     * @param <TEvent> Class type of specific event.
     * @return The instance of {@link Flowable}.
     * @since 2017/11/11
     */
    public <TEvent> Flowable<TEvent> toFlowable(final Class<TEvent> inEventType, final BackpressureStrategy inStrategy) {
        return this.mBus.toFlowable(inStrategy).ofType(inEventType);
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public void register(final Object inSubscriber) {
        if (inSubscriber instanceof IRxDispatch) {
            final IRxDispatch subscriber = (IRxDispatch)inSubscriber;
            final List<Object> keys = new ArrayList<>();

            keys.add(inSubscriber);
            if (subscriber.getKeys() != null) {
                keys.addAll(subscriber.getKeys());
            }

            for (Object key : keys) {
                final Disposable subscription;

                if (key == inSubscriber) {
                    subscription = subscriber.onDispatch(this.mBus);
                } else {
                    subscription = subscriber.onDispatch(key, this.mBus);
                }

                if (subscription != null) {
                    this.mSubscriptionManager.put(key, subscription);
                }
            }
        } else {
            throw new IllegalArgumentException("Must implement IRxDispatch interface.");
        }
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public void unregister(final Object inSubscriber) {
        final List<Object> keys = new ArrayList<>();

        keys.add(inSubscriber);
        if (inSubscriber instanceof IRxDispatch) {
            final IRxDispatch subscriber = (IRxDispatch)inSubscriber;

            if (subscriber.getKeys() != null) {
                keys.addAll(subscriber.getKeys());
            }
        }

        for (Object key : keys) {
            this.removeSubscription(key);
        }
    }

    /**
     * @since 2017/11/11
     */
    @Override
    public void post(final Object inEvent) {
        if (this.mBus.hasObservers()) {
            this.mBus.accept(inEvent);
        }
    }

    /**
     * Add a {@link Disposable} object into list that keep in {@link RxBus}. It can add additional
     * {@link Disposable} after called {@link RxBus#register(Object)}.
     *
     * @param inKey An object to identify {@link Disposable} object.
     * @param inSubscription {@link Disposable} object.
     * @since 2017/11/11
     */
    public void addSubscription(final Object inKey, final Disposable inSubscription) {
        if (inKey != null && inSubscription != null && inSubscription.isDisposed() == false) {
            final Disposable oldSubscription = this.mSubscriptionManager.put(inKey, inSubscription);

            if (oldSubscription != null && oldSubscription.isDisposed() == false) {
                oldSubscription.dispose();
            }
        }
    }

    /**
     * Remove a {@link Disposable} object from list that keep in {@link RxBus}
     * that is not called by {@link RxBus#register(Object)}.
     *
     * @param inKey An object to identify {@link Disposable} object.
     * @since 2017/11/11
     */
    public void removeSubscription(final Object inKey) {
        final Disposable subscription = this.mSubscriptionManager.remove(inKey);

        if (subscription != null && subscription.isDisposed() == false) {
            subscription.dispose();
        }
    }

}
