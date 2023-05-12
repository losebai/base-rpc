package com.base.rpc.processor;

import com.base.core.buffer.ImplBuffer;
import com.base.core.processor.Processor;
import com.base.rpc.Instantiate.InstantiateImpl;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
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
        System.out.println("开始处理消息");
        pool.execute(
                ()->{
                    BaseProtocol.Builder response = msg.toBuilder();
                    response.setReqRes(2);
                    try {
                        InstantiateImpl instantiate = new InstantiateImpl(msg, implBuffer.get(msg.getBody().getClassName().toStringUtf8()));
                        instantiate.invoke();
                        byte[] data = instantiate.getBytes(); // 发送数据，但是不知道数据data-length
                        synchronized (session){
                            session.writeBuffer().writeInt(data.length + 4);
                            session.writeBuffer().write(data);
                            session.writeBuffer().flush();
                        }
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
