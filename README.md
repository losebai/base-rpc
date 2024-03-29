# base-rpc

#### 介绍
smart-socket(aio) + 主多从分类reactor(nio) + protobuf

#### 快速入门

### 事件处理
为io处理提供了多个回调
```java
public interface EventHandler<T> {
    /**
     * 连接回调
     */
    void onConnect();

    /**
     * 过程
     *
     * @param t t
     */
    T process(T t);

    /**
     * 打开回调
     *
     * @param t t
     */
    void onOpen(T t);

    /**
     * 在消息
     *
     * @param t t
     */
    void onMessage(T t);


    /**
     * 可读回调
     *
     * @param buffer 缓冲
     */
    void readable(ByteBuffer buffer);


    /**
     * 可写回调
     */
    void writeable();


    /**
     * 在关闭
     */
    void onClose();

}
```
#### nio-reactor

##### 服务端
###### 解码器

```java
public static class StringIOBaseProtocol implements IOBaseProtocol<String> {

        private final Map<TCPSession, FixedLengthFrameDecoder> decoderMap = new ConcurrentHashMap<>();

        @Override
        public String decode(TCPSession tcpSession, ByteBuffer readBuffer) {
            int remaining = readBuffer.remaining();
            if (remaining < Integer.BYTES) {
                return null;
            }
            readBuffer.mark();
            int length = readBuffer.getInt();
            //消息长度超过缓冲区容量引发的半包,启用定长消息解码器,本次解码失败
            if (length + Integer.BYTES > readBuffer.capacity()) {
                FixedLengthFrameDecoder fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
                decoderMap.put(tcpSession, fixedLengthFrameDecoder);
                return null;
            }
            //半包，解码失败
            if (length > readBuffer.remaining()) {
                readBuffer.reset();
                return null;
            }
            return convert(readBuffer, length);
        }

        /**
         * 消息解码
         */
        private String convert(ByteBuffer byteBuffer, int length) {
            byte[] b = new byte[length];
            byteBuffer.get(b);
            return new String(b, StandardCharsets.UTF_8);
        }
    }
```
###### 通道处理器

```java
public static class StringProcessor implements TCPProcessor<String> {


        @Override
        public void process(TCPSession session, String msg) {
            System.out.printf(msg);
            if (msg.equals("你好2")) {
                byte[] bytes = "收到".getBytes();
                ByteBuffer buffer = ByteBuffer.allocate(Config.WRITE_BUFFER_SIZE);
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                session.setWriteBuffer(buffer);
            }
        }
    }
```
###### 启动服务端

```java
public static void main(String[] args) throws IOException {
        ReactorSocketServer reactorSocketServer = new ReactorSocketServer("127.0.0.1", 7777,
                new StringIOBaseProtocol(), new StringProcessor());
        reactorSocketServer.start();
    }
```


##### 客户端
```java
public static void main(String[] args) throws Exception {
        //启动我们客户端
        ReactorClient chatClient = new ReactorClient("127.0.0.1", 7777, new StringIOBaseProtocol(), new StringProcessor());
        chatClient.start();
        chatClient.send("你好1".getBytes());
        chatClient.send("你好2".getBytes());
        chatClient.send("你好3".getBytes());
        chatClient.send("你好4".getBytes());
        Thread.sleep(1000);
        chatClient.stop();
    }
```
#### 如下实例
![img.png](img.png)
![img_1.png](img_1.png)




