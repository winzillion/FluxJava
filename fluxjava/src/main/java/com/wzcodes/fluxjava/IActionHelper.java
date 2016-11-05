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
package com.wzcodes.fluxjava;

/**
 * A helper class for {@link ActionCreator}.
 * It will provide the custom logic when {@link ActionCreator} creates an instance of Action.
 *
 * @author WZ
 * @version 20160705
 */
public interface IActionHelper {

    /**
     * Get a type of Action from type identity.
     *
     * @param inActionTypeId The type identity of Action.
     * @since 2016/7/5
     */
    Class<?> getActionClass(final Object inActionTypeId);

    /**
     * Wrap the data format from display to storage.
     *
     * @param inData The data need to be wrapped.
     * @since 2016/7/5
     */
    Object wrapData(final Object inData);

}
