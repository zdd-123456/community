package com.hbfu.community;

import com.hbfu.community.dao.DiscussPostMapper;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133, 0, 100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人,使劲灌水.");
        discussRepository.save(post);
    }

    @Test
    public void testDelete() {
        // discussRepository.deleteById(231);
        discussRepository.deleteAll();
    }

    //    @Test
//    public void testSearchDocumen() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("discusspost");
//        //构建搜索条件
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //QueryBuilders.termQuery 精确查找
////		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "李仙子");
//        //QueryBuilders.matchAllQuery查询所有
////        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
//        MultiMatchQueryBuilder matchAllQueryBuilder=QueryBuilders.multiMatchQuery("互联网寒冬","title","content");
//        searchSourceBuilder.query(matchAllQueryBuilder);
//
//        //设置高亮显示
//        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
//        highlightBuilder.preTags("<em>");
//        highlightBuilder.postTags("</em>");
//        searchSourceBuilder.highlighter(highlightBuilder);
//
//        //构建分页
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        //遍历结果
//        for(SearchHit hit : search.getHits()){
//            Map<String, Object> source = hit.getSourceAsMap();
//            //处理高亮片段
//            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
//            HighlightField titleField = highlightFields.get("title");
//            HighlightField contentField = highlightFields.get("content");
//            if(titleField!=null){
////                Text[] fragments = titleField.fragments()[0];
////                StringBuilder nameTmp = new StringBuilder();
////                for(Text text:fragments){
////                    nameTmp.append(text);
////                }
//                //将高亮片段组装到结果中去
//                source.put("title",titleField.getFragments()[0].toString());
//
//            }
//            if (contentField!=null){
//                source.put("content",contentField.getFragments()[0].toString());
//            }
//        }
//        SearchHits hits = search.getHits();
//        SearchHit[] searchHits = hits.getHits();
//        List<DiscussPost> blogList = new ArrayList<>();
//        for (SearchHit hit : searchHits) {
//            JSONObject jsonObject = new JSONObject(hit.getSourceAsMap());
//            blogList.add(JSONObject.toJavaObject(jsonObject, DiscussPost.class));
//        }
//        System.out.println(blogList);
//
//    }
    @Test
    public void testSearchDocumen() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //QueryBuilders.termQuery 精确查找
//		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "李仙子");
        //QueryBuilders.matchAllQuery查询所有
//        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        MultiMatchQueryBuilder matchAllQueryBuilder=QueryBuilders.multiMatchQuery("因特网寒冬","title","content");
        searchSourceBuilder.query(matchAllQueryBuilder);

        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //构建分页
        searchSourceBuilder.from(1);
        searchSourceBuilder.size(10);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("总量"+search.getHits().getTotalHits().value);

        //遍历结果
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
            System.out.println("数量"+list.size());
        }
    }


}