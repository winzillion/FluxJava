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
package com.wzcodes.fluxjava

import spock.lang.Specification

class FluxActionSpec extends Specification {

    private class StubAction extends FluxAction<String, String> {
        protected StubAction(String inType, String inData) {
            super(inType, inData);
        }
    }

    def "Test constructor"() {
        when:
        new StubAction(null, "")

        then:
        thrown(IllegalArgumentException)
    }

    def "Test getType"() {
        given:
        def expected = "expected";
        def target = new StubAction(expected, "");

        expect:
        target.getType() == expected
    }

    def "Test getData"() {
        given:
        def expected = "expected";
        def target = new StubAction("", expected);

        expect:
        target.getData() == expected
    }

}