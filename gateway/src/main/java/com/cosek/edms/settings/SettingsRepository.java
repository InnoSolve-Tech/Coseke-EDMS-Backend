package com.cosek.edms.settings;

import com.cosek.edms.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
     Optional<Settings> findByUser(User user);
}
