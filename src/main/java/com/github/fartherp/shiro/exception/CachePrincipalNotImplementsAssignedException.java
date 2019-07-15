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

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/31
 */
public class CachePrincipalNotImplementsAssignedException extends RuntimeException {

	private static final long serialVersionUID = -6914915272655633632L;

    private static final String MESSAGE = "Principal %s must implements com.github.fartherp.shiro.ShiroFieldAccess!";

	public CachePrincipalNotImplementsAssignedException(Class clazz) {
        super(String.format(MESSAGE, clazz));
    }
}
