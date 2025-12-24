package com.hoaithi.comment_service.repository;

import com.hoaithi.comment_service.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, String> {
}
