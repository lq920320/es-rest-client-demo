package com.es.controllers;

import com.es.common.constants.EsConstant;
import com.es.common.result.Result;
import com.es.model.person.ModifyPersonReq;
import com.es.model.person.Person;
import com.es.service.PersonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zetu
 * @date 2021/5/10
 */
@RestController
@RequestMapping("api/person")
@Api(tags = "简单结构 ES 增删改查")
public class PersonController {

    @Autowired
    private PersonService personService;

    @GetMapping
    @ApiOperation("获取所有数据")
    public Result<List<Person>> list() {
        List<Person> result = personService.searchList();
        return Result.success(result);
    }

    @GetMapping("{id}")
    @ApiOperation("根据ID获取单条数据")
    public Result<Person> getById(@PathVariable(value = "id", name = "id") Long id) {
        Person result = personService.getById(id);
        return Result.success(result);
    }

    @PostMapping()
    @ApiOperation("添加插入数据")
    public Result<Boolean> add(@RequestBody ModifyPersonReq modifyReq) {
        Boolean result = personService.add(modifyReq);
        return Result.success(result);
    }

    @PutMapping("{id}")
    @ApiOperation("根据ID更新修改数据")
    public Result<Boolean> update(@PathVariable("id") Long id, @RequestBody ModifyPersonReq modifyReq) {
        Boolean result = personService.update(modifyReq);
        return Result.success(result);
    }

    @DeleteMapping("{id}")
    @ApiOperation("根据ID删除单个数据")
    public Result<Boolean> delete(@PathVariable(value = "id", name = "id") Long id) {
        Boolean result = personService.delete(id);
        return Result.success(result);
    }
}
