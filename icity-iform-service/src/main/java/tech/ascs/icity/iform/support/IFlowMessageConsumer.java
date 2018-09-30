package tech.ascs.icity.iform.support;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

import tech.ascs.icity.model.NameEntity;

@EnableBinding(Sink.class)
public class IFlowMessageConsumer {

    @StreamListener(Sink.INPUT)
    public void receive(Message<NameEntity> message) {
        System.out.println("接收到MQ消息: {id: " + message.getPayload().getId() + ", name: " + message.getPayload().getName() + "}");
    }
}
