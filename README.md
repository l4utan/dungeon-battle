# Dungeon Battle

Game battle turn-based. Ksatria Tudung melawan Tengkorak Hidup.

![Preview gameplay](assets/preview.gif)

## Cara main

1. Install Java 17 atau lebih baru.
2. Download `game.jar` dari [Releases](https://github.com/l4utan/dungeon-battle/releases/latest).
3. Jalankan:

```bash
java -jar game.jar
```

## Catatan

Save game tersimpan di memori. Hilang kalau game ditutup.

## Build dari source

```bash
javac -cp lib/postgresql.jar -d build Main.java game/*.java ui/*.java
```