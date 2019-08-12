package tech.ascs.icity.iform.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import tech.ascs.icity.common.event.EntityEvent;

@EnableBinding(IFormEvents.class)
public class IFormMessageProducer {

	@Autowired
	@Output(IFormEvents.OUTPUT)
	private MessageChannel channel;

	@SuppressWarnings("rawtypes")
	public void send(EntityEvent entityEvent) {
		Message<EntityEvent> message = MessageBuilder.withPayload(entityEvent)
				.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
				.setHeader("entityType", entityEvent.getEntity().getClass().getName())
				.build();
		channel.send(message);
	}
}
