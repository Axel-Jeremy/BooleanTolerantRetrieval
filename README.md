# Boolean & Tolerant Information Retrieval System

Sistem *Information Retrieval* ini dibangun menggunakan bahasa pemrograman **Java**. Proyek ini mengimplementasikan model pencarian teks klasik yang menggabungkan kemampuan **Boolean Retrieval** (pencarian dengan operator logika) dan **Tolerant Retrieval** (pencarian yang mentoleransi variasi kueri).

Proyek ini dibuat untuk memenuhi tugas mata kuliah *Information Retrieval* sarjana Informatika di Universitas Katolik Parahyangan.


## Cara Run (Gunakan CMD atau PowerShell)
 
```bash
cd SearchEngine
javac *.java
java Main
```


## Fitur Utama

Sistem ini terdiri dari beberapa modul utama:

1. **Document Reader:** Modul untuk membaca dan mengekstraksi teks dari kumpulan dokumen/korpus.
2. **Text Preprocessing:**
   - **Tokenizer:** Memecah teks utuh menjadi token-token (kata) tunggal.
   - **Stemmer:** Mengembalikan kata ke bentuk dasarnya (stemming) menggunakan algoritma Porter Stemmer.
3. **Inverted Index:** Struktur data utama yang memetakan setiap term (kata) ke daftar dokumen (postings list) yang memuatnya.
4. **Query Handling Logic (Boolean Model):** Memproses kueri pengguna yang menggunakan operator logika boolean (`AND`, `OR`, `NOT`) dan melakukan operasi irisan (intersection) atau gabungan (union) juga negasi pada *postings list*.
5. **Tolerant Model:** Menangani kueri tidak baku / typo dengan koreksi ejaan (*spelling correction*) berdasarkan edit distance.


## Contoh Penggunaan

Setelah program berjalan, sistem akan memproses dokumen terlebih dahulu (membangun Inverted Index). Setelah itu, Anda dapat memasukkan kueri melalui terminal.
Contoh kueri masukan: **sistem AND informasi NOT jaringan**
