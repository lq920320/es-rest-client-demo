package com.es.controllers;

import com.es.common.result.Result;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.model.book.Book;
import com.es.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

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


    @PostMapping("search")
    @ApiOperation("搜索图书信息")
    public Result<List<Book>> searchBook(@RequestBody SearchBookReq searchReq) {
        SearchBookRes searchRes = bookService.searchBook(searchReq);
        return Result.success(searchRes.getBookList(), searchRes.getTotal());
    }


}
