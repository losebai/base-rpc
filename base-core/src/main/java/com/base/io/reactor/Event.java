package com.base.io.reactor;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 事件
 *
 * @author bai
 * @date 2023/06/13
 */
@Getter
public class Event {

    /**
     * 事件类型
     *
     * @author bai
     * @date 2023/06/13
     */

    /**
     * 处理程序
     */
    private final List<EventHandler> handlers = new ArrayList<>();
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
    public Event(EventHandler... handler) {
        this.handlers.addAll(Arrays.asList(handler));
        this.type = EventType.READ;
        this.subReactorId = 1;
//        this.subReactorId = handler.getChannel().hashCode() % Runtime.getRuntime().availableProcessors() >> 1; // 子任务数量
    }


    public boolean addEventHandler(EventHandler handler){
        return handlers.add(handler);
    }
}
