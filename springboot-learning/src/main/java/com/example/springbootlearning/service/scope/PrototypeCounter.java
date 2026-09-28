package com.example.springbootlearning.service.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Minh hoạ Prototype Scope:
 * Mỗi lần IoC Container được yêu cầu tạo bean này, nó sẽ tạo một instance MỚI HOÀN TOÀN.
 */
@Component
@Scope("prototype")
public class PrototypeCounter {

    private int count = 0;

    public int incrementAndGet() {
        return ++count;
    }

    public int getCount() {
        return count;
    }

    public String getInstanceId() {
        return "PrototypeInstance@" + Integer.toHexString(System.identityHashCode(this));
    }
}
