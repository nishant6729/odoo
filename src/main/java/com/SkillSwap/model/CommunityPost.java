package com.SkillSwap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_posts")
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String authorId;
    private String authorName;
    private String authorImageUrl;
    private String type;
    private String category;
    private String title;
    private String content;
    private String location;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    private Integer likeCount = 0;
    private Integer commentCount = 0;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String offer;
    private String want;

    // Getters and Setters
}