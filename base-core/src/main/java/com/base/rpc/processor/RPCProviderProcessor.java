package com.base.rpc.processor;

import com.base.core.buffer.ImplBuffer;
import com.base.core.processor.Processor;
import com.base.core.util.ByteStringUtil;
import com.base.rpc.Instantiate.InstantiateImpl;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * rpcprovider处理器
 *
 * @author bai
 * @date 2023/03/28
 */
@Slf4j
public class RPCProviderProcessor implements Processor<BaseProtocol> {

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private final Map<String, Class<?>> implBuffer = new ImplBuffer<>();

    @Override
    public void process(AioSession session, BaseProtocol msg) {
        pool.execute(
                ()->{
                    BaseProtocol.Builder response = msg.toBuilder();
                    BaseProtocol.Body.Builder body  = response.getBody().toBuilder();
                    response.setReqRes(2);
                    try {
                        InstantiateImpl instantiate = new InstantiateImpl(msg, implBuffer.get(msg.getBody().getClassName().toStringUtf8()));
                        Object result = instantiate.invoke();
                        byte[] resultByte = ByteStringUtil.ToByte(result);
                        body.setReturn(ByteString.copyFrom(resultByte));
                        body.setMethodLength(resultByte.length);
                        response.setBody(body);
                        byte[] data =  response.build().toByteArray(); // 发送数据
                        synchronized (session){
                            session.writeBuffer().writeInt(data.length + 4);
                            session.writeBuffer().write(data);
                            session.writeBuffer().flush();
                        }
                        log.info( "response: "+ response.toString());
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                             InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public void pushImplClass(Class<?> _class, Class<?> _impl_class){
        log.info("载入" + _class.getName());
        implBuffer.put(_class.getName(), _impl_class);
    }

}
