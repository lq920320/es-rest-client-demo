package com.es.controllers;

import com.es.common.result.Result;
import com.es.dto.book.ModifyBookReq;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.model.book.Book;
import com.es.model.book.CategoryGroup;
import com.es.service.BookService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("add")
    @ApiOperation("添加图书信息")
    public Result<Boolean> addBook(@RequestBody ModifyBookReq addRequest) {
        Boolean addResult = bookService.add(addRequest);
        return Result.success(addResult);
    }

    @GetMapping("{bookId}")
    @ApiOperation("根据图书ID获取图书信息")
    public Result<Book> addBook(@PathVariable(value = "bookId") Long bookId) {
        Book result = bookService.getById(bookId);
        return Result.success(result);
    }

    @PostMapping("search")
    @ApiOperation("搜索图书信息")
    public Result<List<Book>> searchBook(@RequestBody SearchBookReq searchReq) {
        SearchBookRes searchRes = bookService.searchBook(searchReq);
        return Result.success(searchRes.getBookList(), searchRes.getTotal());
    }


    @GetMapping("categoryGroup")
    @ApiOperation("按照类目分组结果")
    public Result<List<CategoryGroup>> categoryGroup() {
        List<CategoryGroup> result = bookService.categoryGroup();
        return Result.success(result);
    }

}
