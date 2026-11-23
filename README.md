# 🗡️ Dungeon Battle

Duel taktis *turn-based* 1 lawan 1 di ruang bawah tanah: **Ksatria Tudung** melawan **Tengkorak Hidup**.

![Preview gameplay](assets/preview.gif)

Bukan sekadar adu klik—kalian berdua sama-sama dibekali **10 Nyawa** dan **10 Energi**. Setiap jurus menguras energi, jadi kamu harus pintar mengatur ritme: kapan waktunya gas menyerang, dan kapan harus menahan diri untuk memulihkan napas sebelum dihabisi balik.

---

## ✨ Fitur Utama

- **Sistem Duel Taktis:** Kelola 10 HP dan 10 Energi dengan cermat di setiap giliran.
- **Aturan Skill Ketat:** Setiap skill punya aturan *balancing* bawaan—nilai damage dan biaya energi tidak boleh sama, dengan selisih maksimal 2 poin. Skill yang melanggar aturan otomatis ditolak sistem (`IllegalArgumentException`).
- **Dialog & Animasi Karakter:** Interaksi pertarungan dilengkapi dialog dinamis dan animasi sprite.
- **Custom Java Swing Window:** Tampilan *window* dibuat kustom dari nol (mulai dari *title bar*, tombol *minimize*, *fullscreen*, *close*, sampai fitur *drag window*).
- **Arsitektur S.O.L.I.D.:** Logika utama game dipisahkan dari urusan penyimpanan data lewat interface `GameRepository` (prinsip *Dependency Inversion*), sehingga mendukung penyimpanan otomatis ke file lokal maupun database PostgreSQL.

---

## 🎮 Cara Main (Windows — Tanpa Perlu Instal Java)

1. Unduh **`DungeonBattle-Windows.zip`** dari halaman [Releases](https://github.com/l4utan/dungeon-battle/releases/latest).
2. Ekstrak file `.zip` tersebut ke folder mana saja.
3. Buka folder hasil ekstrak, lalu klik dua kali **`DungeonBattle.exe`** untuk langsung bermain.
   *(Jika muncul peringatan Windows SmartScreen, klik **More info** lalu pilih **Run anyway**).*

> **Catatan Penyimpanan:** Secara *default*, progres *autosave* akan tersimpan otomatis ke dalam file `savegame.properties` di folder yang sama saat game ditutup, jadi progres pertarunganmu tetap aman saat dibuka kembali.

---

## 🛠️ Build & Jalankan dari Source (Linux / macOS / Windows)

Ingin menjalankan dari *source code* (butuh **Java 17+**) atau menyambungkannya ke database **PostgreSQL** lokal?

1. **Compile seluruh source code:**
```bash
javac -cp lib/postgresql.jar -d build Main.java game/*.java ui/*.java