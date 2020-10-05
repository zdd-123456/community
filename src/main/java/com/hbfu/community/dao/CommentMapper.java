package com.hbfu.community.dao;

import com.hbfu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    //查询评论条数
    int selectCountByEntity(int entityType,int entityId);

    //插入评论
    int insertComment(Comment comment);

}
