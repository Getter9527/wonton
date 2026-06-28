package com.wonton;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HelloTest {

    @Test
    void testSayHello() {
        System.out.println("Hello World");
    }

    @Test
    void testAdd() {
        int result = 1 + 1;
        // 断言
        Assertions.assertEquals(result, 4, "1+1应该等于2");
    }
}
