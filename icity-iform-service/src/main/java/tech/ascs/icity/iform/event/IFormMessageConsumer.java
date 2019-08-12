package tech.ascs.icity.iform.event;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;

import tech.ascs.icity.common.event.EntityEvent;

@EnableBinding(IFormEvents.class)
public class IFormMessageConsumer {

    @SuppressWarnings("rawtypes")
	@StreamListener(IFormEvents.INPUT_IFLOW)
    public void receive(Message<EntityEvent> message) {
        System.out.println("接收到IFlow消息:  " + message.getPayload());
    }
}
