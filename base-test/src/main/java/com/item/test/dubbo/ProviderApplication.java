//package com.item.test.dubbo;
//
//
//import org.apache.dubbo.config.ProtocolConfig;
//import org.apache.dubbo.config.RegistryConfig;
//import org.apache.dubbo.config.ServiceConfig;
//import org.apache.dubbo.config.bootstrap.DubboBootstrap;
//
//public class ProviderApplication {
//    public static void main(String[] args) {
//        // 定义具体的服务
//        ServiceConfig<GreetingsService> service = new ServiceConfig<>();
//        service.setInterface(GreetingsService.class);
//        service.setRef(new GreetingsServiceImpl());
//
//        // 启动 Dubbo
//        DubboBootstrap.getInstance()
//                .application("first-dubbo-provider")
//                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
//                .protocol(new ProtocolConfig("dubbo", -1))
//                .service(service)
//                .start()
//                .await();
//    }
//}