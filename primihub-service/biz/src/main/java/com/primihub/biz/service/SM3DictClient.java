package com.primihub.biz.service;


import com.primihub.biz.entity.SM3Dict;
import com.primihub.grpc.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SM3DictClient {

    private QueryGrpc.QueryBlockingStub blockingStub;

    private final Channel channel;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public SM3DictClient(String address, Integer port) {
        this.channel = getDefaultTypeChannel(address, port);
        this.blockingStub = QueryGrpc.newBlockingStub(channel);
    }

    /**
     * @param fieldValueSet sm3 id_num set
     */
    public List<SM3Dict> grpcQuery(Set<String> fieldValueSet) {
        log.info("[query][query size] --- [{}]", fieldValueSet.size());
        List<InputObject> requestList = fieldValueSet.stream().map(s -> InputObject.newBuilder().setIdNum(s).build()).collect(Collectors.toList());
        QueryRequest grpcRequest = QueryRequest.newBuilder().addAllRequestList(requestList).build();
        QueryReply grpcResponse;
        try {
            grpcResponse = blockingStub.queryData(grpcRequest);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            return null;
        }
        log.info("[query][return size] --- [{}]", grpcResponse.getReplyListCount());

        List<OutputObject> replyListList = grpcResponse.getReplyListList();
        List<SM3Dict> poList = replyListList.stream().map(this::convertGRPCVoToPo).collect(Collectors.toList());
        return poList;
    }

    private SM3Dict convertGRPCVoToPo(OutputObject outputObject) {
        SM3Dict sm3Dict = new SM3Dict();
        sm3Dict.setIdNum(outputObject.getIdNum());
        sm3Dict.setPhoneNum(outputObject.getPhoneNum());
        return sm3Dict;
    }

    public List<SM3Dict> commit(Set<String> fieldValueSet) {

        try {
            return this.grpcQuery(fieldValueSet);
        } finally {
            try {
                ((ManagedChannel) channel).shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("[grpc channel][close channel error]", e);
            }
        }
    }

    static Channel getDefaultTypeChannel(String grpcClientAddress, Integer grpcClientPort) {
        return ManagedChannelBuilder
                .forAddress(grpcClientAddress, grpcClientPort)
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .maxInboundMetadataSize(Integer.MAX_VALUE)
                .usePlaintext()
                .build();
    }
}
