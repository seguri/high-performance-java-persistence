package com.vladmihalcea.hpjp.spring.data.recursive.repository;

import com.vladmihalcea.hpjp.spring.data.recursive.domain.PostCommentDTO;
import java.util.List;
import org.springframework.data.repository.query.Param;

/**
 * @author Vlad Mihalcea
 */
public interface CustomPostRepository {

  List<PostCommentDTO> findTopCommentDTOsByPost(@Param("postId") Long postId, int ranking);
}
