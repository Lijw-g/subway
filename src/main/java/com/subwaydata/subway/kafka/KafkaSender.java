package com.subwaydata.subway.kafka;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.logging.Logger;

/**
 * 消息生产者
 *
 * @author Jarvis
 * @date 2018/8/3
 */
@Component
public class KafkaSender<T> {

    public static Logger logger = Logger.getLogger(KafkaSender.class.getName());

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /***
     * @author: Lijiwen
     * Description:kafka 发送消息
     * @param obj
     * @param topic
     * @return void
     * @createDate
     **/
    public void sendWithTopic(Object obj, String topic) {
        String jsonObj = JSON.toJSONString(obj);
        logger.info("jsonObj" + jsonObj);
        //发送消息
        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, jsonObj);
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logger.info("Produce: The message failed to be sent:" + throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                logger.info("Produce: The message was sent successfully:");
                logger.info("Produce: _+_+_+_+_+_+_+ result: " + stringObjectSendResult.toString());
            }
        });
    }
}