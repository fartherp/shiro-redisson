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
package com.github.fartherp.shiro;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/25
 */
public class LocalDateTimeUtilies {

    public static long getTimestamp(Function<LocalDateTime, LocalDateTime> function) {
        return getTimestamp(LocalDateTime.now(), function);
    }

    public static long getTimestamp(LocalDateTime localDateTime, Function<LocalDateTime, LocalDateTime> function) {
        return function.apply(localDateTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
