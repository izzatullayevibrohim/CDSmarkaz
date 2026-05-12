    package org.example.cdsmarkaztelegrambot.models;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    @Table(name = "roles")
    public class Role {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @Column(name = "user_id")
        private Long userId;
    }
