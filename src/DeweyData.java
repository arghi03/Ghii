import java.util.LinkedHashMap;
import java.util.Map;

public class DeweyData {

    /**
     * Mengembalikan data utama Klasifikasi Desimal Dewey (DDC)
     * dalam bentuk Peta Bersarang (Nested Map).
     * Key luar: Kategori utama (misal: "000 - Karya Umum")
     * Key dalam: Kode sub-kategori (misal: "010")
     * Value dalam: Nama sub-kategori (misal: "Bibliografi")
     */
    public static Map<String, Map<String, String>> getDdcMap() {
        Map<String, Map<String, String>> ddcMap = new LinkedHashMap<>();

        ddcMap.put("000 - Karya Umum", mapOf(
                "000", "Karya umum",
                "010", "Bibliografi",
                "020", "Ilmu perpustakaan & informasi",
                "030", "Ensiklopedia",
                "050", "Majalah & serial",
                "060", "Organisasi",
                "070", "Media & jurnalisme",
                "080", "Kutipan",
                "090", "Naskah & buku langka"
        ));

        ddcMap.put("100 - Filsafat & Psikologi", mapOf(
                "100", "Filsafat & psikologi",
                "110", "Metafisika",
                "120", "Epistemologi",
                "130", "Parapsikologi & okultisme",
                "140", "Aliran filsafat",
                "150", "Psikologi",
                "160", "Logika",
                "170", "Etika",
                "180", "Filsafat kuno & timur",
                "190", "Filsafat modern"
        ));

        ddcMap.put("200 - Agama", mapOf(
                "200", "Agama",
                "210", "Filsafat agama",
                "220", "Alkitab",
                "230", "Kekristenan",
                "240", "Moral & devosi Kristen",
                "250", "Pelayanan gereja",
                "260", "Teologi sosial & gerejawi",
                "270", "Sejarah Kekristenan",
                "280", "Denominasi Kristen",
                "290", "Agama lain"
        ));

        ddcMap.put("300 - Ilmu Sosial", mapOf(
                "300", "Ilmu sosial",
                "310", "Statistik umum",
                "320", "Ilmu politik",
                "330", "Ekonomi",
                "340", "Hukum",
                "350", "Administrasi publik & militer",
                "360", "Masalah & layanan sosial",
                "370", "Pendidikan",
                "380", "Perdagangan, komunikasi & transportasi",
                "390", "Adat, etiket & cerita rakyat"
        ));

        ddcMap.put("400 - Bahasa", mapOf(
                "400", "Bahasa",
                "410", "Linguistik",
                "420", "Bahasa Inggris",
                "430", "Bahasa Jerman",
                "440", "Bahasa Perancis",
                "450", "Bahasa Italia",
                "460", "Bahasa Spanyol & Portugis",
                "470", "Bahasa Latin",
                "480", "Bahasa Yunani",
                "490", "Bahasa lainnya"
        ));

        ddcMap.put("500 - Ilmu Pengetahuan Alam dan Matematika", mapOf(
                "500", "Ilmu alam",
                "510", "Matematika",
                "520", "Astronomi",
                "530", "Fisika",
                "540", "Kimia",
                "550", "Ilmu bumi & geologi",
                "560", "Fosil & kehidupan prasejarah",
                "570", "Biologi",
                "580", "Botani",
                "590", "Zoologi"
        ));

        ddcMap.put("600 - Teknologi dan Ilmu-Ilmu Terapan", mapOf(
                "600", "Teknologi",
                "610", "Kedokteran & kesehatan",
                "620", "Rekayasa & teknik",
                "630", "Pertanian",
                "640", "Manajemen rumah tangga",
                "650", "Manajemen & hubungan masyarakat",
                "660", "Teknik kimia",
                "670", "Industri manufaktur",
                "680", "Produk jadi",
                "690", "Konstruksi bangunan"
        ));

        ddcMap.put("700 - Seni, Hiburan, dan Olahraga", mapOf(
                "700", "Seni & rekreasi",
                "710", "Tata kota & lansekap",
                "720", "Arsitektur",
                "730", "Seni patung",
                "740", "Seni dekoratif & desain",
                "750", "Lukisan",
                "760", "Seni grafis",
                "770", "Fotografi & seni komputer",
                "780", "Musik",
                "790", "Hiburan & olahraga"
        ));

        ddcMap.put("800 - Sastra", mapOf(
                "800", "Sastra umum",
                "810", "Sastra Amerika",
                "820", "Sastra Inggris",
                "830", "Sastra Jerman",
                "840", "Sastra Perancis",
                "850", "Sastra Italia",
                "860", "Sastra Spanyol & Portugis",
                "870", "Sastra Latin",
                "880", "Sastra Yunani",
                "890", "Sastra lainnya"
        ));

        ddcMap.put("900 - Geografi dan Sejarah", mapOf(
                "900", "Sejarah & geografi",
                "910", "Geografi & perjalanan",
                "920", "Biografi & silsilah",
                "930", "Sejarah dunia kuno",
                "940", "Sejarah Eropa",
                "950", "Sejarah Asia",
                "960", "Sejarah Afrika",
                "970", "Sejarah Amerika Utara",
                "980", "Sejarah Amerika Selatan",
                "990", "Wilayah lainnya"
        ));

        return ddcMap;
    }

    // Method helper untuk membuat Map dengan mudah
    private static Map<String, String> mapOf(String... entries) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
        return map;
    }
}