package tr.edu.kocaeli.wordcrush.model;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
public class Trie {
    private final TrieNode kok;
    private final List<String> kelimeHavuzu;

    public Trie() {
        this.kok = new TrieNode();
        this.kelimeHavuzu = new ArrayList<>();
    }
    public void ekle(String kelime) {
        TrieNode aktif = kok;
        kelime = kelime.toUpperCase();

        for (char c : kelime.toCharArray()) {
            aktif.getCocuklar().putIfAbsent(c, new TrieNode());
            aktif = aktif.getCocuklar().get(c);
        }
        if (!aktif.isKelimeSonuMu()) {
            aktif.setKelimeSonuMu(true);
            kelimeHavuzu.add(kelime);
        }
    }
    public boolean kelimeMi(String kelime) {
        TrieNode dugum = dugumBul(kelime.toUpperCase());
        return dugum != null && dugum.isKelimeSonuMu();
    }
    public boolean baslangicVarMi(String onek) {
        return dugumBul(onek.toUpperCase()) != null;
    }
    private TrieNode dugumBul(String metin) {
        TrieNode aktif = kok;
        for (char c : metin.toCharArray()) {
            aktif = aktif.getCocuklar().get(c);
            if (aktif == null) return null;
        }
        return aktif;
    }
    public List<String> getKelimeHavuzu() {
        return kelimeHavuzu;
    }
    public void streamDenYukle(java.io.InputStream is) {
        try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(is))) {
            String satir;
            while ((satir = br.readLine()) != null) {
                ekle(satir.trim());
            }
        } catch (IOException e) {
            System.err.println("Stream yükleme hatası: " + e.getMessage());
        }
    }
}