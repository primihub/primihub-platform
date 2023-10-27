package com.primihub.biz.config.mq;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;


public interface SingleTaskChannel {
    String INPUT = "singleTaskInput";
    String SEATUNNEL_INPUT = "singleSeatunnelInput";
    String OUTPUT = "singleTaskOutput";

    @Input(SingleTaskChannel.INPUT)
    SubscribableChannel input();

    @Output(SingleTaskChannel.OUTPUT)
    MessageChannel output();

    @Input(SingleTaskChannel.SEATUNNEL_INPUT)
    SubscribableChannel seatunnelInput();
}
