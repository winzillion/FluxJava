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
package com.example.fluxjava.rx2.domain.actions;

import com.example.fluxjava.rx2.domain.models.Todo;

import java.util.List;

import io.wzcodes.fluxjava.FluxAction;

public class TodoAction extends FluxAction<Integer, List<Todo>> {

    public TodoAction(final Integer inType, final List<Todo> inData) {
        super(inType, inData);
    }

}
