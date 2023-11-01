package com.vladmihalcea.hpjp.spring.blaze.repository;

import com.blazebit.persistence.PagedList;
import com.vladmihalcea.hpjp.spring.blaze.domain.Post;
import com.vladmihalcea.hpjp.spring.blaze.domain.views.PostWithCommentsAndTagsView;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 * @author Vlad Mihalcea
 */
public interface CustomPostRepository {

  PagedList<Post> findTopN(Sort sortBy, int pageSize);

  PagedList<Post> findNextN(Sort sortBy, PagedList<Post> previousPage);

  List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId);

  List<PostWithCommentsAndTagsView> findPostWithCommentsAndTagsViewByIds(Long minId, Long maxId);
}
