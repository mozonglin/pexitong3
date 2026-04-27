package com.example.pexitong2.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JPushService {

    private static final Logger log = LoggerFactory.getLogger(JPushService.class);

    @Value("${jpush.app-key}")
    private String appKey;

    @Value("${jpush.master-secret}")
    private String masterSecret;

    private JPushClient jpushClient;

    @PostConstruct
    public void init() {
        jpushClient = new JPushClient(masterSecret, appKey);
    }

    public void pushToStudents(List<String> studentIds, String title, String content, Map<String, String> extras) {
        if (studentIds == null || studentIds.isEmpty()) {
            log.warn("推送目标学生列表为空，跳过推送");
            return;
        }

        try {
            PushPayload.Builder payloadBuilder = PushPayload.newBuilder()
                    .setPlatform(Platform.all())
                    .setAudience(Audience.alias(studentIds))
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(AndroidNotification.newBuilder()
                                    .setAlert(content)
                                    .setTitle(title)
                                    .addExtras(extras != null ? extras : Map.of())
                                    .build())
                            .addPlatformNotification(IosNotification.newBuilder()
                                    .setAlert(content)
                                    .addExtras(extras != null ? extras : Map.of())
                                    .build())
                            .build());

            PushResult result = jpushClient.sendPush(payloadBuilder.build());
            log.info("JPush推送成功，目标学生数: {}, msgId: {}", studentIds.size(), result.msg_id);
        } catch (APIConnectionException e) {
            log.error("JPush连接异常，推送失败: {}", e.getMessage());
        } catch (APIRequestException e) {
            log.error("JPush请求异常，推送失败: status={}, errorCode={}, errorMessage={}",
                    e.getStatus(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("JPush推送发生未知异常: {}", e.getMessage(), e);
        }
    }

    public void pushToAll(String school, String title, String content) {
        try {
            PushPayload payload = PushPayload.newBuilder()
                    .setPlatform(Platform.all())
                    .setAudience(Audience.tag(school))
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(AndroidNotification.newBuilder()
                                    .setAlert(content)
                                    .setTitle(title)
                                    .build())
                            .addPlatformNotification(IosNotification.newBuilder()
                                    .setAlert(content)
                                    .build())
                            .build())
                    .build();

            PushResult result = jpushClient.sendPush(payload);
            log.info("JPush全校推送成功，学校: {}, msgId: {}", school, result.msg_id);
        } catch (APIConnectionException e) {
            log.error("JPush连接异常，全校推送失败: {}", e.getMessage());
        } catch (APIRequestException e) {
            log.error("JPush请求异常，全校推送失败: status={}, errorCode={}, errorMessage={}",
                    e.getStatus(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            log.error("JPush全校推送发生未知异常: {}", e.getMessage(), e);
        }
    }
}
