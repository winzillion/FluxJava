# FluxJava
FluxJava is a java library that implements Flux pattern. And it is small and light-weight can be used easily to follow the Flux pattern.
[Flux](https://facebook.github.io/flux/docs/overview.html) is an architecture designed at Facebook.
With it's one-way data flow principle, it makes tracing changes during development simpler and makes bugs easier to track down and fix.

FluxJava is not only focus on Android projects but also any other Java ones.
Flux is a good practice to help reduce the problems of MVC complicate dependencies when the number of views grow up in Android projects.
It also can be applied into different kinds of interactive interface that need to access data, not just user interface.

# Getting Started
In repository, you can find the source of FluxJava in `fluxjava` folder
and a demo Android project in `demo-eventbus` folder.
The demo is a simple todo application with one Activity.
In this demo, you can:
 * View todo list.
 * Switch list between different names.
 * Add a todo.
 * Close/Open a todo.

![demo-screenshot](https://cloud.githubusercontent.com/assets/22995091/20027437/ac19b4fa-a34f-11e6-95f9-d2f0f5159a0c.gif)

This demo project uses [EventBus](https://github.com/greenrobot/EventBus) from [greenrobot](http://greenrobot.org/) to help dispatcher and stores to send out messages.

If you want to get benefit from [RxJava](https://github.com/ReactiveX/RxJava), you can check out `fluxjava-rx` folder for FluxJava addon.
There is a demo duplicated from `demo-eventbus` sitting in `demo-rx` folder.
In this demo the EventBus is replaced by RxBus from `fluxjava-rx`.
The RxBus provides bus functionalities with RxJava 1.x library as EventBus do.

# How to use
## Preparation
* **Bus**<br />
Bus is called by dispatcher and stores to deliver messages.
You need to create your own bus by implement IFluxBus interface.
In this class, you can use your prefer bus solution, like Otto, EventBus, even your own one.
If you also include `fluxjava-rx` in your project, then RxBus will be the one you need.
* **Action**<br />
Dispatcher uses actions to inform store something to do.
There are two properties in Action, type and data.
The type tells store what to do and the data is the additional information for specific action.
In the demo project, when a todo be added from UI the new todo will be included in the data of action.
* **ActionHelper**<br />
ActionHelper assists ActionCreator to decide which action to create and how to prepare data in action when a request coming.
* **Store**<br />
Store receives the action sent from Dispatcher and processes data base on action.
It will emmit an event when job done then the event listener can refresh the new state of data.
* **Store Map**<br />
StoreMap is a hash map, it tells framework how to get an instance of store.
For example, if the relationship of action and store is one-by-one, you can use Action type for the key of map.
In the demo project, you can find two types of action, UserAction and TodoAction.
They are also used to map store. So, TodoStore will be responsible for process todo data request and so on.

## Initialization
In FluxJava, you can find a FluxContext as entry point of framework.
FluxContext is a singleton, it helps to hook components together and maintain the instance of components.

You can build FluxContext instance by using Builder inside it as below:
```
FluxContext.getBuilder()
        .setBus(new Bus())
        .setActionHelper(new ActionHelper())
        .setStoreMap(storeMap)
        .build();
```

## Send Request
After UI component get the input from user, it can push an action through ActionCreator which is get from FluxContext.
The built-in ActionCreator only provides one function - `sendRequest`.
UI component has to input which class of action to be created by an Id and the data get from user.
The data can be transfer into the format for store need later in ActionHelper.

Here is the sample code below:

```
Todo todo = new Todo();

FluxContext.getInstance()
        .getActionCreator()
        .sendRequestAsync(TODO_ADD, todo);
```

## Process Data
Base on which bus solution you used, you need to intercept the specific types of action in store.
You may add a function inside store class with annotation like demo project did.
The code below is the example when using EventBus:
```
@Subscribe(threadMode = ThreadMode.BACKGROUND)
public void onAction(final TodoAction inAction) {
    switch (inAction.getType()) {
        case TODO_LOAD:
            ...
            super.emitChange(new ListChangeEvent());
            break;
        case TODO_ADD:
            ...
            super.emitChange(new ListChangeEvent());
            break;
        case TODO_CLOSE:
            ...
            super.emitChange(new ItemChangeEvent(i));
            break;
    }
}
```
If you use `fluxjava-rx`, your store class inherits from RxStore, then you can overwrite `onAction` from RxStore as below:
```
@Override
protected <TAction extends IFluxAction> void onAction(final TAction inAction) {
    final TodoAction action = (TodoAction)inAction;

    switch (action.getType()) {
        case TODO_LOAD:
            ...
            super.emitChange(new ListChangeEvent());
            break;
        case TODO_ADD:
            ...
            super.emitChange(new ListChangeEvent());
            break;
        case TODO_CLOSE:
            ...
            super.emitChange(new ItemChangeEvent(i));
            break;
    }
}
```
## Display Change
As store, UI component want to receive the events from store is also base on which bus you use.
In EventBus case:
```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onEvent(final TodoStore.ListChangeEvent inEvent) {
    super.notifyDataSetChanged();
}
```
In RxBus case:
```
todoStore.toObservable(TodoStore.ListChangeEvent.class)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                new Action1<TodoStore.ListChangeEvent>() {
                    @Override
                    public void call(final TodoStore.ListChangeEvent inEvent) {
                        TodoAdapter.super.notifyDataSetChanged();
                    }
                });
```

License
=======

    Copyright 2016 Bugs will find a way (https://wznote.blogspot.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
