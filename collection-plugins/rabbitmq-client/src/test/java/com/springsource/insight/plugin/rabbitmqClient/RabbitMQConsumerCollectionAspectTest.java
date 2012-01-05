/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.rabbitmqClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.rabbitmq.client.AMQP.Basic.RecoverOk;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Channel.FlowOk;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;
import com.rabbitmq.client.AMQP.Exchange.BindOk;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Exchange.DeleteOk;
import com.rabbitmq.client.AMQP.Exchange.UnbindOk;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.AMQP.Tx.CommitOk;
import com.rabbitmq.client.AMQP.Tx.RollbackOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.FlowListener;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;

public class RabbitMQConsumerCollectionAspectTest extends OperationCollectionAspectTestSupport {
    
    @Test
    public void testHandleDelivery() throws IOException {
        MockConsumer consumer = new MockConsumer();
        
        String consumerTag = "1";
        Envelope envelope = new Envelope(1l, false, "exchange", "routingKey");
        BasicProperties props = create();
        byte[] body = new byte[25];
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        consumer.handleDelivery(consumerTag, envelope, props, body);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        assertOperation(envelope, props, body);
    }
    
    @Test
    public void testBasicGet() throws IOException {
        
        Envelope envelope = new Envelope(1l, false, "exchange", "routingKey");
        BasicProperties props = create();
        byte[] body = new byte[25];
        
        MockChannel channel = new MockChannel(envelope, props, body);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        channel.basicGet("rk", false);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        assertOperation(envelope, props, body);
    }

    void assertOperation(Envelope envelope, BasicProperties props, byte[] body) {
        Operation op = getOperation();
        
        assertEquals(OperationType.valueOf("rabbitmq-client-consumer"), op.getType());
        assertEquals("Consume", op.getLabel());
        assertEquals(body.length, (int)op.get("bytes", Integer.class));
        
        OperationMap envMap = op.get("envelope", OperationMap.class);
        
        assertNotNull(envMap);
        assertEquals(envelope.getDeliveryTag() , (long)envMap.get("deliveryTag", Long.class));
        assertEquals(envelope.getExchange() , envMap.get("exchange", String.class));
        assertEquals(envelope.getRoutingKey() , envMap.get("routingKey", String.class));
        
        assertNull(op.get("connectionUrl"));
        assertNull(op.get("serverVersion"));
        assertNull(op.get("clientVersion"));
        
        OperationMap propsMap = op.get("props", OperationMap.class);
        
        assertEquals(props.getAppId(), propsMap.get("App Id"));
        assertEquals(props.getContentEncoding(), propsMap.get("Content Encoding"));
        assertEquals(props.getContentType(), propsMap.get("Content Type"));
        assertEquals(props.getDeliveryMode(), propsMap.get("Delivery Mode"));
        assertEquals(props.getExpiration(), propsMap.get("Expiration"));
    }

    private Operation getOperation() {
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        operation.finalizeConstruction();
        getAspect().setCollector(originalOperationCollector);
        return operation;
    }
    
    private BasicProperties create() {
        BasicProperties.Builder builder = new BasicProperties.Builder();
        
        builder.appId("RabbitMQ")
               .contentEncoding("UTF-8")
               .contentType("TEXT")
               .correlationId("None")
               .deliveryMode(3)
               .expiration("Never")
               .messageId("Message-1")
               .priority(4)
               .timestamp(new Date());
        
        return builder.build();
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return RabbitMQConsumerCollectionAspect.aspectOf();
    }
    
    private static final class MockConsumer implements Consumer {
        public void handleCancel(String arg0) throws IOException {
        }

        public void handleCancelOk(String arg0) {
        }

        public void handleConsumeOk(String arg0) {
        }

        public void handleDelivery(String consumerTag, Envelope envelope,
                BasicProperties props, byte[] body) throws IOException {
        }

        public void handleRecoverOk() {
        }

        public void handleShutdownSignal(String arg0, ShutdownSignalException arg1) {
        }
    }
    
    private static final class MockChannel implements Channel {
        private Envelope envelope;
        private BasicProperties props;
        private byte[] body;
        
        public MockChannel(Envelope envelope, BasicProperties props, byte[] body) {
            super();
            this.envelope = envelope;
            this.props = props;
            this.body = body;
        }

        public void addShutdownListener(ShutdownListener arg0) {
        }

        public ShutdownSignalException getCloseReason() {
            return null;
        }

        public boolean isOpen() {
            return false;
        }

        public void notifyListeners() {
        }

        public void removeShutdownListener(ShutdownListener arg0) {
        }

        public void abort() throws IOException {
        }

        public void abort(int arg0, String arg1) throws IOException {
        }

        public void asyncRpc(Method arg0) throws IOException {
        }

        public void basicAck(long arg0, boolean arg1) throws IOException {
        }

        public void basicCancel(String arg0) throws IOException {
        }

        public String basicConsume(String arg0, Consumer arg1)
                throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, Consumer arg2)
                throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, String arg2,
                Consumer arg3) throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, String arg2,
                boolean arg3, boolean arg4, Map<String, Object> arg5,
                Consumer arg6) throws IOException {
            return null;
        }

        public GetResponse basicGet(String arg0, boolean arg1)
                throws IOException {

            GetResponse resp = mock(GetResponse.class);
            when(resp.getBody()).thenReturn(body);
            when(resp.getEnvelope()).thenReturn(envelope);
            when(resp.getProps()).thenReturn(props);
            
            return resp;
        }

        public void basicNack(long arg0, boolean arg1, boolean arg2)
                throws IOException {
        }

        public void basicPublish(String arg0, String arg1,
                BasicProperties arg2, byte[] arg3) throws IOException {
        }

        public void basicPublish(String arg0, String arg1, boolean arg2,
                boolean arg3, BasicProperties arg4, byte[] arg5)
                throws IOException {
        }

        public void basicQos(int arg0) throws IOException {
        }

        public void basicQos(int arg0, int arg1, boolean arg2)
                throws IOException {
        }

        public RecoverOk basicRecover() throws IOException {
            return null;
        }

        public RecoverOk basicRecover(boolean arg0) throws IOException {
            return null;
        }

        public void basicRecoverAsync(boolean arg0) throws IOException {
        }

        public void basicReject(long arg0, boolean arg1) throws IOException {
        }

        public void close() throws IOException {
        }

        public void close(int arg0, String arg1) throws IOException {
        }

        public SelectOk confirmSelect() throws IOException {
            return null;
        }

        public BindOk exchangeBind(String arg0, String arg1, String arg2)
                throws IOException {
            return null;
        }

        public BindOk exchangeBind(String arg0, String arg1, String arg2,
                Map<String, Object> arg3) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1, boolean arg2)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1,
                boolean arg2, boolean arg3, Map<String, Object> arg4)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1,
                boolean arg2, boolean arg3, boolean arg4,
                Map<String, Object> arg5) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclarePassive(String arg0) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String arg0) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String arg0, boolean arg1)
                throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2)
                throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2,
                Map<String, Object> arg3) throws IOException {
            return null;
        }

        public FlowOk flow(boolean arg0) throws IOException {
            return null;
        }

        public int getChannelNumber() {
            return 0;
        }

        public ConfirmListener getConfirmListener() {
            return null;
        }

        public Connection getConnection() {
            return null;
        }

        public Consumer getDefaultConsumer() {
            return null;
        }

        public FlowOk getFlow() {
            return null;
        }

        public FlowListener getFlowListener() {
            return null;
        }

        public long getNextPublishSeqNo() {
            return 0;
        }

        public ReturnListener getReturnListener() {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0,
                String arg1, String arg2) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0,
                String arg1, String arg2, Map<String, Object> arg3)
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare()
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare(
                String arg0, boolean arg1, boolean arg2, boolean arg3,
                Map<String, Object> arg4) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclarePassive(
                String arg0) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0)
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0,
                boolean arg1, boolean arg2) throws IOException {
            return null;
        }

        public PurgeOk queuePurge(String arg0) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0,
                String arg1, String arg2) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0,
                String arg1, String arg2, Map<String, Object> arg3)
                throws IOException {
            return null;
        }

        public Method rpc(Method arg0) throws IOException {
            return null;
        }

        public void setConfirmListener(ConfirmListener arg0) {
        }

        public void setDefaultConsumer(Consumer arg0) {
        }

        public void setFlowListener(FlowListener arg0) {
        }

        public void setReturnListener(ReturnListener arg0) {
        }

        public CommitOk txCommit() throws IOException {
            return null;
        }

        public RollbackOk txRollback() throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Tx.SelectOk txSelect()
                throws IOException {
            return null;
        }
    }

}
