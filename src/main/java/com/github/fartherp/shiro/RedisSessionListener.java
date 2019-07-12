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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.CachingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author CK
 * @date 2019/1/4
 */
public class RedisSessionListener implements SessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionListener.class);

    private final SessionDAO sessionDAO;

    private final List<CachingRealm> cachingRealms;

    public RedisSessionListener(SessionDAO sessionDAO, List<CachingRealm> cachingRealms) {
        this.sessionDAO = sessionDAO;
        this.cachingRealms = cachingRealms;
    }

    public void onStart(Session session) {
        // do nothing
    }

    public void onStop(Session session) {
        LOGGER.debug("session onStop ID: " + session.getId());
        this.sessionDAO.delete(session);
    }

    public void onExpiration(Session session) {
        LOGGER.debug("session onExpiration ID: " + session.getId());
        this.sessionDAO.delete(session);
        this.cachingRealms.forEach(o -> o.onLogout(SecurityUtils.getSubject().getPrincipals()));
    }
}
