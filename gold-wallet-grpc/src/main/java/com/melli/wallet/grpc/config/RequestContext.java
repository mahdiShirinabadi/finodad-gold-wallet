package com.melli.wallet.grpc.config;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import org.apache.logging.log4j.ThreadContext;

/**
 * Class Name: RequestContect
 * Author: Mahdi Shirinabadi
 * Date: 6/2/2025
 */
public class RequestContext {

    private static final ThreadLocal<String> CLIENT_IP = new ThreadLocal<>();
    private static final ThreadLocal<String> UUID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<ChannelEntity> CHANNEL_ENTITY = new ThreadLocal<>();

    public static void setClientIp(String ip) {
        CLIENT_IP.set(ip);
        ThreadContext.put("ipAddress", ip);
    }

    public static String getClientIp() {
        return CLIENT_IP.get();
    }

    public static void setUuid(String uuid) {
        UUID.set(uuid);
        ThreadContext.put("uuid", uuid.toUpperCase().replace("-", ""));
    }

    public static String getUuid() {
        return UUID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
        ThreadContext.put("username", username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setChannelEntity(ChannelEntity channelEntity) {
        CHANNEL_ENTITY.set(channelEntity);
    }

    public static ChannelEntity getChannelEntity() {
        return CHANNEL_ENTITY.get();
    }

    public static void clear() {
        CLIENT_IP.remove();
        UUID.remove();
        USERNAME.remove();
        CHANNEL_ENTITY.remove();
        ThreadContext.clearAll();
    }
}
