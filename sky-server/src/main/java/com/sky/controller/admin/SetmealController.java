package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @CacheEvict(cacheNames="setmealCache",key="#setmealDTO.categoryId") //缓存删除
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

    /**
     * 批量删除
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    @CacheEvict(cacheNames="setmealCache",allEntries = true)
    //没法空手套白狼地猜出 URL 里的 1,2,3 应该变成一个 List 还是一个普通的 String。
    public Result delete(@RequestParam List<Long> ids){
        //此时为批量删除故需将ID传入list集合中
        log.info("批量删除：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 根据ID查询套餐
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐")
    //此处ID有路径参数 传递故需要@PathVariable
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据ID查询套餐：{}", id);
        //此处返回给前端VO 因为需要返回菜品名称 DTO 是前端发给你的“请求包”，而 VO 是你发给前端的“展示包”
        //在Service层中用套餐ID查询套餐信息
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames="setmealCache",allEntries = true)
    //前端使用JSON数据传递 故用@RequestBody进行接收转换为DTO
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售")
    @CacheEvict(cacheNames="setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐起售停售：{}", id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
