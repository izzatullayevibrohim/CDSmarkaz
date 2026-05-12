package org.example.cdsmarkaztelegrambot.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scheduled_feedbacks")
public class ScheduledFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_message_id")
    private Long checkMessageId;

    @Column(name = "user_chat_id")
    private String userChatId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Builder.Default
    private Boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private Integer rate;
    private String text;

    @Column(name = "unique_name")
    private String uniqueName;
}