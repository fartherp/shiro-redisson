/**
 *    Copyright (c) 2019 CK.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.fartherp.shiro.exception;

public class PrincipalInstanceException extends RuntimeException  {

    private static final String MESSAGE = "We need a field to identify this Cache Object in Redis. "
            + "So you need to defined an id field which you can get unique id to identify this principal. "
            + "For example, if you use UserInfo as Principal class, the id field maybe userId, userName, email, etc. "
            + "For example, getUserId(), getUserName(), getEmail(), etc.\n"
            + "Default value is \"id\", that means your principal object has a method called \"getId()\"";

    public PrincipalInstanceException(Class clazz, String idMethodName) {
        super(clazz + " must has getter for field: " +  idMethodName + "\n" + MESSAGE);
    }

    public PrincipalInstanceException(Class clazz, String idMethodName, Exception e) {
        super(clazz + " must has getter for field: " +  idMethodName + "\n" + MESSAGE, e);
    }
}
