package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.ApiOperation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController //身份声明 这个类是一个 Web 控制器，专门负责处理 HTTP 请求
@RequestMapping("/admin/dish")  //地址导航写在类上面（提取公共路径）
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDto
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    //拦截 HTTP 请求 解析 URL 里的参数。 寻找同名的 DTO 属性 调用 Setter 方法完成赋值
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDto) {
        log.info("菜品分页查询：{}", dishPageQueryDto);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDto);
        return Result.success(pageResult);
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        //@RequestParam强制将 Query 参数转为 List 为了避免歧义
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }
}
