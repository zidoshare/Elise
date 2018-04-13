package com.hnqc.ironhand.common.sender;

import com.hnqc.ironhand.common.pojo.Seed;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DownLoadSender {
    private KafkaTemplate<Integer, Object> template;

    public DownLoadSender(KafkaTemplate<Integer, Object> template) {
        this.template = template;
    }

    public void send(Seed message) {
        template.send("download", message);
    }
}