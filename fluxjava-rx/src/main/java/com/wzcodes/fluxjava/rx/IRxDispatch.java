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
package com.wzcodes.fluxjava.rx;

import java.util.List;

import rx.Observable;
import rx.Subscription;

/**
 * An interface used to register to {@link RxBus}.
 * The stores want to get event from {@link RxBus} need to implement this interface in order to
 * interact with {@link RxBus}.
 *
 * @author WZ
 * @version 20160705
 */
public interface IRxDispatch {

    /**
     * When there are more then one {@link Subscription}, get the key list of subscriptions.
     *
     * @return The keys that map to each {@link Subscription}.
     * @since 2016/7/5
     */
    List<Object> getKeys();

    /**
     * Handle the subscription request sent from {@link RxBus}.
     * {@link Observable} can be used to customize which event to receive and how to process.<br />
     * The key is used to manage {@link Subscription} objects for {@link RxBus}.
     * It does not allow key duplication in the same or different class
     * that implements {@link IRxDispatch}.
     *
     * @param inKey The key to identity {@link Subscription} object.
     * @param inObservable Then {@link Observable} object.
     * @return The {@link Subscription} object.
     * @since 2016/7/5
     */
    Subscription onDispatch(Object inKey, Observable<?> inObservable);

    /**
     * Handle the subscription request sent from {@link RxBus}.
     * {@link Observable} can be used to customize which event to receive and how to process.
     *
     * @param inObservable Then {@link Observable} object.
     * @return The {@link Subscription} object.
     * @since 2016/7/5
     */
    Subscription onDispatch(Observable<?> inObservable);

}
