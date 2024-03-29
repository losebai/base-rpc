package com.base.rpc.api;


/**
 * 演示api impl
 *
 * @author bai
 * @date 2023/03/16
 */
public class DemoApiImpl implements DemoApi {

    public DemoApiImpl(){

    }
    @Override
    public String test(String name) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name);
        return "hello " + name;
    }

    @Override
    public int sum(int a, int b) {
        System.out.println(a + " " + b);
        return a + b;
    }

    public int sum(int... nums){
        int t = 0;
        for (int i : nums){
            t+=i;
        }
        return t;
    }
}
