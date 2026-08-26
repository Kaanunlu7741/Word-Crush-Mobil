package tr.edu.kocaeli.wordcrush.model;
import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private Map<Character, TrieNode> cocuklar;
    private boolean kelimeSonuMu;
    public TrieNode() {
        this.cocuklar = new HashMap<>();
        this.kelimeSonuMu = false;
    }
    public Map<Character, TrieNode> getCocuklar() {
        return cocuklar;
    }

    public boolean isKelimeSonuMu() {
        return kelimeSonuMu;
    }

    public void setKelimeSonuMu(boolean kelimeSonuMu) {
        this.kelimeSonuMu = kelimeSonuMu;
    }
}