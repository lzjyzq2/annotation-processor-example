package cn.settile.lzjyzq2.example;

import java.util.Arrays;

/**
 * @author lzjyz
 */
public class Program {

    public static void main(String[] args) {
        User user = new User();
        user.setId(10000);
        user.setName("admin");
        user.setAge(23);
        user.setSex(1);

        System.out.println("id = " + user.getId());
        System.out.println("name = " + user.getName());
        System.out.println("age = " + user.getAge());
        System.out.println("sex = " + (user.getSex() == 1 ? '男' : '女'));
    }
}
