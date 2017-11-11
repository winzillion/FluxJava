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
package com.example.fluxjava.rx2.domain.stores

import com.example.fluxjava.rx2.domain.actions.UserAction
import com.example.fluxjava.rx2.domain.models.User
import io.reactivex.functions.Consumer
import io.wzcodes.fluxjava.rx.RxBus
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static com.example.fluxjava.rx2.domain.Constants.USER_LOAD

class UserStoreSpec extends Specification {

    def "Test load data"() {
        given:
        def bus = new RxBus()
        def target = new UserStore(bus)
        def random = new Random()
        def expected = [new User("dummy")] * random.nextInt(10)
        def findItem = new User("test")
        def countDownLatch = new CountDownLatch(1)
        def actual
        def actualIndex

        expected << findItem
        bus.register(target)
        target.toObservable(UserStore.ListChangeEvent.class)
                .subscribe(
                    new Consumer<UserStore.ListChangeEvent>() {
                        @Override
                        void accept(UserStore.ListChangeEvent inListChangeEvent) throws Exception {
                            countDownLatch.countDown()
                        }
                    })

        when:
        bus.post(new UserAction(USER_LOAD, expected))
        countDownLatch.await()

        then:
        target.getCount() == expected.size()

        when: "get the last item"
        actual = target.getItem(expected.size() - 1)

        then: "the user name will match"
        actual.name == findItem.name

        when: "find an item"
        actualIndex = target.findItem(findItem)

        then: "item in the list"
        actualIndex > -1

        cleanup:
        bus.unregister(target)
    }

}