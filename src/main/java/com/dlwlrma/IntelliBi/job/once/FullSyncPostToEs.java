package com.dlwlrma.IntelliBi.job.once;

import com.dlwlrma.IntelliBi.model.dto.post.PostEsDTO;
import com.dlwlrma.IntelliBi.model.entity.Post;
import com.dlwlrma.IntelliBi.esdao.PostEsDao;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;

/**
 * 全量同步帖子到 es
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private com.dlwlrma.IntelliBi.service.PostService PostService;

    @Resource
    private PostEsDao PostEsDao;

    @Override
    public void run(String... args) {
        List<Post> PostList = PostService.list();
        if (CollectionUtils.isEmpty(PostList)) {
            return;
        }
        List<PostEsDTO> PostEsDTOList = PostList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = PostEsDTOList.size();
        log.info("FullSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            PostEsDao.saveAll(PostEsDTOList.subList(i, end));
        }
        log.info("FullSyncPostToEs end, total {}", total);
    }
}
