package cn.settile.lzjyzq2.example.pojo;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PeopleTest {

    @Test
    void name() {
        People people = new People();
        people.setName("张三");
        assertEquals("张三", people.getName());
    }
}
