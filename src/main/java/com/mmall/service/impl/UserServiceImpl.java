package com.mmall.service.impl;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //todo 密码登录MD5
        String md5password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user){
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str,String type){
        //判断type是否为空
        if (StringUtils.isNotBlank(type)){
            //开始校验
            if (Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户已存在");
                }
            }
            if (Const.EMAIL.equals(type)){
                int resultCount= userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question=userMapper.selectQuestionByusername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount=userMapper.checkAnwer(username,question,answer);
        if (resultCount>0){
            //说明问题及答案是这个用户的，并且是正确的
            String forgetToken= UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，Token需要传递");
        }
        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("Token无效或者过期");
        }
        if (StringUtils.equals(token,forgetToken)){
            String md5password=MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount=userMapper.updatePasswordByUsername(username,md5password);
            if (resultCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码错误");
    }

}


