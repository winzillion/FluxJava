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
package com.example.fluxjava.rx.domain

import com.example.fluxjava.rx.domain.actions.TodoAction
import com.example.fluxjava.rx.domain.actions.UserAction
import com.example.fluxjava.rx.domain.models.Todo
import com.example.fluxjava.rx.domain.models.User
import spock.lang.Specification
import spock.lang.Unroll

import static com.example.fluxjava.rx.domain.Constants.*

@Unroll
class ActionHelperSpec extends Specification {

    def "Test getActionClass"() {
        given:
        def target = new ActionHelper()

        expect:
        target.getActionClass(action) == expected

        where:
        action     || expected
        USER_LOAD  || UserAction.class
        TODO_LOAD  || TodoAction.class
        TODO_ADD   || TodoAction.class
        TODO_CLOSE || TodoAction.class
    }

    def "Test wrapData"() {
        given:
        def target = new ActionHelper()
        def actual

        when: "pass load user command"
        actual = target.wrapData(USER_LOAD)

        then: "get the user list"
        actual.size == 3
        actual[0] instanceof User

        when: "pass load todo command"
        actual = target.wrapData(TODO_LOAD + ":0")

        then: "get the todo list"
        actual.size == 8
        actual[0] instanceof Todo

        when: "pass a todo item"
        actual = target.wrapData(new Todo())

        then: "get a list of todo"
        actual.size == 1
        actual[0] instanceof Todo
    }

}