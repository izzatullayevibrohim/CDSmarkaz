package org.example.cdsmarkaztelegrambot.repositories;

import org.example.cdsmarkaztelegrambot.models.CheckMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckMessageRepository extends JpaRepository<CheckMessage, Long> {}