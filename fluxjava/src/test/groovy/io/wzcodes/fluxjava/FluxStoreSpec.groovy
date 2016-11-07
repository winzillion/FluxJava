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
package io.wzcodes.fluxjava

import io.wzcodes.fluxjava.FluxContext
import io.wzcodes.fluxjava.FluxStore
import io.wzcodes.fluxjava.IFluxBus
import spock.lang.Specification

class FluxStoreSpec extends Specification {

    def mBus = Mock(IFluxBus)
    def mTarget = new FluxStore(mBus) {
        @Override
        Object getItem(int inIndex) {
            return null
        }

        @Override
        int findItem(Object inIndex) {
            return 0
        }

        @Override
        int getCount() {
            return 0
        }
    }

    def "Test register"() {
        given:
        def expected = new Object()

        when:
        this.mTarget.register(expected)

        then:
        1 * this.mBus.register(expected)
    }

    def "Test unregister"() {
        given:
        def expected = new Object()

        when:
        this.mTarget.unregister(expected)

        then:
        1 * this.mBus.unregister(expected)
    }

    def "Test tag"() {
        given:
        def expected = new Object()

        when:
        this.mTarget.setTag(expected)

        then:
        this.mTarget.tag == expected
    }

    def "Test changeBus"() {
        given:
        def newBus = Mock(IFluxBus)
        def expected = new Object()

        when:
        this.mTarget.changeBus(newBus)
        this.mTarget.register(expected)

        then:
        1 * newBus.register(expected)
        0 * this.mBus.register(_)
    }

    def "Test emitChange"() {
        given:
        def expected = new FluxContext.StoreChangeEvent() {}

        when:
        this.mTarget.emitChange(expected)

        then:
        1 * this.mBus.post(expected)
    }

    def "Test matchTag with not null"() {
        given:
        def expected = "expected"

        when:
        this.mTarget.setTag(expected)

        then:
        this.mTarget.matchTag(expected)
        !this.mTarget.matchTag("")
        !this.mTarget.matchTag(null)
    }

    def "Test matchTag with null"() {
        given:
        def expected = "expected"

        expect:
        this.mTarget.matchTag(null)
        !this.mTarget.matchTag(expected)
    }

}