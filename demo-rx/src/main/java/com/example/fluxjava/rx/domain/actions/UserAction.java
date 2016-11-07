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
package com.example.fluxjava.rx.domain.actions;

import com.example.fluxjava.rx.domain.models.User;
import io.wzcodes.fluxjava.FluxAction;

import java.util.List;

public class UserAction extends FluxAction<Integer, List<User>> {

    public UserAction(final Integer inType, final List<User> inData) {
        super(inType, inData);
    }

}
