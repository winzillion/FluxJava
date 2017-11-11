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

import io.wzcodes.fluxjava.IFluxBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Use RxJava to implement {@link IFluxBus}.
 *
 * @author WZ
 * @version 20160705
 */
public class RxBus implements IFluxBus {

    private static volatile RxBus sInstance;
    private final Subject<Object, Object> mBus;
    private final Map<Object, Subscription> mSubscriptionManager = new HashMap<>();

    /**
     * Constructor.
     *
     * @since 2016/7/5
     */
    public RxBus() {
        // Subject that, once an Observer has subscribed,
        // emits all subsequently observed items to the subscriber.
        // PublishSubject is not thread-safe,
        // PublishSubject need to be converted into SerializedSubject
        this.mBus = new SerializedSubject<>(PublishSubject.create());
    }

    /**
     * Get the default instance of {@link RxBus}.
     *
     * @return The instance of {@link RxBus}.
     * @since 2016/7/5
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
     * @since 2016/7/5
     */
    public <TEvent> Observable<TEvent> toObservable(final Class<TEvent> inEventType) {
        // Filter events by specific type passed in
        return this.mBus.ofType(inEventType);
    }

    /**
     * @since 2016/7/5
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
                final Subscription subscription;

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
     * @since 2016/7/5
     */
    @Override
    public void unregister(final Object inSubscriber) {
        final IRxDispatch subscriber = (IRxDispatch)inSubscriber;
        final List<Object> keys = new ArrayList<>();

        keys.add(inSubscriber);
        if (subscriber.getKeys() != null) {
            keys.addAll(subscriber.getKeys());
        }

        for (Object key : keys) {
            this.removeSubscription(key);
        }
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void post(final Object inEvent) {
        if (this.mBus.hasObservers()) {
            this.mBus.onNext(inEvent);
        }
    }

    /**
     * Add a {@link Subscription} object into list that keep in {@link RxBus}. It can add additional
     * {@link Subscription} after called {@link RxBus#register(Object)}.
     *
     * @param inKey An object to identify {@link Subscription} object.
     * @param inSubscription {@link Subscription} object.
     * @since 2016/7/5
     */
    public void addSubscription(final Object inKey, final Subscription inSubscription) {
        if (inKey != null && inSubscription != null && inSubscription.isUnsubscribed() == false) {
            final Subscription oldSubscription = this.mSubscriptionManager.put(inKey, inSubscription);

            if (oldSubscription != null && oldSubscription.isUnsubscribed() == false) {
                oldSubscription.unsubscribe();
            }
        }
    }

    /**
     * Remove a {@link Subscription} object from list that keep in {@link RxBus}
     * that is not called by {@link RxBus#register(Object)}.
     *
     * @param inKey An object to identify {@link Subscription} object.
     * @since 2016/7/5
     */
    public void removeSubscription(final Object inKey) {
        final Subscription subscription = this.mSubscriptionManager.remove(inKey);

        if (subscription != null && subscription.isUnsubscribed() == false) {
            subscription.unsubscribe();
        }
    }

}
