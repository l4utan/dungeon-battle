package game;

import java.util.Random;

public class DialogManager {
    private static final Random random = new Random();

    private static final String[] PLAYER_START = {
        "Tulang belulang lagi...",
        "Jangan menghalangi jalanku.",
        "Pedangku sudah siap."
    };

    private static final String[] ENEMY_START = {
        "Kriek... daging segar!",
        "Berani masuk ke dungeon-ku?!",
        "Heh, jubah birumu bagus juga."
    };

    private static final String[] PLAYER_ATTACK = {
        "Rasakan ini!",
        "Terimalah!",
        "Belum selesai!"
    };

    private static final String[] PLAYER_ULTIMATE = {
        "PLAK!! Tamparan maut!",
        "Ini jurus pamungkasku!"
    };

    private static final String[] PLAYER_HIT = {
        "Cih, lumayan juga...",
        "Hanya goresan kecil.",
        "Awas kau!"
    };

    private static final String[] ENEMY_ATTACK = {
        "Makan sabit ini!",
        "Kriek-kriek! Kena kau!",
        "Jangan harap bisa kabur!"
    };

    private static final String[] ENEMY_HIT = {
        "Aduh! Tulang rusukku!",
        "Kriek?! Sakit juga!",
        "Curang, sakit banget!"
    };

    private static final String[] PLAYER_REST = {
        "Tarik napas dulu...",
        "Mengumpulkan energi..."
    };

    public static String getRandomDialog(String type) {
        String[] pool = switch (type) {
            case "PLAYER_START" -> PLAYER_START;
            case "ENEMY_START" -> ENEMY_START;
            case "PLAYER_ATTACK" -> PLAYER_ATTACK;
            case "PLAYER_ULTIMATE" -> PLAYER_ULTIMATE;
            case "PLAYER_HIT" -> PLAYER_HIT;
            case "ENEMY_ATTACK" -> ENEMY_ATTACK;
            case "ENEMY_HIT" -> ENEMY_HIT;
            case "PLAYER_REST" -> PLAYER_REST;
            default -> new String[]{""};
        };
        return pool[random.nextInt(pool.length)];
    }
}