package cn.settile.lzjyzq2.example;

import cn.settile.lzjyzq2.processor.annotation.Getter;
import cn.settile.lzjyzq2.processor.annotation.Setter;

/**
 * @author lzjyz
 */
@Getter
@Setter
public class User {

    private Integer id;

    private String name;

    private int age;

    private int sex;

}
