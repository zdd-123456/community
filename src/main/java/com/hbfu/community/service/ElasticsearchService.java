package com.hbfu.community.service;

import com.hbfu.community.dao.elasticsearch.DiscussPostRepository;
import com.hbfu.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void saveDiscussPost(DiscussPost post){
        discussRepository.save(post);
    }
    public void deleteDiscussPost(int id){
        discussRepository.deleteById(id);
    }
    public Map<String,Object> searchDiscussPost(String keyword, int current, int limit){
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder matchAllQueryBuilder= QueryBuilders.multiMatchQuery(keyword,"title","content");

        searchSourceBuilder.query(matchAllQueryBuilder);


        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //构建分页
        searchSourceBuilder.from(current*limit);
        searchSourceBuilder.size(limit);
//        FieldSortBuilder sortBuilder = SortBuilders.fieldSort("creatTime").order(SortOrder.DESC);
//        searchSourceBuilder.sort(sortBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //遍历结果
        System.out.println("数量"+search.getHits().getTotalHits().value);
        Map<String,Object> map=new HashMap<>();
        map.put("count",search.getHits().getTotalHits().value);
        List<DiscussPost> list = new ArrayList<>();
        for(SearchHit hit : search.getHits()){
            DiscussPost post = new DiscussPost();

            String id = hit.getSourceAsMap().get("id").toString();
            post.setId(Integer.valueOf(id));

            String userId = hit.getSourceAsMap().get("userId").toString();
            post.setUserId(Integer.valueOf(userId));

            String title = hit.getSourceAsMap().get("title").toString();
            post.setTitle(title);

            String content = hit.getSourceAsMap().get("content").toString();
            post.setContent(content);

            String status = hit.getSourceAsMap().get("status").toString();
            post.setStatus(Integer.valueOf(status));

            String createTime = hit.getSourceAsMap().get("createTime").toString();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                post.setCreateTime(simpleDateFormat.parse(createTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String commentCount = hit.getSourceAsMap().get("commentCount").toString();
            post.setCommentCount(Integer.valueOf(commentCount));

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                post.setTitle(titleField.getFragments()[0].toString());
            }

            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }

            list.add(post);
        }
        map.put("list",list);
        return map;

    }
}
