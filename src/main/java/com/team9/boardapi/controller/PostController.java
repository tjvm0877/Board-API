package com.team9.boardapi.controller;

import com.team9.boardapi.dto.PostRequestDto;
import com.team9.boardapi.dto.PostResponseDto;
import com.team9.boardapi.dto.ResponseDto;
import com.team9.boardapi.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;


    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> read(@PathVariable Long id){
        return postService.getPost(id);
    }
    @PostMapping("")
    public PostResponseDto createPost(@RequestBody PostRequestDto requestDto){

        return postService.createPost(requestDto);
    }

    @PatchMapping("/{id}")
    public PostResponseDto updatePost(@PathVariable Long id, @RequestBody PostRequestDto requestDto){

        return postService.updatePost(id, requestDto);
    }


}
