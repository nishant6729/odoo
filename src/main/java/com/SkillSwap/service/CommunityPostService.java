package com.SkillSwap.service;

import com.SkillSwap.model.CommunityPost;
import com.SkillSwap.repository.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommunityPostService {

    @Autowired
    private CommunityPostRepository postRepo;

    public CommunityPost createPost(CommunityPost post) {
        return postRepo.save(post);
    }

    public Optional<CommunityPost> getPostById(String id) {
        return postRepo.findById(id);
    }

    public List<CommunityPost> getPostsByType(String type) {
        return postRepo.findByTypeOrderByTimestampDesc(type);
    }

    public List<CommunityPost> getPostsByTypeAndCategory(String type, String category) {
        return postRepo.findByTypeAndCategoryOrderByTimestampDesc(type, category);
    }
}