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
package com.example.fluxjava.rx2.domain;

public class Constants {

    // constants for data type
    public static final int DATA_USER = 10;
    public static final int DATA_TODO = 20;

    // constants for actions of user data
    public static final int USER_LOAD = DATA_USER + 1;

    // constants for actions of todo data
    public static final int TODO_LOAD = DATA_TODO + 1;
    public static final int TODO_ADD = DATA_TODO + 2;
    public static final int TODO_CLOSE = DATA_TODO + 3;

}
