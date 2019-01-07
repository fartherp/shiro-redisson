/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.CachingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: CK
 * @date: 2019/1/4
 */
public class RedisSessionListener implements SessionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionListener.class);

    private SessionDAO sessionDAO;

    private CachingRealm cachingRealm;

    public RedisSessionListener(SessionDAO sessionDAO, CachingRealm cachingRealm) {
        this.sessionDAO = sessionDAO;
        this.cachingRealm = cachingRealm;
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
        this.cachingRealm.onLogout(SecurityUtils.getSubject().getPrincipals());
    }
}
