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
package io.wzcodes.fluxjava;

/**
 * Send the messages by request in framework.
 *
 * @author WZ
 * @version 20160705
 */
public interface IFluxBus {

    /**
     * Register to bus.
     *
     * @param inSubscriber The subscriber to add into bus.
     * @since 2016/7/5
     */
    void register(final Object inSubscriber);

    /**
     * Unregister from bus.
     *
     * @param inSubscriber The subscriber remove from bus.
     * @since 2016/7/5
     */
    void unregister(final Object inSubscriber);

    /**
     * Send a message to all subscribers.
     *
     * @param inEvent The message need to be sent.
     * @since 2016/7/5
     */
    void post(final Object inEvent);

}
