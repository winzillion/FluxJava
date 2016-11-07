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
package com.example.fluxjava.rx;

import android.app.Application;

import com.example.fluxjava.rx.domain.ActionHelper;
import com.example.fluxjava.rx.domain.stores.TodoStore;
import com.example.fluxjava.rx.domain.stores.UserStore;
import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.rx.RxBus;

import java.util.HashMap;

import static com.example.fluxjava.rx.domain.Constants.DATA_TODO;
import static com.example.fluxjava.rx.domain.Constants.DATA_USER;

public class AppConfig extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        this.setupFlux();
    }

    private void setupFlux() {
        HashMap<Object, Class<?>> storeMap = new HashMap<>();

        storeMap.put(DATA_USER, UserStore.class);
        storeMap.put(DATA_TODO, TodoStore.class);

        // setup relationship of components in framework
        FluxContext.getBuilder()
                .setBus(RxBus.getDefault())
                .setActionHelper(new ActionHelper())
                .setStoreMap(storeMap)
                .build();
    }

}
