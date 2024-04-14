package com.yupi.springbootinit.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 *
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * ai剩余调用次数
     */
    private Integer aiCount;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}