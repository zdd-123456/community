package com.hbfu.community.dao;

import com.hbfu.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    //添加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //获取帖子详情
    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

}
