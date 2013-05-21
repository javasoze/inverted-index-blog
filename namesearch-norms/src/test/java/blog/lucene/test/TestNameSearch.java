package blog.lucene.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NameQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import blog.lucene.NameSimilarityWrapper;

public class TestNameSearch {
  
  static final String NAME_FIELD = "name";
  
  static Directory createIndex(String[] names) throws Exception {
    RAMDirectory ramdir = new RAMDirectory();
    
    IndexWriterConfig writerCfg = new IndexWriterConfig(Version.LUCENE_41, new StandardAnalyzer(Version.LUCENE_41));
    writerCfg.setSimilarity(new NameSimilarityWrapper(NAME_FIELD));
    
    IndexWriter writer = new IndexWriter(ramdir, writerCfg);
    
    for (String name : names) {
      Document doc = new Document();
      Field f = new TextField(NAME_FIELD, name, Store.NO);
      doc.add(f);
      writer.addDocument(doc);
    }
    
    writer.commit();
    writer.close();
    
    return ramdir;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception{
    String[] names = new String[]{
      "john paul",
      "john paul the second",
      "john john john",
      "paul paul",
      "pope john paul",
      "pope john paul the second"
    };

    Directory dir = createIndex(names);
    
    DirectoryReader reader = DirectoryReader.open(dir);
    
    IndexSearcher searcher = new IndexSearcher(reader);
    
    BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
    while(true) {
      System.out.print("> ");
      String line = lineReader.readLine();
      if ("exit".equals(line)) break;
      
      String[] terms = line.split("\\s");
      
      Query nameQuery = new NameQuery(NAME_FIELD, new HashSet<String>(Arrays.asList(terms)));
      TopDocs topdocs = searcher.search(nameQuery, 10);
      
      ScoreDoc[] sd = topdocs.scoreDocs;
      
      for (ScoreDoc d : sd) {
        System.out.println(names[ d.doc]+", "+d.doc+": "+d.score);
        System.out.println(searcher.explain(nameQuery, d.doc));
      }  
    }
    
    reader.close();
  }
}
