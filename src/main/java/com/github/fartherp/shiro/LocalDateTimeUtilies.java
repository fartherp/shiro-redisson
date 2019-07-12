/*
 * Copyright (c) 2019. CK. All rights reserved.
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
