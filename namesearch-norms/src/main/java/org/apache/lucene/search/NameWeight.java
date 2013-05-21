package org.apache.lucene.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.util.Bits;


public class NameWeight extends Weight {
  private final NameQuery sq;
  public NameWeight(NameQuery sq) {
    this.sq = sq;
  }

  @Override
  public Explanation explain(AtomicReaderContext context, int doc)
      throws IOException {
    
    Scorer scorer = scorer(context, true, context.isTopLevel, context.reader().getLiveDocs());
    scorer.advance(doc);
    float score = scorer.score();
    
    Explanation expl = new Explanation(score, "name query score");
    
    if (scorer instanceof DisjunctionTermMatchedScorer) {
      DisjunctionTermMatchedScorer distScorer = (DisjunctionTermMatchedScorer)scorer;
    
      float numUniques = distScorer.getNumUniqTerms(doc);
      
      expl.addDetail(new Explanation(numUniques, "number of unique terms"));
      expl.addDetail(new Explanation(distScorer.nrMatchers, "number of terms matched"));
      expl.addDetail(new Explanation(distScorer.numTerms, "number of query terms"));
      float matchRatio = distScorer.calculateMatchedRatio(numUniques);
      expl.addDetail(new Explanation(matchRatio, "match ratio"));
    }
    return expl;
  }

  @Override
  public Query getQuery() {
    return sq;
  }

  @Override
  public float getValueForNormalization() throws IOException {
    return 0;
  }

  @Override
  public void normalize(float norm, float topLevelBoost) {
    
  }

  
  @Override
  public Scorer scorer(AtomicReaderContext context, boolean scoreDocsInOrder,
      boolean topScorer, Bits acceptDocs) throws IOException {
    List<Scorer> scorerList = DisjunctionTermMatchedScorer.makeScorers(this,  context.reader(), sq.field, sq.terms);
    
    if (scorerList.isEmpty()) return null;
    
    if (scorerList.size() > 1) {
      return new DisjunctionTermMatchedScorer(this, scorerList, context.reader(), sq.field, sq.terms);
    }
    
    return scorerList.get(0);
  }

}
