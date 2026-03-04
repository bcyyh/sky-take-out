package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 套餐管理
 */
@RestController //身份声明 这个类是一个 Web 控制器，专门负责处理 HTTP 请求
@RequestMapping("/admin/setmeal")
@Slf4j

public class SetmealController {
    @Autowired  //自动装配需要实现类Service继承接口 并注入@Service
    private SetmealService setmealService;
    /**
     * 新增套餐 新增post 更新put
     * @param
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    //请求参数是JSON 故需要@RequestBody
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);  //服务层逻辑处理
        return Result.success();
    }
}
