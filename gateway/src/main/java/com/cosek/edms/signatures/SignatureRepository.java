package com.cosek.edms.signatures;

import com.cosek.edms.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    Optional<List<Signature>> findByUser(User user);
}
