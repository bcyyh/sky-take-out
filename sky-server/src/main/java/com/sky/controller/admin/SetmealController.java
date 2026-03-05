package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 套餐管理
 */
@RestController //身份声明 这个类是一个 Web 控制器，专门负责处理 HTTP 请求
@RequestMapping("/admin/setmeal")//此处为公共路径
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
        //DTO 就像是一个量身定制的快递盒，专门为了适应前端传过来的那堆杂乱数据
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);  //服务层逻辑处理
        return Result.success();
    }


    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        //此处仅仅为请求参数get方法 故不需要@RequestBody 且与DTO参数同名故可以自动赋值
        log.info("分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
}
