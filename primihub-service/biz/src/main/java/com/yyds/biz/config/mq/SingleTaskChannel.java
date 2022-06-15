package com.yyds.biz.config.mq;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


public interface SingleTaskChannel {
    String INPUT = "singleTaskInput";
    String OUTPUT = "singleTaskOutput";

    @Input(SingleTaskChannel.INPUT)
    SubscribableChannel input();

    @Output(SingleTaskChannel.OUTPUT)
    MessageChannel output();
}
