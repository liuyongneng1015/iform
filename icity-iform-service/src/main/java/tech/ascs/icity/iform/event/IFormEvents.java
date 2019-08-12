package tech.ascs.icity.iform.event;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface IFormEvents {

	String OUTPUT = "output";

	String INPUT_IFLOW = "inputIFlow";

	@Output(IFormEvents.OUTPUT)
    MessageChannel output();

	@Input(IFormEvents.INPUT_IFLOW)
    SubscribableChannel inputIFlow();
}
