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

import java.lang.reflect.Field

class ActionCreatorSpec extends Specification {

    private static abstract class StubAbsAction extends FluxAction<String, String> {
        StubAbsAction(String inType, String inData) {
            super(inType, inData)
        }
    }

    private static class StubAction extends FluxAction<String, String> {
        StubAction(String inType, String inData) {
            super(inType, inData)
        }
    }

    private static class StubListAction extends FluxAction<String, List<String>> {
        StubListAction(String inType, List<String> inData) {
            super(inType, inData)
        }
    }

    private static class StubMapAction extends FluxAction<String, Map<Integer, List<String>>> {
        StubMapAction(String inType, Map<Integer, List<String>> inData) {
            super(inType, inData)
        }
    }

    private static class StubErrorAction extends StubAbsAction {
        StubErrorAction(String inType, String inData) {
            super(inType, inData)
            throw new Exception()
        }
    }

    private static class StubNoParamAction extends StubAbsAction {
        StubNoParamAction() {
            super("", "")
        }
    }

    private static class FakeAction extends FluxAction<String, String> {
        FakeAction(String inType) {
            super(inType, null);
        }
    }

    def setup() {
        Field field = FluxContext.class.getDeclaredField("sInstance");

        field.setAccessible(true);
        // Reset the instance of FluxContext
        field.set(null, null);
    }

    def "Test sendRequest"() {
        given:
        def bus = Mock(IFluxBus);
        def builder = FluxContext.getBuilder()
        def actionHelper = Mock(IActionHelper)
        def context
        def target

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [0:FluxContextSpec.StubStore.class]
        context = builder.build()

        when:
        new ActionCreator(null);

        then: "exception thrown when bus is null"
        thrown(IllegalArgumentException)

        when: "ActionHelper is null"
        target = context.actionCreator
        context.setActionHelper(null)
        target.sendRequest("", "")

        then:
        thrown(IllegalArgumentException)

        when: "action class is null"
        context.setActionHelper(actionHelper);
        target.sendRequest("", "")

        then:
        thrown(IllegalStateException)

        when: "invalid Action class"
        actionHelper.getActionClass("0") >> Object.class
        target.sendRequest("0", "");

        then:
        thrown(IllegalStateException)

        when: "Action class can't invoke"
        actionHelper.getActionClass("1") >> FakeAction.class
        target.sendRequest("1", "");

        then:
        thrown(IllegalStateException)

        when: "Action class is abstract"
        actionHelper.getActionClass("2") >> StubAbsAction.class
        target.sendRequest("2", "");

        then:
        thrown(IllegalStateException)

        when: "constructor of Action class throw exception"
        actionHelper.getActionClass("3") >> StubErrorAction.class
        target.sendRequest("3", "");

        then:
        thrown(IllegalStateException)

        when: "constructor of Action class not match"
        actionHelper.getActionClass("4") >> StubNoParamAction.class
        target.sendRequest("4", "");

        then:
        thrown(IllegalStateException)

        when: "normal condition"
        actionHelper.getActionClass("5") >> StubAction.class
        target.sendRequest("5", "");

        then:
        1 * bus.post(_ as StubAction)

        when: "data type is list"
        actionHelper.wrapData("6") >> new ArrayList<>()
        actionHelper.getActionClass("6") >> StubListAction.class
        target.sendRequest("6", "6");

        then:
        1 * bus.post(_ as StubListAction)

        when: "data type is map"
        actionHelper.wrapData("7") >> new HashMap<>()
        actionHelper.getActionClass("7") >> StubMapAction.class
        target.sendRequest("7", "7");

        then:
        1 * bus.post(_ as StubMapAction)

        when: "data type is map but data is not map"
        actionHelper.wrapData("7") >> new ArrayList()
        actionHelper.getActionClass("7") >> StubMapAction.class
        target.sendRequest("7", "7");

        then:
        thrown(IllegalArgumentException)
    }

    def "Test sendRequestAsync"() {
        given:
        def bus = Mock(IFluxBus);
        def builder = FluxContext.getBuilder()
        def actionHelper = Mock(IActionHelper)
        def context
        def target
        def expected

        builder.bus = bus
        builder.actionHelper = actionHelper
        builder.storeMap = [0:FluxContextSpec.StubStore.class]
        context = builder.build()
        target = context.actionCreator

        when:
        actionHelper.getActionClass(_) >> StubAction.class
        expected = target.sendRequestAsync("", "");
        expected.join()

        then:
        1 * bus.post(_ as StubAction)
    }

}