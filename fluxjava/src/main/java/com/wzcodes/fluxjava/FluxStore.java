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
 * The abstract Store class in framework.
 *
 * @author WZ
 * @version 20160705
 */
public abstract class FluxStore<TEntity> implements IFluxStore<TEntity> {

    private IFluxBus mBus;
    private Object mTag;

    /**
     * Constructor.
     *
     * @param inBus The instance of {@link IFluxBus}.
     * @since 2016/7/5
     */
    protected FluxStore(final IFluxBus inBus) {
        this.mBus = inBus;
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void register(final Object inView) {
        this.mBus.register(inView);
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void unregister(final Object inView) {
        this.mBus.unregister(inView);
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public Object getTag() {
        return this.mTag;
    }

    /**
     * @since 2016/7/5
     */
    @Override
    public void setTag(Object inTag) {
        if (this.mTag == null && inTag != null) {
            // The tag can't be changed after first assigned
            // Get new object from FluxContext if tag changed
            this.mTag = inTag;
        }
    }

    /**
     * Change the instance of {@link IFluxBus}.
     *
     * @param inBus The new instance of {@link IFluxBus}.
     * @return The old instance keep in store.
     * @since 2016/7/5
     */
    protected IFluxBus changeBus(final IFluxBus inBus) {
        final IFluxBus oldBus = this.mBus;

        this.mBus = inBus;

        return oldBus;
    }

    /**
     * Send a data change event.
     *
     * @param inEvent The event object.
     * @since 2016/7/5
     */
    protected void emitChange(final FluxContext.StoreChangeEvent inEvent) {
        this.mBus.post(inEvent);
    }

    /**
     * Check if the request tag match the one keep in store.
     *
     * @param inTag The tag to be matched
     * @return Check result.
     * @since 2016/7/5
     */
    protected boolean matchTag(final Object inTag) {
        boolean result = false;

        if (this.mTag == null) {
            if (inTag == null) {
                result = true;
            }
        } else {
            if (inTag != null && this.mTag.getClass() == inTag.getClass()) {
                result = this.mTag.equals(inTag);
            }
        }

        return  result;
    }

}
