package com.example.tools;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;
import java.util.*;

public class QuizDuplicateChecker {
    // Tính toán TF-IDF cho một câu hỏi
    private static Map<String, Float> calculateTFIDF(IndexReader reader, String text, Analyzer analyzer) throws IOException {
        Map<String, Integer> termFreq = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");

        // Tính TF (Term Frequency)
        for (String word : words) {
            termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
        }

        Map<String, Float> tfidf = new HashMap<>();
        int numDocs = reader.numDocs();
        ClassicSimilarity similarity = new ClassicSimilarity();

        for (String term : termFreq.keySet()) {
            int docFreq = reader.docFreq(new Term("content", term));
            float idf = similarity.idf(docFreq, numDocs);
            tfidf.put(term, termFreq.get(term) * idf);
        }

        return tfidf;
    }

    // Hàm tính toán Cosine Similarity giữa hai vector TF-IDF
    private static float cosineSimilarity(Map<String, Float> vec1, Map<String, Float> vec2) {
        Set<String> allWords = new HashSet<>(vec1.keySet());
        allWords.addAll(vec2.keySet());

        float dotProduct = 0;
        float normVec1 = 0;
        float normVec2 = 0;

        for (String word : allWords) {
            float v1 = vec1.getOrDefault(word, 0f);
            float v2 = vec2.getOrDefault(word, 0f);

            dotProduct += v1 * v2;
            normVec1 += v1 * v1;
            normVec2 += v2 * v2;
        }

        return normVec1 == 0 || normVec2 == 0 ? 0 : (dotProduct / (float) (Math.sqrt(normVec1) * Math.sqrt(normVec2)));
    }

    public static boolean checkDuplicateQuestion(List<String> questions, String newQuestion) throws IOException {
        // Tạo index TF-IDF
        RAMDirectory index = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        for (String question : questions) {
            Document doc = new Document();
            doc.add(new TextField("content", question, Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.close();

        // Tính toán TF-IDF
        IndexReader reader = DirectoryReader.open(index);
        Map<String, Float> tfidfNewQuestion = calculateTFIDF(reader, newQuestion, analyzer);

        float maxSimilarity = 0;
        String mostSimilarQuestion = null;

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Float> tfidfExisting = calculateTFIDF(reader, questions.get(i), analyzer);
            float similarity = cosineSimilarity(tfidfNewQuestion, tfidfExisting);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilarQuestion = questions.get(i);
            }
        }

        reader.close();

        // Kiểm tra nếu câu hỏi trùng lặp (ngưỡng 0.8)
        if (maxSimilarity >= 0.8) {
            return true;
        } else {
            return false;
        }
    }

    // ✅ Hàm mới: Kiểm tra độ tương đồng giữa 2 câu hỏi
    public static float checkSimilarityBetweenTwoQuestions(String question1, String question2) throws IOException {
        RAMDirectory index = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        Document doc1 = new Document();
        doc1.add(new TextField("content", question1, Field.Store.YES));
        writer.addDocument(doc1);

        Document doc2 = new Document();
        doc2.add(new TextField("content", question2, Field.Store.YES));
        writer.addDocument(doc2);

        writer.close();

        IndexReader reader = DirectoryReader.open(index);
        Map<String, Float> tfidf1 = calculateTFIDF(reader, question1, analyzer);
        Map<String, Float> tfidf2 = calculateTFIDF(reader, question2, analyzer);

        float similarity = cosineSimilarity(tfidf1, tfidf2);
        reader.close();

        return similarity;
    }

    /*public static void main(String[] args) throws IOException {
        String q1 = "Java là gì?";
        String q2 = "Bạn có thể giải thích Java là gì không?";
        float similarity = checkSimilarityBetweenTwoQuestions(q1, q2);
        System.out.println("Độ tương đồng: " + similarity);
    }*/
}
