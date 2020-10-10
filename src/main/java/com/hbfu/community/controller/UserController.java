package com.hbfu.community.controller;

import com.hbfu.community.annotation.LoginRequired;
import com.hbfu.community.dao.UserMapper;
import com.hbfu.community.entity.User;
import com.hbfu.community.service.FollowService;
import com.hbfu.community.service.LikeService;
import com.hbfu.community.service.UserService;
import com.hbfu.community.util.CommunityConstant;
import com.hbfu.community.util.CommunityUtil;
import com.hbfu.community.util.HostHolder;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private  String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    //更改用户头像
    public  String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return  "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest=new File(uploadPath+"/"+fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常"+e);
        }
        //更新当前用户的头像路径
        User user=hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }
    //获取头像
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        //文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);

        try (FileInputStream fis=new FileInputStream(fileName);
             OutputStream os=response.getOutputStream();
        ){

            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }
    //更改用户密码
    @RequestMapping(path = "/changePassword",method = RequestMethod.POST)
    public String changePassword(String oldPassword,String newPassword,@CookieValue("ticket") String ticket,Model model){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","请输入原密码");
            return "site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","请输入新密码");
            return "site/setting";
        }
        User user=hostHolder.getUser();
        if(!user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt()))){
            model.addAttribute("oldPasswordMsg","原密码错误");
            return "site/setting";
        }
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        userService.logout(ticket);
        model.addAttribute("msg","修改成功！");
        model.addAttribute("target","/login");
        return "site/operate-result";
    }
    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user=userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount=likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount=followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount=followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed=false;
        if (hostHolder.getUser()!=null){
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }
}

