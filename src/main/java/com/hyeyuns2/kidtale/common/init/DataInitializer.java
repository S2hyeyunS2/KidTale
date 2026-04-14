package com.hyeyuns2.kidtale.common.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeyuns2.kidtale.story.entity.Story;
import com.hyeyuns2.kidtale.story.repository.StoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    private static final String STORIES_JSON_PATH = "dummy-data/stories.json";

    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(StoryRepository storyRepository, ObjectMapper objectMapper) {
        this.storyRepository = storyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (storyRepository.count() > 0) {
            log.debug("[DataInitializer] 이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("[DataInitializer] 더미 동화 데이터를 삽입합니다.");
        List<DummyStoryData> dummyList = loadDummyData();

        List<Story> stories = dummyList.stream()
                .map(this::toStory)
                .toList();

        storyRepository.saveAll(stories);
        log.info("[DataInitializer] 더미 동화 {}편 삽입 완료.", stories.size());
    }

    private List<DummyStoryData> loadDummyData() throws Exception {
        ClassPathResource resource = new ClassPathResource(STORIES_JSON_PATH);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<DummyStoryData>>() {});
        }
    }

    private Story toStory(DummyStoryData data) {
        try {
            String pagesJson = objectMapper.writeValueAsString(data.pages());
            return Story.create(null, data.childName(), data.childAge(), data.theme(), data.title(), pagesJson);
        } catch (Exception e) {
            log.error("[DataInitializer] 더미 스토리 변환 실패. title={}", data.title(), e);
            throw new RuntimeException("더미 데이터 변환 실패: " + data.title(), e);
        }
    }
}
