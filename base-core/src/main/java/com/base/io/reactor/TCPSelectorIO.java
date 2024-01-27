package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.io.common.BaseIO;
import com.base.io.common.Channel;
import com.base.io.common.TCPProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.base.io.reactor.TCPSession.SESSION_STATUS_ENABLED;

/**
 * io 实际处理逻辑
 *
 * @author bai
 * @date 2024/01/27
 */
public class TCPSelectorIO<T> implements BaseIO<SelectionKey, TCPSession> {


    private static final Logger log = LoggerFactory.getLogger(TCPSelectorIO.class);

    // 事件处理列表
    protected volatile  Map<SelectableChannel, BaseEventHandler<T>> handlerMap = new ConcurrentHashMap<>();

    /**
     * 协议
     */
    private final IOBaseProtocol<T> protocol;

    /**
     * 处理器
     */
    private final TCPProcessor<T> processor;

    public TCPSelectorIO(IOBaseProtocol<T> protocol, TCPProcessor<T> processor){
        this.protocol = protocol;
        this.processor = processor;
    }

    @Override
    public void connect(SelectionKey selectionKey, TCPSession tcpSession) {
        // 注册到子SubReactor上视为已连接
        // todo 这里可能会出现handler为空的情况，原因是主线程put，子线程get
        BaseEventHandler<?> handler = handlerMap.get(selectionKey.channel());
        if (handler != null){
            handler.onConnect();
        }
        log.info("TCPSelectorIO connect");
    }

    @Override
    public void close(SelectionKey selectionKey, TCPSession tcpSession) throws IOException {
        selectionKey.cancel();
        tcpSession.close();
        log.info("TCPSelectorIO close");
    }

    @Override
    public void error(SelectionKey selectionKey, TCPSession tcpSession) {
        log.info("TCPSelectorIO error");
    }

    @Override
    public void handler(SelectionKey key, TCPSession tcpSession) throws IOException {
        BaseEventHandler<?> handler = handlerMap.get(key.channel());
        Channel channel = tcpSession.getChannel();
        if (key.isConnectable()) {
            //与远程服务器建立连接。
            // 子协议
            int i;
        } else if (key.isReadable()) { // read
            log.info(tcpSession.getRemoteAddress() + " read... ");
            ByteBuffer buffer = tcpSession.readBuffer();
            int readNum = channel.read(buffer); // 从缓冲区中读取
            // 读回调
            handler.readable(buffer);
            buffer.flip();
            while (buffer.hasRemaining()) {
                T object = null;
                try {
                    // 解码
                    object = protocol.decode(tcpSession, buffer);

                } catch (Exception e) {
                    tcpSession.setStatus(TCPSession.DECODE_EXCEPTION);
                }
                if (object == null) {
                    break;
                }
                try {
                    // 处理消息
                    processor.process(tcpSession, object);
                } catch (Exception e){
                    tcpSession.setStatus(TCPSession.PROCESS_EXCEPTION);
                }
                log.info(tcpSession.getRemoteAddress() + " size {} read... {}", readNum, object);
            }

            if (tcpSession.status == TCPSession.SESSION_STATUS_CLOSING) {
                log.info(tcpSession.getRemoteAddress() + " read ...close..... ");
                key.channel().close(); // 关闭后, 如果未接收数据
                key.cancel();
            }

            // 压缩读缓冲区
            buffer.compact();
            buffer.clear();
            if (readNum == -1){
                tcpSession.setStatus(TCPSession.SESSION_STATUS_CLOSING);
                return;
            }
//             读完数据后，为 SelectionKey 注册可写事件
            if (!isInterest(key, SelectionKey.OP_WRITE)) {
                key.interestOps(key.interestOps() + SelectionKey.OP_WRITE);
            }

            // 刷新写缓冲区
            tcpSession.flush();
            //   写操作的就绪条件为底层缓冲区有空闲空间，而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理线程全占用整个CPU资源。
            //   所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册
        } else if (key.isWritable()) { // write 事件 当接收到连接时，第二次一直会触发写入事件，直到对方写入数据为止,, 慎用，可直接忽略，采用write写操作就行
            tcpSession.setStatus(SESSION_STATUS_ENABLED); // 连接成功
            log.info(tcpSession.getRemoteAddress() + " write... ");
            // 写完数据后，要把写事件取消，否则当写缓冲区有剩余空间时，会一直触发写事件
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            // 写回调
            handler.writeable();
        }
    }
}
