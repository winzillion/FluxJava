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
package com.example.fluxjava.rx2.domain.models

import spock.lang.Specification

class TodoSpec extends Specification {

    def "Test clone"() {
        given:
        def target
        def expected = new Todo()
        def random = new Random()

        when:
        expected.id = random.nextInt(10)
        expected.title = "title"
        expected.memo = "memo"
        expected.dueDate = "dueDate"
        expected.closed = true
        target = new Todo(expected)

        then:
        target.id == expected.id
        target.title == expected.title
        target.memo == expected.memo
        target.dueDate == expected.dueDate
        target.closed == expected.closed
    }

}