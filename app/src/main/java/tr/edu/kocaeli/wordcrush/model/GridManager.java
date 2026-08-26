package tr.edu.kocaeli.wordcrush.model;
import java.util.*;
public class GridManager {
    private Hucre[][] grid;
    private int boyut;
    private Trie sozluk;
    private Random random;
    private Map<Character, Integer> harfHavuzu;
    private List<String> yerlestirilenKelimeler;
    private List<String> kalanTohumKelimeler;
    private Set<String> ekstraKelimelerTorbasi;
    private final Map<Character, Integer> harfPuanlari;
    private Set<String> bulunanKelimelerHafizasi;
    private final NavigableMap<Double, Character> harfFrekansHafizasi = new TreeMap<>();
    private double toplamFrekansAgirligi = 0;
    private final Locale TR_LOCALE = new Locale("tr", "TR");
    public GridManager(int boyut, Trie sozluk) {
        this.boyut = boyut;
        this.grid = new Hucre[boyut][boyut];
        this.sozluk = sozluk;
        this.random = new Random();
        this.harfHavuzu = new HashMap<>();
        this.yerlestirilenKelimeler = new ArrayList<>();
        this.kalanTohumKelimeler = new ArrayList<>();
        this.ekstraKelimelerTorbasi = new HashSet<>();
        this.harfPuanlari = new HashMap<>();
        this.bulunanKelimelerHafizasi = new HashSet<>();

        puanTablosunuHazirla();
        frekansTablosunuHazirla();
        tahtayiOlustur();
    }

    private void frekansTablosunuHazirla() {
        frekansEkle(Arrays.asList('A', 'E', 'İ', 'L', 'R', 'N'), 10.0);
        frekansEkle(Arrays.asList('K', 'M', 'T', 'S', 'Y', 'D'), 5.0);
        frekansEkle(Arrays.asList('J', 'Ğ', 'F', 'V'), 1.5);
        List<Character> digerleri = Arrays.asList('B','C','Ç','G','H','I','O','Ö','P','Ş','U','Ü','Z');
        frekansEkle(digerleri, 2.0);
    }
    private void frekansEkle(List<Character> harfler, double agirlik) {
        for (char h : harfler) {
            toplamFrekansAgirligi += agirlik;
            harfFrekansHafizasi.put(toplamFrekansAgirligi, h);
        }
    }

    private char rastgeleHarfSec() {
        if (toplamFrekansAgirligi <= 0) return 'A';
        double rastgeleDeger = random.nextDouble() * toplamFrekansAgirligi;
        return harfFrekansHafizasi.higherEntry(rastgeleDeger).getValue();
    }

    private void puanTablosunuHazirla() {
        Object[][] veriler = {
                {'A', 1}, {'B', 3}, {'C', 4}, {'Ç', 4}, {'D', 3}, {'E', 1}, {'F', 7}, {'G', 5},
                {'Ğ', 8}, {'H', 5}, {'I', 2}, {'İ', 1}, {'J', 10}, {'K', 1}, {'L', 1}, {'M', 2},
                {'N', 1}, {'O', 2}, {'Ö', 7}, {'P', 5}, {'R', 1}, {'S', 2}, {'Ş', 4}, {'T', 1},
                {'U', 2}, {'Ü', 3}, {'V', 7}, {'Y', 3}, {'Z', 4}
        };
        for (Object[] satir : veriler) {
            harfPuanlari.put((Character) satir[0], (Integer) satir[1]);
        }
    }
    public Map<String, Object> comboAnaliziYap(String anaKelime) {
        String temizKelime = anaKelime.toUpperCase(TR_LOCALE);
        Set<String> bulunanGecerliAltKelimeler = new HashSet<>();
        Map<String, Integer> comboDetaylari = new HashMap<>();

        int toplamComboPuani = 0;
        boolean anaKelimeTohumMu = false;

        if (kalanTohumKelimeler.contains(temizKelime)) {
            anaKelimeTohumMu = true;
            kalanTohumKelimeler.remove(temizKelime);
        } else {
            ekstraKelimelerTorbasi.add(temizKelime);
        }

        for (int i = 0; i < temizKelime.length(); i++) {
            for (int j = i + 3; j <= temizKelime.length(); j++) {
                String altKelime = temizKelime.substring(i, j);

                if (sozluk.kelimeMi(altKelime)) {
                    if (!altKelime.equals(temizKelime) && !bulunanGecerliAltKelimeler.contains(altKelime)) {
                        bulunanGecerliAltKelimeler.add(altKelime);
                        int altPuan = kelimePuaniHesapla(altKelime);
                        toplamComboPuani += altPuan;
                        comboDetaylari.put(altKelime, altPuan);

                        if (kalanTohumKelimeler.contains(altKelime)) {
                            kalanTohumKelimeler.remove(altKelime);
                        } else {
                            ekstraKelimelerTorbasi.add(altKelime);
                        }

                        bulunanKelimelerHafizasi.add(altKelime);
                    }
                }
            }
        }

        Map<String, Object> sonuc = new HashMap<>();
        sonuc.put("puan", toplamComboPuani);
        sonuc.put("sayi", bulunanGecerliAltKelimeler.size() + 1);
        sonuc.put("detaylar", comboDetaylari);
        sonuc.put("anaKelimeTohumMu", anaKelimeTohumMu);
        return sonuc;
    }

    public Set<int[]> patlayacakAlanlariHesapla(List<int[]> secilenKoordinatlar) {
        Set<String> islenmisKoorSeti = new HashSet<>();
        List<int[]> patlamaKuyrugu = new ArrayList<>(secilenKoordinatlar);
        Set<int[]> finalPatlayacaklar = new HashSet<>();

        int index = 0;
        while (index < patlamaKuyrugu.size()) {
            int[] koor = patlamaKuyrugu.get(index++);
            int r = koor[0]; int c = koor[1];
            String key = r + "," + c;

            if (islenmisKoorSeti.contains(key)) continue;
            islenmisKoorSeti.add(key);
            finalPatlayacaklar.add(koor);

            Hucre h = grid[r][c];
            if (h.ozelGucVarMi()) {
                Hucre.GucTipi guc = h.getOzelGucTipi();
                h.setOzelGucTipi(Hucre.GucTipi.YOK);
                List<int[]> etkiAlani = getEtkiAlani(r, c, guc);
                for (int[] ekKoor : etkiAlani) {
                    if (!islenmisKoorSeti.contains(ekKoor[0] + "," + ekKoor[1])) {
                        patlamaKuyrugu.add(ekKoor);
                    }
                }
            }
        }
        return finalPatlayacaklar;
    }

    private List<int[]> getEtkiAlani(int r, int c, Hucre.GucTipi guc) {
        List<int[]> alan = new ArrayList<>();
        switch (guc) {
            case SATIR_TEMIZLE: for (int j = 0; j < boyut; j++) alan.add(new int[]{r, j}); break;
            case SUTUN_TEMIZLE: for (int i = 0; i < boyut; i++) alan.add(new int[]{i, c}); break;
            case ALAN_PATLAT:
                for (int i = r - 1; i <= r + 1; i++)
                    for (int j = c - 1; j <= c + 1; j++)
                        if (gecerliKoor(i, j)) alan.add(new int[]{i, j});
                break;
            case MEGA_PATLAT:
                for (int i = r - 2; i <= r + 2; i++)
                    for (int j = c - 2; j <= c + 2; j++)
                        if (gecerliKoor(i, j)) alan.add(new int[]{i, j});
                break;
        }
        return alan;
    }

    public int[][] yercekimiUygula(Collection<int[]> patlayanlar, int[] sonHarfKoor, int uzunluk, char sonHarfChar) {
        boolean gucKazandi = (uzunluk >= 4 && sonHarfKoor != null);
        int[][] kaymaMiktarlari = new int[boyut][boyut];

        for (int[] k : patlayanlar) {
            if (gucKazandi && k[0] == sonHarfKoor[0] && k[1] == sonHarfKoor[1]) {
                grid[k[0]][k[1]].setHarf(sonHarfChar);
                grid[k[0]][k[1]].ozelGucAta(uzunluk);
                continue;
            }
            grid[k[0]][k[1]].setHarf(' ');
            grid[k[0]][k[1]].setOzelGucTipi(Hucre.GucTipi.YOK);
        }

        for (int j = 0; j < boyut; j++) {
            int bosluk = 0;
            for (int i = boyut - 1; i >= 0; i--) {
                if (grid[i][j].getHarf() == ' ') {
                    bosluk++;
                } else if (bosluk > 0) {
                    Hucre ust = grid[i][j];
                    Hucre alt = grid[i + bosluk][j];

                    alt.setHarf(ust.getHarf());
                    alt.setOzelGucTipi(ust.getOzelGucTipi());
                    
                    kaymaMiktarlari[i + bosluk][j] = bosluk;

                    ust.setHarf(' ');
                    ust.setOzelGucTipi(Hucre.GucTipi.YOK);
                }
            }
            for (int i = 0; i < bosluk; i++) {
                grid[i][j].setHarf(rastgeleHarfSec());
                grid[i][j].setOzelGucTipi(Hucre.GucTipi.YOK);
                kaymaMiktarlari[i][j] = 99;
            }
        }

        Iterator<String> iterator = kalanTohumKelimeler.iterator();
        while (iterator.hasNext()) {
            String tohum = iterator.next();
            if (!tahtadaKelimeVarMi(tohum)) {
                iterator.remove();
            }
        }
        return kaymaMiktarlari;
    }

    public boolean tahtadaKelimeVarMi(String kelime) {
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                if (grid[i][j].getHarf() == kelime.charAt(0)) {

                    if (dfsKontrol(i, j, kelime, 0, new boolean[boyut][boyut])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean dfsKontrol(int r, int c, String kelime, int index, boolean[][] visited) {
        if (index == kelime.length()) return true;
        if (r < 0 || r >= boyut || c < 0 || c >= boyut || visited[r][c] || grid[r][c].getHarf() != kelime.charAt(index)) {
            return false;
        }

        visited[r][c] = true;
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            if (dfsKontrol(r + dr[i], c + dc[i], kelime, index + 1, visited)) return true;
        }

        visited[r][c] = false;
        return false;
    }

    private boolean dfsSpesifikKelimeAra(int r, int c, String kelime, int idx, boolean[][] viz) {
        if (idx == kelime.length()) return true;

        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];

            if (gecerliKoor(nr, nc) && !viz[nr][nc] && grid[nr][nc].getHarf() == kelime.charAt(idx)) {
                viz[nr][nc] = true;
                if (dfsSpesifikKelimeAra(nr, nc, kelime, idx + 1, viz)) return true;
                viz[nr][nc] = false;
            }
        }
        return false;
    }

    public int patlamaEtkiPuaniniHesapla(Set<int[]> patlayanlar) {
        int toplam = 0;
        for (int[] p : patlayanlar) {
            toplam += getHarfPuani(grid[p[0]][p[1]].getHarf());
        }
        return toplam;
    }

    public int kelimePuaniHesapla(String kelime) {
        int toplam = 0;
        String temiz = kelime.toUpperCase(TR_LOCALE);
        for (char c : temiz.toCharArray()) {
            toplam += getHarfPuani(c);
        }
        return toplam;
    }

    public int getHarfPuani(char c) {
        return harfPuanlari.getOrDefault(Character.toUpperCase(c), 0);
    }

    public void gridiYenile() {
        bulunanKelimelerHafizasi.clear();
        yerlestirilenKelimeler.clear();
        kalanTohumKelimeler.clear();
        tahtayiOlustur();
    }

    public boolean tohumKelimelerTukendiMi() {
        return kalanTohumKelimeler.isEmpty();
    }

    public boolean hamleVarMi() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                if (dfsHamleAra(i, j, sb, new boolean[boyut][boyut])) return true;
            }
        }
        return false;
    }
    private boolean dfsHamleAra(int r, int c, StringBuilder mevcut, boolean[][] viz) {
        mevcut.append(grid[r][c].getHarf());
        String anlikKelime = mevcut.toString();

        if (!sozluk.baslangicVarMi(anlikKelime)) {
            mevcut.deleteCharAt(mevcut.length() - 1);
            return false;
        }
        if (anlikKelime.length() >= 3 && sozluk.kelimeMi(anlikKelime) && !bulunanKelimelerHafizasi.contains(anlikKelime)) {
            mevcut.deleteCharAt(mevcut.length() - 1);
            return true;
        }
        if (anlikKelime.length() >= 8) {
            mevcut.deleteCharAt(mevcut.length() - 1);
            return false;
        }

        viz[r][c] = true;
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1}, dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int i = 0; i < 8; i++) {
            int nr = r + dr[i], nc = c + dc[i];
            if (gecerliKoor(nr, nc) && !viz[nr][nc]) {
                if (dfsHamleAra(nr, nc, mevcut, viz)) {
                    return true;
                }
            }
        }
        viz[r][c] = false;
        mevcut.deleteCharAt(mevcut.length() - 1);
        return false;
    }
    private void tahtayiOlustur() {
        for (int i = 0; i < boyut; i++) for (int j = 0; j < boyut; j++) grid[i][j] = new Hucre(' ');
        harfHavuzunuDoldur(64);

        List<String> tumHavuz = new ArrayList<>(sozluk.getKelimeHavuzu());
        if (!tumHavuz.isEmpty()) {
            int havuzBoyutu = tumHavuz.size();
            int denemeSayisi = 0;
            int maksimumDeneme = 500;

            while (yerlestirilenKelimeler.size() < 12 && denemeSayisi < maksimumDeneme) {
                denemeSayisi++;
                String k = tumHavuz.get(random.nextInt(havuzBoyutu));

                if (k.length() < 3 || k.length() > 8) continue;
                if (yerlestirilenKelimeler.contains(k)) continue;

                if (havuzdaVarMi(k) && tohumKelimeYerlestir(k)) {
                    havuzdanHarfDus(k);
                    yerlestirilenKelimeler.add(k);
                    kalanTohumKelimeler.add(k);
                }
            }
        }
        kalanlariFrekanslaDoldur();
    }

    private void harfHavuzunuDoldur(int adet) {
        for (int i = 0; i < adet; i++) {
            char h = rastgeleHarfSec();
            harfHavuzu.put(h, harfHavuzu.getOrDefault(h, 0) + 1);
        }
    }

    private boolean havuzdaVarMi(String k) {
        Map<Character, Integer> kopya = new HashMap<>(harfHavuzu);
        for (char c : k.toUpperCase(TR_LOCALE).toCharArray()) {
            if (kopya.getOrDefault(c, 0) <= 0) return false;
            kopya.put(c, kopya.get(c) - 1);
        }
        return true;
    }

    private void havuzdanHarfDus(String k) {
        for (char c : k.toUpperCase(TR_LOCALE).toCharArray()) {
            if (harfHavuzu.containsKey(c)) harfHavuzu.put(c, harfHavuzu.get(c) - 1);
        }
    }
    private boolean tohumKelimeYerlestir(String k) {
        for (int d = 0; d < 30; d++) {
            int r = random.nextInt(boyut), c = random.nextInt(boyut);
            if (grid[r][c].getHarf() == ' ') {
                boolean[][] viz = new boolean[boyut][boyut];
                grid[r][c].setHarf(k.charAt(0));
                viz[r][c] = true;
                if (yolBulVeYerlestir(k, 1, r, c, viz)) return true;
                grid[r][c].setHarf(' ');
            }
        }
        return false;
    }

    private boolean yolBulVeYerlestir(String k, int idx, int r, int c, boolean[][] viz) {
        if (idx == k.length()) return true;
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1}, dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        List<Integer> l = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));
        Collections.shuffle(l);
        for (int i : l) {
            int nr = r + dr[i], nc = c + dc[i];
            if (gecerliKoor(nr, nc) && !viz[nr][nc] && grid[nr][nc].getHarf() == ' ') {
                grid[nr][nc].setHarf(k.charAt(idx));
                viz[nr][nc] = true;
                if (yolBulVeYerlestir(k, idx + 1, nr, nc, viz)) return true;
                grid[nr][nc].setHarf(' ');
                viz[nr][nc] = false;
            }
        }
        return false;
    }

    private void kalanlariFrekanslaDoldur() {
        for (int i = 0; i < boyut; i++)
            for (int j = 0; j < boyut; j++)
                if (grid[i][j].getHarf() == ' ') grid[i][j].setHarf(rastgeleHarfSec());
    }

    public void bulunanKelimeyiKaydet(String k) { bulunanKelimelerHafizasi.add(k.toUpperCase(TR_LOCALE)); }
    public boolean dahaOnceBulunduMu(String k) { return bulunanKelimelerHafizasi.contains(k.toUpperCase(TR_LOCALE)); }
    private boolean gecerliKoor(int r, int c) { return r >= 0 && r < boyut && c >= 0 && c < boyut; }

    public Hucre[][] getGrid() { return grid; }
    public List<String> getKalanTohumKelimeler() { return kalanTohumKelimeler; }
    public Set<int[]> jokerBalikUygula() {
        Set<int[]> patlayanlar = new HashSet<>();
        int silinecekAdet = boyut + 2;
        for (int i = 0; i < silinecekAdet; i++) {
            patlayanlar.add(new int[]{random.nextInt(boyut), random.nextInt(boyut)});
        }
        return patlayanlar;
    }
    public void jokerDegistirmeUygula(int r1, int c1, int r2, int c2) {
        Hucre temp = grid[r1][c1];
        grid[r1][c1] = grid[r2][c2];
        grid[r2][c2] = temp;
        tohumKelimeleriGuncelle();
    }
    private void tohumKelimeleriGuncelle() {
        List<String> yeniKalanlar = new ArrayList<>();

        for (String kelime : kalanTohumKelimeler) {
            if (tahtadaKelimeVarMi(kelime)) {
                yeniKalanlar.add(kelime);
            } else {
                System.out.println("⚠️ HEDEF BOZULDU: " + kelime);
            }
        }
        this.kalanTohumKelimeler = yeniKalanlar;
    }

    public void jokerKaristirmaUygula() {
        Map<Character, Integer> mevcutHarfHavuzu = new HashMap<>();
        Map<Character, List<Hucre.GucTipi>> ozelGucHavuzu = new HashMap<>();

        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                char c = grid[i][j].getHarf();
                Hucre.GucTipi guc = grid[i][j].getOzelGucTipi();

                mevcutHarfHavuzu.put(c, mevcutHarfHavuzu.getOrDefault(c, 0) + 1);
                ozelGucHavuzu.putIfAbsent(c, new ArrayList<>());
                ozelGucHavuzu.get(c).add(guc);
            }
        }
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                grid[i][j] = new Hucre(' ');
            }
        }
        bulunanKelimelerHafizasi.clear();
        yerlestirilenKelimeler.clear();
        kalanTohumKelimeler.clear();
        ekstraKelimelerTorbasi.clear();

        this.harfHavuzu = mevcutHarfHavuzu;
        List<String> tumHavuz = new ArrayList<>(sozluk.getKelimeHavuzu());
        if (!tumHavuz.isEmpty()) {
            int havuzBoyutu = tumHavuz.size();
            int denemeSayisi = 0;
            int maksimumDeneme = 1000;

            while (yerlestirilenKelimeler.size() < 12 && denemeSayisi < maksimumDeneme) {
                denemeSayisi++;
                String k = tumHavuz.get(random.nextInt(havuzBoyutu));

                if (k.length() < 3 || k.length() > 8) continue;
                if (yerlestirilenKelimeler.contains(k)) continue;

                if (havuzdaVarMi(k) && tohumKelimeYerlestir(k)) {
                    havuzdanHarfDus(k);
                    yerlestirilenKelimeler.add(k);
                    kalanTohumKelimeler.add(k);
                }
            }
        }
        List<Character> artanHarfler = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : harfHavuzu.entrySet()) {
            for (int k = 0; k < entry.getValue(); k++) {
                artanHarfler.add(entry.getKey());
            }
        }

        Collections.shuffle(artanHarfler, random);

        int artanIndex = 0;
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                if (grid[i][j].getHarf() == ' ') {
                    if (artanIndex < artanHarfler.size()) {
                        grid[i][j].setHarf(artanHarfler.get(artanIndex++));
                    } else {
                        grid[i][j].setHarf(rastgeleHarfSec());
                    }
                }
            }
        }
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                char c = grid[i][j].getHarf();
                List<Hucre.GucTipi> gucler = ozelGucHavuzu.get(c);
                if (gucler != null && !gucler.isEmpty()) {
                    Hucre.GucTipi atanacakGuc = gucler.remove(0);
                    grid[i][j].setOzelGucTipi(atanacakGuc);
                }
            }
        }
    }

    public Set<int[]> jokerPartiUygula() {
        Set<int[]> patlayanlar = new HashSet<>();
        for (int i = 0; i < boyut; i++) {
            for (int j = 0; j < boyut; j++) {
                patlayanlar.add(new int[]{i, j});
            }
        }
        return patlayanlar;
    }
}