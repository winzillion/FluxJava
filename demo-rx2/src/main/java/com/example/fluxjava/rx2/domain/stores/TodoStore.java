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
package com.example.fluxjava.rx2.domain.stores;

import com.example.fluxjava.rx2.domain.actions.TodoAction;
import com.example.fluxjava.rx2.domain.models.Todo;

import java.util.ArrayList;

import io.wzcodes.fluxjava.FluxContext;
import io.wzcodes.fluxjava.IFluxAction;
import io.wzcodes.fluxjava.IFluxBus;
import io.wzcodes.fluxjava.rx.RxStore;

import static com.example.fluxjava.rx2.domain.Constants.TODO_ADD;
import static com.example.fluxjava.rx2.domain.Constants.TODO_CLOSE;
import static com.example.fluxjava.rx2.domain.Constants.TODO_LOAD;

public class TodoStore extends RxStore<Todo> {

    public static class ListChangeEvent implements FluxContext.StoreChangeEvent {}

    public static class ItemChangeEvent implements FluxContext.StoreChangeEvent {
        public int position;

        ItemChangeEvent(final int inPosition) {
            this.position = inPosition;
        }
    }

    ArrayList<Todo> mList = new ArrayList<>();

    TodoStore(final IFluxBus inBus) {
        super(inBus);
    }

    @Override
    protected Class<? extends IFluxAction> getActionType() {
        return TodoAction.class;
    }

    @Override
    protected <TAction extends IFluxAction> void onAction(final TAction inAction) {
        final TodoAction action = (TodoAction)inAction;

        // base on input action to process data
        switch (action.getType()) {
            case TODO_LOAD:
                this.mList.clear();
                this.mList.addAll(action.getData());
                super.emitChange(new ListChangeEvent());
                break;
            case TODO_ADD:
                this.mList.addAll(action.getData());
                super.emitChange(new ListChangeEvent());
                break;
            case TODO_CLOSE:
                for (int j = 0; j < action.getData().size(); j++) {
                    for (int i = 0; i < this.mList.size(); i++) {
                        if (this.mList.get(i).id == action.getData().get(j).id) {
                            this.mList.set(i, action.getData().get(j));
                            super.emitChange(new ItemChangeEvent(i));
                            break;
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onError(final Throwable inThrowable) {
        // put error handle here
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

}
