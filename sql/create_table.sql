# 建表脚本
-- 创建库
create database if not exists bi_db;

-- 切换库
use bi_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表表
create table if not exists chart
(
    id          bigint auto_increment comment 'id' primary key,
    goal        text                                   null comment '分析目标',
    `name`      varchar(128)                           null comment '图表名称',
    chartType   varchar(128)                           null comment '图表类型',
    genChart    text                                   null comment '生成的图表数据',
    genResult   text                                   null comment '生成的分析结论',
    chartStatus varchar(128) default 'wait'            not null comment 'wait-等待,running-生成中,succeed-成功生成,failed-生成失败',
    execMessage text                                   null comment '执行信息',
    userId      bigint                                 null comment '创建用户 id',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

-- AI调用次数表
create table if not exists ai_frequency
(
    id              bigint auto_increment comment 'id' primary key,
    userId          bigint                             not null comment '用户 id',
    totalFrequency  bigint   default 0                 not null comment '总调用次数',
    remainFrequency int      default 5                 not null comment '剩余调用次数',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除'
) comment 'ai调用次数表' collate = utf8mb4_unicode_ci;


-- 将图表信息表当中的信息字段进行分表处理
create table if not exists chart_data
(
    id         bigint auto_increment comment 'id' primary key,
    chartId     bigint                             not null comment '图表 id',
    chartData  text                               null comment '图表数据',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图表原数据表' collate = utf8mb4_unicode_ci;