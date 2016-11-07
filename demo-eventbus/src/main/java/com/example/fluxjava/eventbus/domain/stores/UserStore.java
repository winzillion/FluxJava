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
package com.example.fluxjava.eventbus.domain.stores;

import com.example.fluxjava.eventbus.domain.actions.UserAction;
import com.example.fluxjava.eventbus.domain.models.User;
import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.FluxStore;
import io.wzcodes.fluxjava.IFluxBus;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.example.fluxjava.eventbus.domain.Constants.USER_LOAD;

public class UserStore extends FluxStore<User> {

    public static class ListChangeEvent implements FluxContext.StoreChangeEvent {}

    ArrayList<User> mList = new ArrayList<>();

    public UserStore(final IFluxBus inBus) {
        super(inBus);
    }

    @Override
    public User getItem(final int inIndex) {
        return new User(this.mList.get(inIndex));
    }

    @Override
    public int findItem(final User inUser) {
        int result = -1;

        // use name field to identify data
        for (int i = 0; i < this.mList.size(); i++) {
            if (this.mList.get(i).name.equals(inUser.name)) {
                result = i;
                break;
            }
        }

        return result;
    }

    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onAction(final UserAction inAction) {
        // base on input action to process data
        // in this sample only define one action
        switch (inAction.getType()) {
            case USER_LOAD:
                this.mList.clear();
                this.mList.addAll(inAction.getData());
                super.emitChange(new ListChangeEvent());
                break;
        }
    }

}
