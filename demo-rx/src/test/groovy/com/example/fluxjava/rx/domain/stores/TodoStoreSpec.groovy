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
package com.example.fluxjava.rx.domain.stores

import com.example.fluxjava.rx.domain.actions.TodoAction
import com.example.fluxjava.rx.domain.models.Todo
import com.wzcodes.fluxjava.rx.RxBus
import rx.functions.Action1
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static com.example.fluxjava.rx.domain.Constants.*

class TodoStoreSpec extends Specification {

    private CountDownLatch mCountDownLatch

    def "Test load data"() {
        given:
        def bus = new RxBus()
        def target = new TodoStore(bus)
        def random = new Random()
        def expected = [new Todo(100)] * random.nextInt(10)
        def findItem = new Todo(1)
        def actual
        def actualIndex

        expected << findItem
        bus.register(target)
        target.toObservable(TodoStore.ListChangeEvent.class)
                .subscribe(
                    new Action1<TodoStore.ListChangeEvent>() {
                        @Override
                        void call(TodoStore.ListChangeEvent inListChangeEvent) {
                            TodoStoreSpec.this.mCountDownLatch.countDown()
                        }
                    })
        target.toObservable(TodoStore.ItemChangeEvent.class)
                .subscribe(
                    new Action1<TodoStore.ItemChangeEvent>() {
                        @Override
                        void call(TodoStore.ItemChangeEvent inListChangeEvent) {
                            TodoStoreSpec.this.mCountDownLatch.countDown()
                        }
                    })

        when:
        this.mCountDownLatch = new CountDownLatch(1)
        bus.post(new TodoAction(TODO_LOAD, expected))
        this.mCountDownLatch.await()

        then:
        target.getCount() == expected.size()

        when: "get the last item"
        actual = target.getItem(expected.size() - 1)

        then: "the todo id will match"
        actual.id == findItem.id

        when: "find an item"
        actualIndex = target.findItem(findItem)

        then: "item in the list"
        actualIndex > -1

        when: "add an item"
        this.mCountDownLatch = new CountDownLatch(1)
        bus.post(new TodoAction(TODO_ADD, [new Todo(101)]))
        this.mCountDownLatch.await()

        then: "the count of list will increase"
        target.getCount() == expected.size() + 1

        when: "update an item"
        this.mCountDownLatch = new CountDownLatch(1)
        findItem.closed = true
        bus.post(new TodoAction(TODO_CLOSE, [findItem]))
        this.mCountDownLatch.await()

        then:
        target.getItem(expected.size() - 1).closed

        cleanup:
        bus.unregister(target)
    }

}