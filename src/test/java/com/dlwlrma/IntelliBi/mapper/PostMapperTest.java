package com.dlwlrma.IntelliBi.mapper;

import com.dlwlrma.IntelliBi.model.entity.Post;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子数据库操作测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class PostMapperTest {

    @Resource
    private PostMapper PostMapper;

    @Test
    void listPostWithDelete() {
        List<Post> PostList = PostMapper.listPostWithDelete(new Date());
        Assertions.assertNotNull(PostList);
    }
}