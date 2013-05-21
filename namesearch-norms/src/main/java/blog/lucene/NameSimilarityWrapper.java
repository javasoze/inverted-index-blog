package blog.lucene;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;

public class NameSimilarityWrapper extends PerFieldSimilarityWrapper {


  static final Similarity defaultSim = new DefaultSimilarity();
  static final Similarity nameSim = new NameSimilarity();
  
  private final String nameField;
  public NameSimilarityWrapper(String nameField) {
    this.nameField = nameField;
  }
  
  @Override
  public Similarity get(String name) {
    if (nameField.equals(name)) return nameSim;
    return defaultSim;
  }
  
}