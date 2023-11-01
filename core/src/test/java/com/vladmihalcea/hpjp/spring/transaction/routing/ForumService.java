package com.vladmihalcea.hpjp.spring.transaction.routing;

import com.vladmihalcea.hpjp.hibernate.transaction.forum.Post;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Vlad Mihalcea
 */
@Service
public interface ForumService {

  Post newPost(String title, String... tags);

  List<Post> findAllPostsByTitle(String title);
}
