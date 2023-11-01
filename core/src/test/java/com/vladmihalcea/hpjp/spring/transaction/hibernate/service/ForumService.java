package com.vladmihalcea.hpjp.spring.transaction.hibernate.service;

import com.vladmihalcea.hpjp.hibernate.forum.dto.PostDTO;
import com.vladmihalcea.hpjp.hibernate.transaction.forum.Post;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Vlad Mihalcea
 */
@Service
public interface ForumService {

  Post newPost(String title, String... tags);

  List<Post> findAllByTitle(String title);

  Post findById(Long id);

  PostDTO getPostDTOById(Long id);

  void processData();
}
