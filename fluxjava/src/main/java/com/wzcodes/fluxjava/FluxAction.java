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
 * The abstract Action class in framework.
 *
 * @author WZ
 * @version 20160705
 */
public abstract class FluxAction<TType, TData> implements IFluxAction {

    private final TType mType;
    private final TData mData;

    /**
     * Constructor.
     *
     * @param inType The type of action.
     * @param inData The data of action.
     * @since 2016/7/5
     */
    protected FluxAction(final TType inType, final TData inData) {
        if (inType == null) {
            throw new IllegalArgumentException("Type can't be null.");
        }
        this.mType = inType;
        this.mData = inData;
    }

    /**
     * Get the type of action.
     *
     * @return The type of action.
     * @since 2016/7/5
     */
    public TType getType() {
        return this.mType;
    }

    /**
     * Get the data of action.
     *
     * @return The data of action.
     * @since 2016/7/5
     */
    public TData getData() {
        return this.mData;
    }

}
