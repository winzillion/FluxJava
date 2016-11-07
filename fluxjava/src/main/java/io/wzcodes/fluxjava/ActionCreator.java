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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * The ActionCreator in framework. It helps to create action when user input.
 *
 * @author WZ
 * @version 20160705
 */
public class ActionCreator {

    private final IFluxBus mDispatcher;

    /**
     * Constructor.
     *
     * @param inDispatcher Instance of Dispatcher.
     * @since 2016/7/5
     */
    protected ActionCreator(final IFluxBus inDispatcher) {
        if (inDispatcher == null) {
            throw new IllegalArgumentException("Dispatcher is missing.");
        }
        this.mDispatcher = inDispatcher;
    }

    /**
     * Send request to store async.
     *
     * @param inTypeId The identity of action type.
     * @param inData The data of action.
     * @return The thread runs sendRequest.
     * @since 2016/7/5
     */
    public <TTypeId, TData> Thread sendRequestAsync(final TTypeId inTypeId, final TData inData) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ActionCreator.this.sendRequest(inTypeId, inData);
            }
        };
        Thread thread = new Thread(runnable);

        thread.start();

        return thread;
    }

    /**
     * Send request to store.
     *
     * @param inTypeId The identity of action type.
     * @param inData The data of action.
     * @since 2016/7/5
     */
    public <TTypeId, TData> void sendRequest(final TTypeId inTypeId, final TData inData) {
        IActionHelper actionHelper = FluxContext.getInstance().getActionHelper();
        Object action = null;

        if (actionHelper ==  null) {
            throw new IllegalArgumentException("ActionBuilder is missing.");
        } else {
            Class<?> typeClass = inTypeId.getClass();
            Class<?> actionClass = actionHelper.getActionClass(inTypeId);

            if (actionClass == null) {
                throw new IllegalStateException("Action class is missing.");
            } else {
                Class<?> dataClass;
                Object actionData = actionHelper.wrapData(inData);

                try {
                    Class<?> workClass = actionClass;

                    while (workClass.getSuperclass() != FluxAction.class) {
                        workClass = workClass.getSuperclass();
                    }

                    final ParameterizedType superType = (ParameterizedType)workClass.getGenericSuperclass();
                    final Type[] typeArguments = superType.getActualTypeArguments();

                    if (typeArguments[1] instanceof ParameterizedType) {
                        dataClass = (Class<?>)((ParameterizedType)typeArguments[1]).getRawType();
                    } else {
                        dataClass = (Class<?>)typeArguments[1];
                    }
                } catch (Exception exGeneral) {
                    throw new IllegalStateException("Data type in Action class is not define.");
                }

                try {
                    final Constructor<?> constructor = actionClass.getConstructor(typeClass, dataClass);

                    action = constructor.newInstance(inTypeId, actionData);
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

            this.mDispatcher.post(action);
        } // actionBuilder ==  null
    }

    /**
     * Handle {@link IllegalAccessException}.
     *
     * @since 2016/7/5
     */
    private void handleIllegalAccessException() {
        throw new IllegalStateException("Can't create Action instance.");
    }

    /**
     * Handle {@link InvocationTargetException}.
     *
     * @since 2016/7/5
     */
    private void handleInvocationTargetException() {
        this.handleIllegalAccessException();
    }

    /**
     * Handle {@link InstantiationException}.
     *
     * @since 2016/7/5
     */
    private void handleInstantiationException() {
        this.handleIllegalAccessException();
    }

    /**
     * Handle {@link NoSuchMethodException}.
     *
     * @since 2016/7/5
     */
    private void handleNoSuchMethodException() {
        this.handleIllegalAccessException();
    }

}
