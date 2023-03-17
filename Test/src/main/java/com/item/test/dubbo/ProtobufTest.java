package com.item.test.dubbo;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.item.test.dubbo.demo.DemoProto;
import com.item.test.dubbo.demo.PersonTest;
import com.item.test.util.ProtoJsonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ProtobufTest {


    public static void main(String[] args) throws IOException {

        List<Descriptors.EnumDescriptor> enumDescriptors = DemoProto.getDescriptor().getEnumTypes();
        DescriptorProtos.FileDescriptorProto  proto =  DemoProto.getDescriptor().toProto();

        PersonTest.Builder personBuilder = PersonTest.newBuilder();
        personBuilder.setName("Jet Chen");
        personBuilder.setEmail("ckk505214992@gmail.com");
        personBuilder.setSex(PersonTest.Sex.MALE);

        PersonTest.PhoneNumber.Builder phoneNumberBuilder =  PersonTest.PhoneNumber.newBuilder();
        phoneNumberBuilder.setType(PersonTest.PhoneNumber.PhoneType.MOBILE);
        phoneNumberBuilder.setNumber("17717037257");
        // personTest 设置 PhoneNumber
        personBuilder.addPhone(phoneNumberBuilder);

        PersonTest personTest = personBuilder.build();
        // 序列化
        byte[] bytes = personTest.toByteArray();
        System.out.println(Arrays.toString(bytes));
        // 反序列化
        PersonTest personTestResult = PersonTest.parseFrom(bytes);
        System.out.printf("反序列化得到的信息，姓名：%s，性别：%d，手机号：%s%n", personTestResult.getName(), personTest.getSexValue(), personTest.getPhone(0).getNumber());

        // 序列化
        ByteString byteString = personTest.toByteString();
        System.out.println(byteString.toString());
        // 反序列化
        personTestResult = PersonTest.parseFrom(byteString);
        System.out.printf("反序列化得到的信息，姓名：%s，性别：%d，手机号：%s%n", personTestResult.getName(), personTest.getSexValue(), personTest.getPhone(0).getNumber());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        personTest.writeDelimitedTo(byteArrayOutputStream);
        // 反序列化，从 steam 中读取一个或者多个 protobuf 字节对象
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        personTestResult = PersonTest.parseDelimitedFrom(byteArrayInputStream);
        System.out.printf("反序列化得到的信息，姓名：%s，性别：%d，手机号：%s%n", personTestResult.getName(), personTest.getSexValue(), personTest.getPhone(0).getNumber());

        // 转化为json ,不支持any
        String json =  ProtoJsonUtils.toJson(personTest);
        System.out.println(json);
        // to message
        ProtoJsonUtils.toProtoBean(PersonTest.newBuilder(), json);
    }
}
