package com.hyeyuns2.kidtale.story.repository;

import com.hyeyuns2.kidtale.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    List<Story> findTop10ByOrderByCreatedAtDesc();

    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);
}
