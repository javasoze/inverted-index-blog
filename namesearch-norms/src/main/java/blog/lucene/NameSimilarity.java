package blog.lucene;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;

public class NameSimilarity extends DefaultSimilarity {

  @Override
  public float idf(long docFreq, long numDocs) {
    return 1.0f;
  }

  @Override
  public float lengthNorm(FieldInvertState invertState) {
    return invertState.getUniqueTermCount();
  }

  @Override
  public float tf(float freq) {
    return 1.0f;
  }

}
