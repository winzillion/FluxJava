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
 * Keep the data in framework.
 *
 * @author WZ
 * @version 20160705
 */
public interface IFluxStore<TEntity> {

    /**
     * Register to store, in order to get data change from store.
     *
     * @param inView The object want to listen data change event.
     * @since 2016/7/5
     */
    void register(final Object inView);

    /**
     * Unregister from store.
     *
     * @param inView The object want to unregister.
     * @since 2016/7/5
     */
    void unregister(final Object inView);

    /**
     * Get the unique identity of this store instance.
     * Tag can be used to distinct if there not only one store instance
     * and action is target on specific one.
     *
     * @return The tag object associated to instance.
     * @since 2016/7/5
     */
    Object getTag();

    /**
     * Set the unique identity of this store instance.
     *
     * @param inTag The tag object.
     * @since 2016/7/5
     */
    void setTag(Object inTag);

    /**
     * Get the data from list by index.
     *
     * @param inIndex The index in list.
     * @return The data object.
     * @since 2016/7/5
     */
    TEntity getItem(final int inIndex);

    /**
     * Get the index of list by data.
     *
     * @param inEntity The data want to be found.
     * @return If data exists then return index, or return -1.
     * @since 2016/7/5
     */
    int findItem(final TEntity inEntity);

    /**
     * Get the count number of list.
     *
     * @return How many data in list.
     * @since 2016/7/5
     */
    int getCount();

}
