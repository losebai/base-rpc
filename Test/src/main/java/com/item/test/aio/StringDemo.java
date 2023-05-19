
package com.item.test.aio;

import com.base.core.Protocol.StringProtocol;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;

public class StringDemo {

    public static void main(String[] args) throws Exception {
        //启动服务端
        MessageProcessor<String> serverProcessor = (session, msg) -> {
            String body = "HTTP/1.1 200\n" +
                    "Set-Cookie: JSESSIONID=d61e031e-bfc5-40ff-b3b2-2b11a41f9909; Path=/tool-admin; HttpOnly; SameSite=lax\n" +
                    "Vary: Access-Control-Request-Method\n" +
                    "Access-Control-Allow-Origin: http://192.168.3.43:8001\n" +
                    "Access-Control-Allow-Credentials: true\n" +
                    "Content-Type: application/json\n" +
                    "Transfer-Encoding: chunked\n" +
                    "Date: Fri, 12 May 2023 07:30:15 GMT\n" +
                    "Keep-Alive: timeout=30\n" +
                    "Connection: keep-alive";
            try {
                byte[] bytes = body.getBytes();
                session.writeBuffer().writeInt(bytes.length);
                session.writeBuffer().write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        AioQuickServer server = new AioQuickServer(8888, new StringProtocol(), serverProcessor);
        server.start(configuration.group());

        //启动客户端
//        MessageProcessor<String> clientProcessor = (session, msg) -> System.out.println("receive data from server：" + msg);
//        AioQuickClient aioQuickClient = new AioQuickClient("localhost", 8888, new StringProtocol(), clientProcessor);
//        AioSession session = aioQuickClient.start();
        byte[] bytes = "smart-socket".getBytes();
        //encode
//        session.writeBuffer().writeInt(bytes.length);
//        session.writeBuffer().write(bytes);
        //flush data
//        session.writeBuffer().flush();

        //shutdown
//        Thread.sleep(1000);
//        aioQuickClient.shutdownNow();
//        server.shutdown();
    }
}
