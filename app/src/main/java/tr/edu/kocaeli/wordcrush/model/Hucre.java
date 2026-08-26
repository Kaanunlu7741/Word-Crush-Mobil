package tr.edu.kocaeli.wordcrush.model;

public class Hucre {
    private char harf;
    private int puan;
    private boolean kullanimda;
    private boolean secildiMi;
    public enum GucTipi {
        YOK,
        SATIR_TEMIZLE,
        ALAN_PATLAT,
        SUTUN_TEMIZLE,
        MEGA_PATLAT
    }
    private GucTipi ozelGucTipi;
    public Hucre(char harf) {
        this.harf = Character.toUpperCase(harf);
        this.puan = puanHesapla(this.harf);
        this.kullanimda = false;
        this.secildiMi = false;
        this.ozelGucTipi = GucTipi.YOK;
    }

    private int puanHesapla(char c) {
        switch (c) {
            case 'A': case 'E': case 'İ': case 'K': case 'L': case 'N': case 'R': case 'T':
                return 1;
            case 'I': case 'M': case 'O': case 'S': case 'U':
                return 2;
            case 'B': case 'D': case 'Ü': case 'Y':
                return 3;
            case 'C': case 'Ç': case 'Ş': case 'Z':
                return 4;
            case 'G': case 'H': case 'P':
                return 5;
            case 'F': case 'Ö': case 'V':
                return 7;
            case 'Ğ':
                return 8;
            case 'J':
                return 10;
            default:
                return 0;
        }
    }
    public String getGucSimgesi() {
        switch (ozelGucTipi) {
            case SATIR_TEMIZLE: return " ↔";
            case ALAN_PATLAT:   return " ✹";
            case SUTUN_TEMIZLE: return " ↕";
            case MEGA_PATLAT:   return " ✪";
            default: return "";
        }
    }
    public void ozelGucAta(int kelimeUzunlugu) {
        if (kelimeUzunlugu == 4) ozelGucTipi = GucTipi.SATIR_TEMIZLE;
        else if (kelimeUzunlugu == 5) ozelGucTipi = GucTipi.ALAN_PATLAT;
        else if (kelimeUzunlugu == 6) ozelGucTipi = GucTipi.SUTUN_TEMIZLE;
        else if (kelimeUzunlugu >= 7) ozelGucTipi = GucTipi.MEGA_PATLAT;
        else ozelGucTipi = GucTipi.YOK;
    }

    public boolean ozelGucVarMi() {
        return ozelGucTipi != GucTipi.YOK;
    }
    public char getHarf() { return harf; }
    public void setHarf(char harf) {
        this.harf = harf;
        this.puan = puanHesapla(harf);
    }

    public int getPuan() { return puan; }
    public GucTipi getOzelGucTipi() { return ozelGucTipi; }
    public void setOzelGucTipi(GucTipi guc) { this.ozelGucTipi = guc; }
}