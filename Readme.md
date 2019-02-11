# shiro-redisson
[![Build Status](https://travis-ci.org/fartherp/shiro-redisson.svg?branch=master)](https://travis-ci.org/fartherp/shiro-redisson)
[![Coverage Status](https://coveralls.io/repos/github/fartherp/shiro-redisson/badge.svg?branch=master)](https://coveralls.io/github/fartherp/shiro-redisson?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fartherp/shiro-redisson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fartherp/shiro-redisson/)
[![GitHub release](https://img.shields.io/github/release/fartherp/shiro-redisson.svg)](https://github.com/fartherp/shiro-redisson/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Project Stats](https://www.openhub.net/p/shiro-redisson/widgets/project_thin_badge.gif)](https://www.openhub.net/p/shiro-redisson)

功能简介

```
1.使用redisson包解决redis缓存
2.解决shiro-redis使用*查询时，导致redis长时间卡死
3.解决使用spring-boot-devtools，出现ClassCastException异常
4.支持redisson提供的编码类型，https://github.com/redisson/redisson/wiki/4.-data-serialization
```
## JDK
> 1.8

## 如何使用？
1. 在项目中加入```shiro-redisson```依赖

    ```Maven```
    ``` xml
    <dependency>
        <groupId>com.github.fartherp</groupId>
        <artifactId>shiro-redisson</artifactId>
        <version>1.0.9</version>
    </dependency>
    ```
    ```Gradle```
    ```
    compile 'com.github.fartherp:shiro-redisson:1.0.9'
    ```

## java使用
``` java

    @Bean
    public MyShiroRealm myShiroRealm() {
        return new MyShiroRealm();
    }
    
    @Bean
    public SessionManager sessionManager(SessionDAO redisSessionDAO, ObjectProvider<SessionListener> sessionListenersProvider) {
        List<SessionListener> sessionListeners = sessionListenersProvider.stream().collect(Collectors.toList());
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO);
        sessionManager.setSessionListeners(sessionListeners);
        return mySessionManager;
    }

    /**
    * 内置session监听器，保证删除session/cache冗余的数据信息
    */
    @Bean
    public SessionListener sessionListener(SessionDAO redisSessionDAO, MyShiroRealm myShiroRealm) {
        return new RedisSessionListener(redisSessionDAO, myShiroRealm);
    }

    @Bean
    public RedisCacheManager cacheManager(RedissonClient redissonClient) {
        return new RedisCacheManager(redissonClient);
    }

    @Bean
    public RedisSessionDAO redisSessionDAO(RedisCacheManager cacheManager) {
        return new RedisSessionDAO(cacheManager);
    }

```
