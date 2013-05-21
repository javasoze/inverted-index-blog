package org.apache.lucene.search;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;


public class NameQuery extends Query {
  final String field;
  final Set<String> terms;
  
  public NameQuery(String field, Set<String> terms) {
    this.field = field;
    this.terms = terms;
  }
  
  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return new NameWeight(this);
  }

  @Override
  public String toString(String field) {
    return field+":"+terms;
  }
}
