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
package com.example.fluxjava.rx.domain.stores;

import com.example.fluxjava.rx.domain.actions.UserAction;
import com.example.fluxjava.rx.domain.models.User;
import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.IFluxAction;
import io.wzcodes.fluxjava.IFluxBus;
import io.wzcodes.fluxjava.rx.RxStore;

import java.util.ArrayList;

import static com.example.fluxjava.rx.domain.Constants.USER_LOAD;

public class UserStore extends RxStore<User> {

    public static class ListChangeEvent implements FluxContext.StoreChangeEvent {}

    ArrayList<User> mList = new ArrayList<>();

    UserStore(final IFluxBus inBus) {
        super(inBus);
    }

    @Override
    protected Class<? extends IFluxAction> getActionType() {
        return UserAction.class;
    }

    @Override
    protected <TAction extends IFluxAction> void onAction(TAction inAction) {
        final UserAction action = (UserAction)inAction;

        // base on input action to process data
        // in this sample only define one action
        switch (action.getType()) {
            case USER_LOAD:
                this.mList.clear();
                this.mList.addAll(action.getData());
                super.emitChange(new ListChangeEvent());
                break;
        }
    }

    @Override
    protected void onError(Throwable inThrowable) {
        // put error handle here
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

}
