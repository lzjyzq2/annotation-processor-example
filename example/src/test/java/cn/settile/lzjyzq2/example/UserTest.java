package cn.settile.lzjyzq2.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void id(){
        User user = new User();
        user.setId(1000);
        assertEquals(1000,user.getId());
    }

    @Test
    void name(){
        User user = new User();
        user.setName("admin");
        assertEquals("admin",user.getName());
    }

    @Test
    void age(){
        User user = new User();
        user.setAge(18);
        assertEquals(18,user.getAge());
    }
    @Test
    void sex(){
        User user = new User();
        user.setSex(1);
        assertEquals(1,user.getSex());
    }
}