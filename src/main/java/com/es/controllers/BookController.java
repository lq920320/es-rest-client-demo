package com.es.controllers;

import com.es.service.BookService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zetu
 * @date 2021/5/10
 */
@RestController
@RequestMapping("api/books")
@Api(tags = "复杂结构 ES 增删改查，复杂查询")
public class BookController {

    @Autowired
    private BookService bookService;


}
