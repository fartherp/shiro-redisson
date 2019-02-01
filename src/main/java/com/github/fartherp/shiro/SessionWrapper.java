/*
 * Copyright (c) 2019. CK. All rights reserved.
 */

package com.github.fartherp.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.util.CollectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: CK
 * Date: 2019/2/1
 */
public class SessionWrapper implements Serializable {
    private Serializable id;
    private Date startTimestamp;
    private Date stopTimestamp;
    private Date lastAccessTime;
    private long timeout;
    private boolean expired;
    private String host;
    private Map<Object, Object> attributes;

    private Date createTime;

    public Session getSession() {
        SimpleSession simpleSession = new SimpleSession();
        simpleSession.setId(this.id);
        simpleSession.setStartTimestamp(this.startTimestamp);
        simpleSession.setStopTimestamp(this.stopTimestamp);
        simpleSession.setLastAccessTime(this.lastAccessTime);
        simpleSession.setTimeout(this.timeout);
        simpleSession.setExpired(this.expired);
        simpleSession.setHost(this.host);
        simpleSession.setAttributes(attributes);
        return simpleSession;
    }

    public void setSession(Session session) {
        this.id = session.getId();
        this.startTimestamp = session.getStartTimestamp();
        this.lastAccessTime = session.getLastAccessTime();
        this.timeout = session.getTimeout();
        this.host = session.getHost();
        if (session instanceof SimpleSession) {
            this.stopTimestamp = ((SimpleSession) session).getStopTimestamp();
            this.expired = ((SimpleSession) session).isExpired();
            this.attributes = ((SimpleSession) session).getAttributes();
        }
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Date stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<Object, Object> attributes) {
        this.attributes = attributes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        short alteredFieldsBitMask = getAlteredFieldsBitMask();
        out.writeShort(alteredFieldsBitMask);
        if (id != null) {
            out.writeObject(id);
        }
        if (startTimestamp != null) {
            out.writeObject(startTimestamp);
        }
        if (stopTimestamp != null) {
            out.writeObject(stopTimestamp);
        }
        if (lastAccessTime != null) {
            out.writeObject(lastAccessTime);
        }
        if (timeout != 0l) {
            out.writeLong(timeout);
        }
        if (expired) {
            out.writeBoolean(expired);
        }
        if (host != null) {
            out.writeUTF(host);
        }
        if (!CollectionUtils.isEmpty(attributes)) {
            out.writeObject(attributes);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        short bitMask = in.readShort();

        if (isFieldPresent(bitMask, ID_BIT_MASK)) {
            this.id = (Serializable) in.readObject();
        }
        if (isFieldPresent(bitMask, START_TIMESTAMP_BIT_MASK)) {
            this.startTimestamp = (Date) in.readObject();
        }
        if (isFieldPresent(bitMask, STOP_TIMESTAMP_BIT_MASK)) {
            this.stopTimestamp = (Date) in.readObject();
        }
        if (isFieldPresent(bitMask, LAST_ACCESS_TIME_BIT_MASK)) {
            this.lastAccessTime = (Date) in.readObject();
        }
        if (isFieldPresent(bitMask, TIMEOUT_BIT_MASK)) {
            this.timeout = in.readLong();
        }
        if (isFieldPresent(bitMask, EXPIRED_BIT_MASK)) {
            this.expired = in.readBoolean();
        }
        if (isFieldPresent(bitMask, HOST_BIT_MASK)) {
            this.host = in.readUTF();
        }
        if (isFieldPresent(bitMask, ATTRIBUTES_BIT_MASK)) {
            this.attributes = (Map<Object, Object>) in.readObject();
        }
    }

    static int bitIndexCounter = 0;
    private static final int ID_BIT_MASK = 1 << bitIndexCounter++;
    private static final int START_TIMESTAMP_BIT_MASK = 1 << bitIndexCounter++;
    private static final int STOP_TIMESTAMP_BIT_MASK = 1 << bitIndexCounter++;
    private static final int LAST_ACCESS_TIME_BIT_MASK = 1 << bitIndexCounter++;
    private static final int TIMEOUT_BIT_MASK = 1 << bitIndexCounter++;
    private static final int EXPIRED_BIT_MASK = 1 << bitIndexCounter++;
    private static final int HOST_BIT_MASK = 1 << bitIndexCounter++;
    private static final int ATTRIBUTES_BIT_MASK = 1 << bitIndexCounter++;

    private short getAlteredFieldsBitMask() {
        int bitMask = 0;
        bitMask = id != null ? bitMask | ID_BIT_MASK : bitMask;
        bitMask = startTimestamp != null ? bitMask | START_TIMESTAMP_BIT_MASK : bitMask;
        bitMask = stopTimestamp != null ? bitMask | STOP_TIMESTAMP_BIT_MASK : bitMask;
        bitMask = lastAccessTime != null ? bitMask | LAST_ACCESS_TIME_BIT_MASK : bitMask;
        bitMask = timeout != 0l ? bitMask | TIMEOUT_BIT_MASK : bitMask;
        bitMask = expired ? bitMask | EXPIRED_BIT_MASK : bitMask;
        bitMask = host != null ? bitMask | HOST_BIT_MASK : bitMask;
        bitMask = !CollectionUtils.isEmpty(attributes) ? bitMask | ATTRIBUTES_BIT_MASK : bitMask;
        return (short) bitMask;
    }

    private static boolean isFieldPresent(short bitMask, int fieldBitMask) {
        return (bitMask & fieldBitMask) != 0;
    }
}
