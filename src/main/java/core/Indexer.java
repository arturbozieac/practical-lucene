package core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Indexer {

  private final String indexPath;
  Directory indexDirectory;

  public Indexer(String indexDirectoryPath) throws IOException {
    this.indexPath = indexDirectoryPath;
    this.indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

  }
  
  public void index(List<File> files) {
    IndexWriter indexWriter = null;
    try {
      indexWriter = getIndexWriter(indexPath);
      for (File file : files) {
        doIndexing(file, indexWriter);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if(indexWriter != null) {
        try {
          indexWriter.close();
        } catch (IOException e) {
          System.out.println("Cannot close writer.");
        }
      }
    }
  }
  
  public List<Document> searchFiles(String inField, String queryString) throws IOException, ParseException {
    Query query = new QueryParser(inField, new StandardAnalyzer()).parse(queryString);
    IndexReader indexReader = DirectoryReader.open(indexDirectory);
    IndexSearcher searcher = new IndexSearcher(indexReader);
    TopDocs topDocs = searcher.search(query, 1);
    System.out.println(topDocs.totalHits);
    return Stream.of(topDocs.scoreDocs)
        .map(scoreDoc -> {
          Document doc = null;
          try {
            doc = searcher.doc(scoreDoc.doc);
          } catch (IOException e) {
            e.printStackTrace();
          }
          return doc;
        })
        .collect(Collectors.toList());
}
  
  public IndexWriter getIndexWriter(String path) throws IOException {
    IndexWriter indexWriter;
    
    //Create the indexer
    indexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(new StandardAnalyzer()));
    
    return indexWriter;
  }
  
  private void doIndexing(File file, IndexWriter indexWriter) throws IOException {
    //Check that the indexWriter is not null and is open
    if (indexWriter == null) {
      throw new IllegalArgumentException("IndexWriter instance must not be null");
    }
    if (!indexWriter.isOpen()) {
      throw new IllegalArgumentException("IndexWriter instance must be open");
    }
    //Get the document using the method we wrote earlier
    Document document = parseAndCreateDocument(file);
    indexWriter.addDocument(document);
  }

  private Document parseAndCreateDocument(File file) throws IOException {
    Document document = new Document();
    
    FileReader fileReader = new FileReader(file);
    document.add(
      new TextField("contents", fileReader));
    document.add(
      new StringField("path", file.getPath(), Field.Store.YES));
    document.add(
      new StringField("filename", file.getName(), Field.Store.YES));
    
    return document;
  }
  
  public void clearIndex() throws IOException {
      Path pathToBeDeleted = Paths.get(indexPath);

      Files.walk(pathToBeDeleted)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);

      indexDirectory = FSDirectory.open(pathToBeDeleted);  
  }
}
