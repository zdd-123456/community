package com.hbfu.community.controller;

import com.hbfu.community.entity.Event;
import com.hbfu.community.entity.Page;
import com.hbfu.community.entity.User;
import com.hbfu.community.event.EventProducer;
import com.hbfu.community.service.FollowService;
import com.hbfu.community.service.UserService;
import com.hbfu.community.util.CommunityConstant;
import com.hbfu.community.util.CommunityUtil;
import com.hbfu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    //关注
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user=hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event=new Event().setTopic(TOPIC_FOLLOW).setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType).setEntityId(entityId).setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");
    }
    //取消关注
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user=hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    //关注列表
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }
    //粉丝列表
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user=userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers"+userId);
        page.setRows((int)followService.findFollowerCount(userId,ENTITY_TYPE_USER));
        List<Map<String ,Object>> userList=followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if (userList!=null){
            for (Map<String ,Object> map :userList){
                User u=(User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";

    }

    private boolean hasFollowed(int userId){
        if (hostHolder.getUser()==null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }

}
