package com.item.test.dubbo;

public class GreetingsServiceImpl implements GreetingsService{


    @Override
    public String apply(String a) {

        return "apply" + a;
    }
}
