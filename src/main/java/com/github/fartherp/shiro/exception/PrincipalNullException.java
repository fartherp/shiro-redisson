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
public class PrincipalNullException extends RuntimeException {

	private static final long serialVersionUID = -7451814784877671981L;

    private static final String MESSAGE = "Principal shouldn't be null";

	public PrincipalNullException() {
		super(MESSAGE);
	}
}
