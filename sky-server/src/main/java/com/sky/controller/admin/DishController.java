package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    /**
     * 根据id查询菜品
     */

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        //此处用的是DishVo因为 里面还需要有口味的相关数据 DishVo中有口味的集合
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品 此时无返回值 泛型不用写了 put更新 post新增
     */

    @PutMapping
    @ApiOperation("修改菜品")
    //请求体用@RequestBody @PathVariable路径参数
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);  //注意此处接收时是DTO 返回时是VO
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }


    /**
     * 菜品起售停售 Path： /admin/dish/status/{status}
     */
    @ApiOperation("菜品起售停售")
    @PostMapping("/status/{status}")  //一个动作的更新用 POST
    public Result startOrStop(@PathVariable Integer status, Long id){
        //url中有路径参数 故此处用@PathVariabl
        log.info("菜品起售停售：{}", id);
        dishService.startOrStop(status, id);  //状态码 菜品id是必须的必须知道菜品才可以进行状态的修改
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品：{}", categoryId);
        List<DishVO> list = dishService.list(categoryId);
        return Result.success(list);
    }

}
