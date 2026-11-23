package game;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class InMemoryRepository implements GameRepository {
    private final Path savePath = Paths.get("savegame.properties");

    @Override
    public void saveGame(Combatant player, Combatant enemy, String battleLog, boolean isPlayerTurn) {
        Properties props = new Properties();
        props.setProperty("player_hp", String.valueOf(player.getNyawa()));
        props.setProperty("player_en", String.valueOf(player.getEnergi()));
        props.setProperty("enemy_hp", String.valueOf(enemy.getNyawa()));
        props.setProperty("enemy_en", String.valueOf(enemy.getEnergi()));
        props.setProperty("battle_log", battleLog != null ? battleLog : "");
        props.setProperty("is_player_turn", String.valueOf(isPlayerTurn));

        try (OutputStream out = Files.newOutputStream(savePath)) {
            props.store(out, "Dungeon Battle Local Save");
        } catch (IOException e) {
            System.err.println("Local Autosave warning: " + e.getMessage());
        }
    }

    @Override
    public String loadGame(Combatant player, Combatant enemy) {
        if (!Files.exists(savePath)) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(savePath)) {
            props.load(in);

            if (player instanceof Char p) {
                p.setNyawa(Integer.parseInt(props.getProperty("player_hp", "10")));
                p.setEnergi(Integer.parseInt(props.getProperty("player_en", "10")));
            }
            if (enemy instanceof Char e) {
                e.setNyawa(Integer.parseInt(props.getProperty("enemy_hp", "10")));
                e.setEnergi(Integer.parseInt(props.getProperty("enemy_en", "10")));
            }
            return props.getProperty("battle_log", null);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Local Load warning: " + e.getMessage());
            return null;
        }
    }
}