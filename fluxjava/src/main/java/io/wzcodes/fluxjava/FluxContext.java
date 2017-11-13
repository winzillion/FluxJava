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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class serves as handleIllegalAccessException coordinating object in framework.
 * It is an entry point to use FluxJava and it will bring the components in framework work together.
 *
 * @author WZ
 * @version 20170523
 */
public class FluxContext {

    private static FluxContext sInstance = null;

    private final IFluxBus mBus;
    private final ActionCreator mActionCreator;
    private IActionHelper mActionHelper = null;
    private Map<Object, Class<?>> mStoreMap = null;
    private Map<Object, IFluxStore> mStoreKeepList = null;

    public interface StoreChangeEvent {}

    public static class Builder {
        private IFluxBus mBus = null;
        private ActionCreator mActionCreator = null;
        private IActionHelper mActionHelper = null;
        private Map<Object, Class<?>> mStoreMap = null;
        private boolean mKeepStore = false;

        Builder() {
            // Do nothing
        }

        private IFluxBus getBus() {
            return this.mBus;
        }

        public Builder setBus(final IFluxBus inBus) {
            this.mBus = inBus;
            return this;
        }

        private ActionCreator getActionCreator() {
            return this.mActionCreator;
        }

        public Builder setActionCreator(final ActionCreator inActionCreator) {
            this.mActionCreator = inActionCreator;
            return this;
        }

        private IActionHelper getActionHelper() {
            return this.mActionHelper;
        }

        public Builder setActionHelper(final IActionHelper inActionHelper) {
            this.mActionHelper = inActionHelper;
            return this;
        }

        private Map<Object, Class<?>> getStoreMap() {
            return this.mStoreMap;
        }

        public Builder setStoreMap(final Map<Object, Class<?>> inStoreMap) {
            this.mStoreMap = inStoreMap;
            return this;
        }

        private boolean getKeepStore() {
            return this.mKeepStore;
        }

        public Builder setKeepStore(final boolean inKeepStore) {
            this.mKeepStore = inKeepStore;
            return this;
        }

        public FluxContext build() {
            if (this.mBus == null) {
                throw new IllegalArgumentException("Flux bus is missing.");
            }
            if (this.mActionCreator == null && this.mActionHelper == null) {
                throw new IllegalArgumentException("ActionHelper or ActionCreator is missing.");
            }
            if (this.mStoreMap == null) {
                throw new IllegalArgumentException("Store map is missing.");
            }

            return FluxContext.getInstance(this);
        }
    }

    /**
     * Constructor.
     *
     * @param inBuilder The parameters to build an instance of {@link FluxContext}.
     * @since 2016/7/5
     */
    private FluxContext(final Builder inBuilder) {
        this.mBus = inBuilder.getBus();
        if (inBuilder.getActionCreator() != null) {
            this.mActionCreator = inBuilder.getActionCreator();
            if (inBuilder.getActionHelper() != null) {
                this.mActionHelper = inBuilder.getActionHelper();
            }
        } else {
            this.mActionCreator = new ActionCreator(this.mBus);
            this.mActionHelper = inBuilder.getActionHelper();
        }
        this.mStoreMap = inBuilder.getStoreMap();
        this.setKeepStore(inBuilder.getKeepStore());
    }

    /**
     * Get handleIllegalAccessException {@link Builder} to pass parameters and build an instance of {@link FluxContext}.
     * This should be called once before using framework.
     *
     * @return Instance of {@link Builder}.
     * @since 2016/7/5
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Get an instance of {@link FluxContext}.
     *
     * @param inBuilder The parameters to build an instance of {@link FluxContext}.
     * @return Instance of {@link FluxContext}.
     * @since 2016/7/5
     */
    static private FluxContext getInstance(final Builder inBuilder) {
        if (FluxContext.sInstance == null) {
            synchronized (FluxContext.class) {
                if (FluxContext.sInstance == null) {
                    FluxContext.sInstance = new FluxContext(inBuilder);
                }
            }
        }

        return FluxContext.sInstance;
    }

    /**
     * Get an instance of {@link FluxContext}.
     *
     * @return Instance of {@link FluxContext}.
     * @since 2016/7/5
     */
    public static FluxContext getInstance() {
        return FluxContext.sInstance;
    }

    /**
     * Set handleIllegalAccessException map that can be used to generate handleIllegalAccessException store.
     *
     * @param inMap The mapping table for stores.
     * @since 2016/7/5
     */
    public void setStoreMap(final Map<Object, Class<?>> inMap) {
        this.mStoreMap = inMap;
    }

    /**
     * Determine if the stores should be keep by {@link FluxContext}
     * in order to preserve the state of data.
     *
     * @param inKeepStore Keep stores or not.
     * @since 2016/7/5
     */
    public void setKeepStore(final boolean inKeepStore) {
        if (inKeepStore) {
            if (this.mStoreKeepList == null) {
                this.mStoreKeepList = new HashMap<>();
            }
        } else {
            this.clearAllStore();
            this.mStoreKeepList = null;
        }
    }

    /**
     * Get the instance of Bus hold by {@link FluxContext}.
     *
     * @return Instance of Bus.
     * @since 2016/7/5
     */
    public IFluxBus getBus() {
        return this.mBus;
    }

    /**
     * Get the instance of {@link ActionCreator} hold by {@link FluxContext}.
     *
     * @return Instance of {@link ActionCreator}.
     * @since 2016/7/5
     */
    public ActionCreator getActionCreator() {
        return this.mActionCreator;
    }

    /**
     * Get an instance of Store by store type identity.
     * When tag is null, it will return the same type of store in keep list
     * or create new one if not found.
     *
     * @param inStoreTypeId The store type identity to match in the map.
     * @param inTag The tag to match in the store keep list.
     * @param inView Optional, the object want to listen events from store.
     *               It also can be registered after get the store.
     * @return Instance of {@link IFluxStore}. If there is anything go wrong it will return null.
     * @since 2016/7/5
     */
    public <TStoreId, TTag, TView> IFluxStore getStore(final TStoreId inStoreTypeId, final TTag inTag, final TView inView) {
        IFluxStore result = null;

        if (this.mStoreKeepList != null && inTag != null) {
            result = this.mStoreKeepList.get(inTag);
        }

        if (result != null) {
            if (inView != null) {
                result.register(inView);
            }
        } else {
            if (this.mStoreMap != null) {
                final Class<?> storeType = this.mStoreMap.get(inStoreTypeId);

                if (storeType != null) {
                    if (inTag == null && this.mStoreKeepList != null) {
                        // If the tag is null and keep list is not null, find the same type in list first
                        for (Map.Entry<Object, IFluxStore> entry : this.mStoreKeepList.entrySet()) {
                            if (entry.getValue().getClass() == storeType) {
                                result = entry.getValue();
                                break;
                            }
                        }
                    }

                    if (result != null) {
                        if (inView != null) {
                            result.register(inView);
                        }
                    } else {
                        try {
                            final Constructor<?> constructor = storeType.getConstructor(IFluxBus.class);

                            result = (IFluxStore)constructor.newInstance(this.mBus);
                            this.registerStore(result, inView);
                            if (inTag != null) {
                                result.setTag(inTag);
                                if (this.mStoreKeepList != null) {
                                    this.mStoreKeepList.put(inTag, result);
                                }
                            }
                        } catch (IllegalAccessException exIllegalAccess) {
                            this.handleIllegalAccessException();
                        } catch (InvocationTargetException exInvocationTarget) {
                            this.handleInvocationTargetException();
                        } catch (InstantiationException exInstantiation) {
                            this.handleInstantiationException();
                        } catch (NoSuchMethodException exNoSuchMethod) {
                            this.handleNoSuchMethodException();
                        }
                    }
                } // storeType != null
            } // this.mStoreMap != null
        } // result == null

        return result;
    }

    /**
     * Get the instance of ActionHelper.
     *
     * @return Instance of {@link IActionHelper}.
     * @since 2016/7/5
     */
    IActionHelper getActionHelper() {
        return this.mActionHelper;
    }

    /**
     * Set the instance of ActionHelper.
     *
     * @param inActionHelper Instance of ActionHelper.
     * @since 2016/7/5
     */
    public void setActionHelper(final IActionHelper inActionHelper) {
        this.mActionHelper = inActionHelper;
    }

    /**
     * Register handleIllegalAccessException store to Dispatcher.
     *
     * @param inStore The instance of store.
     * @param inView Optional, the object want to listen events from store.
     * @since 2016/7/5
     */
    public <TView> void registerStore(final IFluxStore inStore, final TView inView) {
        if (inView != null) {
            inStore.register(inView);
        }
        this.mBus.register(inStore);
    }

    /**
     * Unregister handleIllegalAccessException store from Dispatcher.
     *
     * @param inStore The instance of store.
     * @param inView Optional, the object registered in store.
     * @since 2016/7/5
     */
    public <TView> void unregisterStore(final IFluxStore inStore, final TView inView) {
        if (inView != null) {
            inStore.unregister(inView);
        }
        this.mBus.unregister(inStore);
    }

    /**
     * Purge all the stores from keep list.
     *
     * @since 2016/7/5
     */
    public void clearAllStore() {
        if (this.mStoreKeepList != null) {
            for (Map.Entry entry : this.mStoreKeepList.entrySet()) {
                this.unregisterStore((IFluxStore)entry.getValue(), null);
            }
            this.mStoreKeepList.clear();
        }
    }

    /**
     * Handle {@link IllegalAccessException}.
     *
     * @since 2016/7/5
     */
    private void handleIllegalAccessException() {
        // Do nothing
    }

    /**
     * Handle {@link InvocationTargetException}.
     *
     * @since 2016/7/5
     */
    private void handleInvocationTargetException() {
        // Do nothing
    }

    /**
     * Handle {@link InstantiationException}.
     *
     * @since 2016/7/5
     */
    private void handleInstantiationException() {
        // Do nothing
    }

    /**
     * Handle {@link NoSuchMethodException}.
     *
     * @since 2016/7/5
     */
    private void handleNoSuchMethodException() {
        // Do nothing
    }

}
