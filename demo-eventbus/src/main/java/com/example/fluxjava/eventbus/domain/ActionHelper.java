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
package com.example.fluxjava.eventbus.domain;

import com.example.fluxjava.eventbus.domain.actions.TodoAction;
import com.example.fluxjava.eventbus.domain.actions.UserAction;
import com.example.fluxjava.eventbus.domain.models.Todo;
import com.example.fluxjava.eventbus.domain.models.User;
import io.wzcodes.fluxjava.IActionHelper;

import java.util.ArrayList;

import static com.example.fluxjava.eventbus.domain.Constants.TODO_ADD;
import static com.example.fluxjava.eventbus.domain.Constants.TODO_CLOSE;
import static com.example.fluxjava.eventbus.domain.Constants.TODO_LOAD;
import static com.example.fluxjava.eventbus.domain.Constants.USER_LOAD;

public class ActionHelper implements IActionHelper {

    @Override
    public Class<?> getActionClass(final Object inActionTypeId) {
        Class<?> result = null;

        if (inActionTypeId instanceof Integer) {
            final int typeId = (int)inActionTypeId;

            // return action type by pre-define id
            switch (typeId) {
                case USER_LOAD:
                    result = UserAction.class;
                    break;
                case TODO_LOAD:
                case TODO_ADD:
                case TODO_CLOSE:
                    result = TodoAction.class;
                    break;
            }
        }

        return result;
    }

    @Override
    public Object wrapData(final Object inData) {
        Object result = inData;

        // base on data type to convert data into require form
        if (inData instanceof Integer) {
            result = this.getData((int)inData, -1);
        }
        if (inData instanceof String) {
            final String[] command = ((String)inData).split(":");
            final int action;
            final int position;

            action = Integer.valueOf(command[0]);
            position = Integer.valueOf(command[1]);
            result = this.getData(action, position);
        }
        if (inData instanceof Todo) {
            final ArrayList<Todo> todoList = new ArrayList<>();

            this.updateRemoteTodo();
            todoList.add((Todo)inData);
            result = todoList;
        }

        return result;
    }

    private Object getData(final int inAction, final int inPosition) {
        Object result = null;

        switch (inAction) {
            case USER_LOAD:
                result = this.getRemoteUser();
                break;
            case TODO_LOAD:
                result = this.getRemoteTodo(inPosition);
                break;
        }

        return result;
    }

    private ArrayList<User> getRemoteUser() {
        // TODO Replace the remote call here
        final ArrayList<User> userList = new ArrayList<>();
        final String[] names = {"Tom", "Mary", "John"};

        // Simulate the data retrieve from remote
        for (String name : names) {
            User user = new User();

            user.name = name;
            userList.add(user);
        }

        return userList;
    }

    private ArrayList<Todo> getRemoteTodo(final int inUserIndex) {
        // TODO Replace the remote call here
        final ArrayList<Todo> todoList = new ArrayList<>();
        final String[] titles = {
                "Meet with Bill",
                "Dinner",
                "Go to bank",
                "Workout",
                "Book flight",
                "Clean the house",
                "Create a new playlist",
                "Send a mail"
        };
        final String[] memos = {
                "Prepare presentation and demo the system.",
                "Jane's birthday.",
                "",
                "",
                "Traveling to LA on weekend.",
                "Living room and kitchen",
                "I bought some new albums last week.",
                "Followup for the new client thread."
        };
        final String[] dueDates = {
                "2016/1/25",
                "2016/2/14",
                "2016/2/2",
                "Never",
                "2016/1/31",
                "2016/1/28",
                "2016/7/12",
                "2016/6/30"
        };
        final int startIndex = (inUserIndex % 3);

        // Simulate the data retrieve from remote
        for (int i = startIndex; i < titles.length; i++) {
            final Todo todo = new Todo();

            todo.id = i;
            todo.title = titles[i];
            todo.memo = memos[i];
            todo.dueDate = dueDates[i];
            todo.closed = false;
            todoList.add(todo);
        }

        return todoList;
    }

    private void updateRemoteTodo() {
        // TODO Add the remote call here to update data
    }

}
