package com.SkillSwap.repository;

import com.SkillSwap.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing CommunityPost entities.
 */
@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, String> {

    /**
     * Find all posts by type, ordered by most recent timestamp.
     * @param type the type of post (e.g., "offer", "request")
     * @return list of posts matching the type, sorted by timestamp descending
     */
    List<CommunityPost> findByTypeOrderByTimestampDesc(String type);

    /**
     * Find posts by type and category, ordered by most recent timestamp.
     * @param type the type of post
     * @param category the category (e.g., "tech", "language", etc.)
     * @return list of posts matching type and category, sorted by timestamp descending
     */
    List<CommunityPost> findByTypeAndCategoryOrderByTimestampDesc(String type, String category);

    /**
     * Optional: Find posts by user ID, if posts are associated with users.
     * Uncomment if CommunityPost has a userId field or a User relation.
     */
    // List<CommunityPost> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Optional: Search posts by keyword in title or description.
     * Requires @Query or full-text index if needed.
     */
    // @Query("SELECT p FROM CommunityPost p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.timestamp DESC")
    // List<CommunityPost> searchByKeyword(@Param("keyword") String keyword);
}
