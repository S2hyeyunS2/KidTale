package com.hyeyuns2.kidtale.story.repository;

import com.hyeyuns2.kidtale.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
}
