package com.SkillSwap.controller;

import com.SkillSwap.model.CommunityPost;
import com.SkillSwap.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private CommunityPostService postService;

    @PostMapping
    public CommunityPost createPost(@RequestBody CommunityPost post) {
        return postService.createPost(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityPost> getPostById(@PathVariable String id) {
        Optional<CommunityPost> post = postService.getPostById(id);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<CommunityPost> getPostsByType(@RequestParam String type,
                                              @RequestParam(required = false) String category) {
        return category == null
            ? postService.getPostsByType(type)
            : postService.getPostsByTypeAndCategory(type, category);
    }
}