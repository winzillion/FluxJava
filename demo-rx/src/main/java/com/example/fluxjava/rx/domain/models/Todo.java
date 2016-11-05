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
package com.example.fluxjava.rx.domain.models;

public class Todo {

    public int id;
    public String title;
    public String dueDate;
    public String memo;
    public boolean closed;

    public Todo() {}

    public Todo(final int inId) {
        id = inId;
    }

    public Todo(final Todo inClone) {
        this.id = inClone.id;
        this.title = inClone.title;
        this.dueDate = inClone.dueDate;
        this.memo = inClone.memo;
        this.closed = inClone.closed;
    }

}
