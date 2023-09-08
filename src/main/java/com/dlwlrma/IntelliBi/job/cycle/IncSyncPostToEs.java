package com.dlwlrma.IntelliBi.job.cycle;

import com.dlwlrma.IntelliBi.model.dto.post.PostEsDTO;
import com.dlwlrma.IntelliBi.model.entity.Post;
import com.dlwlrma.IntelliBi.esdao.PostEsDao;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 增量同步帖子到 es
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private com.dlwlrma.IntelliBi.mapper.PostMapper PostMapper;

    @Resource
    private PostEsDao PostEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Post> PostList = PostMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(PostList)) {
            log.info("no inc Post");
            return;
        }
        List<PostEsDTO> PostEsDTOList = PostList.stream()
                .map(PostEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = PostEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            PostEsDao.saveAll(PostEsDTOList.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
