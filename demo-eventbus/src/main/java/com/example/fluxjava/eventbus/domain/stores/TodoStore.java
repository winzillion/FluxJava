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

import com.example.fluxjava.eventbus.domain.actions.TodoAction;
import com.example.fluxjava.eventbus.domain.models.Todo;
import com.wzcodes.fluxjava.FluxContext;
import com.wzcodes.fluxjava.FluxStore;
import com.wzcodes.fluxjava.IFluxBus;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.example.fluxjava.eventbus.domain.Constants.TODO_ADD;
import static com.example.fluxjava.eventbus.domain.Constants.TODO_CLOSE;
import static com.example.fluxjava.eventbus.domain.Constants.TODO_LOAD;

public class TodoStore extends FluxStore<Todo> {

    public static class ListChangeEvent implements FluxContext.StoreChangeEvent {}

    public static class ItemChangeEvent implements FluxContext.StoreChangeEvent {
        public int position;

        ItemChangeEvent(final int inPosition) {
            this.position = inPosition;
        }
    }

    ArrayList<Todo> mList = new ArrayList<>();

    public TodoStore(final IFluxBus inBus) {
        super(inBus);
    }

    @Override
    public Todo getItem(final int inIndex) {
        return new Todo(this.mList.get(inIndex));
    }

    @Override
    public int findItem(final Todo inTodo) {
        int result = -1;

        // use id field to identify data
        for (int i = 0; i < this.mList.size(); i++) {
            if (this.mList.get(i).id == inTodo.id) {
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
    public void onAction(final TodoAction inAction) {
        // base on input action to process data
        switch (inAction.getType()) {
            case TODO_LOAD:
                this.mList.clear();
                this.mList.addAll(inAction.getData());
                super.emitChange(new ListChangeEvent());
                break;
            case TODO_ADD:
                this.mList.addAll(inAction.getData());
                super.emitChange(new ListChangeEvent());
                break;
            case TODO_CLOSE:
                for (int j = 0; j < inAction.getData().size(); j++) {
                    for (int i = 0; i < this.mList.size(); i++) {
                        if (this.mList.get(i).id == inAction.getData().get(j).id) {
                            this.mList.set(i, inAction.getData().get(j));
                            super.emitChange(new ItemChangeEvent(i));
                            break;
                        }
                    }
                }
                break;
        }
    }

}
