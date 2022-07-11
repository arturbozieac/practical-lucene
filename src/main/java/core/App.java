package core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

public class App {
  public static void main(String[] args) throws IOException {
    Indexer indexer = new Indexer("./src/main/resources/index");
    indexer.index(Arrays.asList(new File("./src/main/resources/index.html")));
    
    try {
      List<Document> searchFiles = indexer.searchFiles("contents", "Search");
      for(Document doc : searchFiles) {
        System.out.println("File name : " + doc.get("filename"));
        System.out.println("File path : " + doc.get("path"));
      }
     } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    indexer.clearIndex();
  }
}
