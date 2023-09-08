package com.dlwlrma.IntelliBi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlwlrma.IntelliBi.common.BaseResponse;
import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.common.ResultUtils;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import com.dlwlrma.IntelliBi.model.entity.AiFrequency;
import com.dlwlrma.IntelliBi.model.entity.User;
import com.dlwlrma.IntelliBi.model.vo.AiFrequencyVO;
import com.dlwlrma.IntelliBi.service.AiFrequencyService;
import com.dlwlrma.IntelliBi.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dlwlrma
 * @createTime 2023/7/11 星期二 23:03
 */
@RestController
@RequestMapping("/aiFrequency")
@CrossOrigin
public class AiFrequencyController {

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private UserService userService;

    /**
     * 用户是否存在，若存在是否有调用次数
     *
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<AiFrequencyVO> getAiFrequency(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<AiFrequency> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        AiFrequency aiFrequency = aiFrequencyService.getOne(queryWrapper);
        if (aiFrequency == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "此id用户不存在");
        }
        Integer remainFrequency = aiFrequency.getRemainFrequency();

        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足1，请及时充值！");
        }
        AiFrequencyVO aiFrequencyVO = new AiFrequencyVO();
        BeanUtils.copyProperties(aiFrequency, aiFrequencyVO);
        return ResultUtils.success(aiFrequencyVO);
    }
}
