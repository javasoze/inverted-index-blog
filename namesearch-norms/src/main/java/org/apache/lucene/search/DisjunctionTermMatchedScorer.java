package org.apache.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.DocValues.Source;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.SmallFloat;


public class DisjunctionTermMatchedScorer extends DisjunctionSumScorer {

  final Source normSource;
  
  final int numTerms;
  
  /** Cache of decoded bytes. */
  private static final float[] NORM_TABLE = new float[256];

  static {
    for (int i = 0; i < 256; i++)
      NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
  }

  /** Decodes a normalization factor stored in an index.
   * @see #encodeNormValue(float)
   */
  public static float decodeNormValue(byte b) {
    return NORM_TABLE[b & 0xFF];  // & 0xFF maps negative bytes to positive above 127
  }
    
  public static List<Scorer> makeScorers(Weight weight, AtomicReader reader, String field, Set<String> terms) throws IOException {
    
    String[] termStrings = terms.toArray(new String[0]);
    List<Scorer> scorerList = new ArrayList<Scorer>();
    for (int i = 0; i < termStrings.length; ++i) {
      
      final DocsEnum denum = reader.termDocsEnum(new Term(field, termStrings[i]));
      if (denum == null) continue;
      scorerList.add(new Scorer(weight){

        @Override
        public float score() throws IOException {
          assert docID() != NO_MORE_DOCS;
          return 1.0f;
        }

        @Override
        public int freq() throws IOException {
          return denum.freq();
        }

        @Override
        public int docID() {
          return denum.docID();
        }

        @Override
        public int nextDoc() throws IOException {
          return denum.nextDoc();
        }

        @Override
        public int advance(int target) throws IOException {
          return denum.advance(target);
        }
        
      });
    }
    
    return scorerList;
  }
  
  float getNumUniqTerms(int docid) {
    return decodeNormValue((byte)normSource.getInt(docid));
  }
  
  float calculateMatchedRatio(float numUniques) {
    float matchRatio;
    if (numTerms > numUniques) {
      matchRatio = numUniques / (float)numTerms;
    }
    else {
      matchRatio = (float)nrMatchers / numUniques;
    }
    return matchRatio;
  }
  
  public DisjunctionTermMatchedScorer(Weight weight, List<Scorer> scorerList, AtomicReader reader, String field, Set<String> terms) throws IOException {
    super(weight, scorerList, 1);
    DocValues norms = reader.normValues(field);
    normSource = norms.getSource();
    numTerms = terms.size();
  }

  @Override
  public float score() throws IOException {
    float numUniques = getNumUniqTerms(docID());
    float matchRatio = calculateMatchedRatio(numUniques);
    return matchRatio * 0.8f + 0.2f*(float)numUniques;
  }
}
