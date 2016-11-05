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

import com.wzcodes.fluxjava.FluxContext;

/**
 * An interface used to register to {@link RxStore}.
 * If you use {@link RxStore#toObservable(Class)} to listen events then you won't need this interface.
 *
 * @author WZ
 * @version 20160705
 */
public interface IRxDataChange {

    /**
     * Handle the data change emitted by {@link RxStore}.
     *
     * @param inEvent The {@link FluxContext.StoreChangeEvent} object that provide change information.
     * @since 2016/7/5
     */
    void onDataChange(FluxContext.StoreChangeEvent inEvent);

    /**
     * Handle the error emitted by {@link RxStore}.
     *
     * @param inThrowable The cause of error.
     * @since 2016/7/5
     */
    void onDataError(final Throwable inThrowable);

}
