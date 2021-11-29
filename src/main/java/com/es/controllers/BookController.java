package com.es.controllers;

import com.es.common.result.Result;
import com.es.dto.book.ModifyBookReq;
import com.es.dto.book.SearchBookReq;
import com.es.dto.book.SearchBookRes;
import com.es.model.book.Book;
import com.es.model.book.CategoryGroup;
import com.es.service.BookService;
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

    @PutMapping("/{bookId}")
    @ApiOperation("更新图书信息")
    public Result<Boolean> updateBook(@PathVariable(value = "bookId") Long bookId, @RequestBody ModifyBookReq addRequest) {
        Boolean addResult = bookService.update(bookId, addRequest);
        return Result.success(addResult);
    }

    @GetMapping("{bookId}")
    @ApiOperation("根据图书ID获取图书信息")
    public Result<Book> getById(@PathVariable(value = "bookId") Long bookId) {
        Book result = bookService.getById(bookId);
        return Result.success(result);
    }

    @PostMapping("search")
    @ApiOperation("搜索图书信息")
    public Result<List<Book>> searchBook(@RequestBody SearchBookReq searchReq) {
        SearchBookRes searchRes = bookService.searchBook(searchReq);
        return Result.success(searchRes.getBookList(), searchRes.getTotal());
    }

    @GetMapping("{bookId}")
    @ApiOperation("根据图书ID删除图书信息")
    public Result<Boolean> deleteById(@PathVariable(value = "bookId") Long bookId) {
        Boolean result = bookService.delete(bookId);
        return Result.success(result);
    }


    @GetMapping("categoryGroup")
    @ApiOperation("按照类目分组结果")
    public Result<List<CategoryGroup>> categoryGroup() {
        List<CategoryGroup> result = bookService.categoryGroup();
        return Result.success(result);
    }

}
