package com.hyeyuns2.kidtale.story.entity;

import com.hyeyuns2.kidtale.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(nullable = false, length = 50)
    private String childName;

    @Column(nullable = false)
    private int childAge;

    @Column(nullable = false, length = 100)
    private String theme;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String pagesJson;

    @Column(length = 100)
    private String sweetBookId;

    @Column(length = 100)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoryStatus status;

    public static Story create(Long userId, String childName, int childAge, String theme, String title, String pagesJson) {
        Story story = new Story();
        story.userId = userId;
        story.childName = childName;
        story.childAge = childAge;
        story.theme = theme;
        story.title = title;
        story.pagesJson = pagesJson;
        story.status = StoryStatus.DRAFT;
        return story;
    }

    public void updateStatusToBookCreated(String sweetBookId) {
        this.sweetBookId = sweetBookId;
        this.status = StoryStatus.BOOK_CREATED;
    }

    public void updateStatusToOrdered(String orderId) {
        this.orderId = orderId;
        this.status = StoryStatus.ORDERED;
    }
}
