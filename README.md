# 🗡️ Dungeon Battle

Duel taktis *turn-based* 1 lawan 1 di ruang bawah tanah: **Ksatria Tudung** melawan **Tengkorak Hidup**[cite: 1].

![Preview gameplay](assets/preview.gif)[cite: 1]

Bukan sekadar adu klik—kalian berdua sama-sama dibekali **10 Nyawa** dan **10 Energi**. Setiap jurus menguras energi, jadi kamu harus pintar mengatur ritme: kapan waktunya gas menyerang, dan kapan harus menahan diri untuk memulihkan napas sebelum dihabisi balik.

---

## ✨ Fitur Utama

* **Sistem Duel Taktis:** Kelola 10 HP dan 10 Energi dengan cermat di setiap giliran.
* **Aturan Skill Ketat:** Setiap skill punya aturan *balancing* bawaan—nilai damage dan biaya energi tidak boleh sama, dengan selisih maksimal 2 poin. Skill yang melanggar aturan otomatis ditolak sistem (`IllegalArgumentException`).
* **Dialog & Animasi Karakter:** Interaksi pertarungan dilengkapi dialog dinamis dan animasi sprite.
* **Custom Java Swing Window:** Tampilan *window* dibuat kustom dari nol (mulai dari *title bar*, tombol *minimize*, *fullscreen*, *close*, sampai fitur *drag window*).
* **Arsitektur S.O.L.I.D.:** Logika utama game dipisahkan dari urusan penyimpanan data lewat interface `GameRepository` (prinsip *Dependency Inversion*), sehingga mudah diganti antara penyimpanan memori atau PostgreSQL.

---

## 🎮 Cara Main (Langsung Gas)

1. Pastikan **Java 17** atau versi lebih baru sudah terpasang di perangkatmu[cite: 1].
2. Unduh file `game.jar` melalui halaman [Releases](https://github.com/l4utan/dungeon-battle/releases/latest)[cite: 1].
3. Buka terminal di folder tempat file diunduh, lalu jalankan[cite: 1]:

```bash
java -jar game.jar
```[cite: 1]

> **Catatan Penyimpanan:** Versi rilis (`game.jar`) menggunakan `InMemoryRepository` agar bisa langsung dimainkan tanpa perlu konfigurasi database PostgreSQL terlebih dahulu. Artinya, progres *autosave* hanya bertahan selama sesi berjalan dan akan reset saat aplikasi ditutup sepenuhnya[cite: 1].

---

## 🛠️ Build & Jalankan dari Source

Ingin mengulik kode atau menyambungkannya ke database PostgreSQL lokal via `PostgresRepository`?

1. **Compile seluruh source code:**
```bash
javac -cp lib/postgresql.jar -d build Main.java game/*.java ui/*.java
```[cite: 1]

2. **Jalankan game dari folder `build`:**
* **Windows:**
  ```bash
  java -cp "build;lib/postgresql.jar" Main