package com.base.io.reactor;

import lombok.Getter;


/**
 * 事件
 *
 * @author bai
 * @date 2023/06/13
 */
@Getter
public class Event {


    /**
     * 处理程序
     */
    private final BaseEventHandler handler;
    /**
     * 类型
     */
    private final EventType type;
    /**
     * 子反应堆id
     */
    private final int subReactorId;

    /**
     * 事件
     *
     * @param handler 处理程序
     */
    public Event(BaseEventHandler handler) {
       this.handler = handler;
        this.type = EventType.READ;
        this.subReactorId = 1;
//        this.subReactorId = handler.getChannel().hashCode() % Runtime.getRuntime().availableProcessors() >> 1; // 子任务数量
    }

}
