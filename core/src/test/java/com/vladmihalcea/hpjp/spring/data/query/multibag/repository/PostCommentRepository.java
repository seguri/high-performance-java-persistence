package com.vladmihalcea.hpjp.spring.data.query.multibag.repository;

import com.vladmihalcea.hpjp.spring.data.query.multibag.domain.PostComment;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface PostCommentRepository extends BaseJpaRepository<PostComment, Long> {

  List<PostComment> findAllByReview(String review);
}
