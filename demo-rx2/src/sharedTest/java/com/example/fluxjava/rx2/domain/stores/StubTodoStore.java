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
import io.wzcodes.fluxjava.IFluxAction;
import io.wzcodes.fluxjava.IFluxBus;

import static com.example.fluxjava.rx2.domain.Constants.TODO_LOAD;

public class StubTodoStore extends TodoStore {

    private int mLoadCount = 0;

    public StubTodoStore(final IFluxBus inBus) {
        super(inBus);
    }

    @Override
    protected <TAction extends IFluxAction> void onAction(TAction inAction) {
        super.onAction(inAction);
        if (((TodoAction)inAction).getType() == TODO_LOAD) {
            for (int i = 0; i < (4 + this.mLoadCount); i++) {
                Todo todo = new Todo(i);

                todo.title = "Test title " + i;
                todo.memo = "This is a sample wording for test.";
                todo.dueDate = "2016/1/1";
                super.mList.add(todo);
            }
            this.mLoadCount++;
        }
    }

}
