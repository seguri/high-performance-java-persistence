package com.vladmihalcea.hpjp.hibernate.query.dto.projection.transformer;

import java.util.List;
import org.hibernate.query.ResultListTransformer;

/**
 * @author Vlad Mihalcea
 */
public class DistinctListTransformer implements ResultListTransformer {

  public static final DistinctListTransformer INSTANCE = new DistinctListTransformer();

  @Override
  public List<PostDTO> transformList(List collection) {
    return collection.stream().distinct().toList();
  }
}
