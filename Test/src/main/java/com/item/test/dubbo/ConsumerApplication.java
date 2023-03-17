package com.item.test.dubbo;


import java.io.IOException;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;


public class ConsumerApplication {

    public static void main(String[] args) throws IOException {
        ReferenceConfig<GreetingsService> reference = new ReferenceConfig<>();
        reference.setInterface(GreetingsService.class);
        reference.setProtocol(CommonConstants.TRIPLE);
        reference.setProxy(CommonConstants.NATIVE_STUB);
//        DubboBootstrap.getInstance()
//                .application("first-dubbo-consumer")
//                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
//                .reference(reference);

        GreetingsService service = reference.get();
        String message = service.apply("dubbo");
        System.out.println("Receive result ======> " + message);
        System.in.read();


    }
}
