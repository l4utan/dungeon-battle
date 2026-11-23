package game;

import java.sql.*;

public class PostgresRepository implements GameRepository {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    public PostgresRepository(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    @Override
    public void saveGame(Combatant player, Combatant enemy, String battleLog, boolean isPlayerTurn) {
        String sql = """
            INSERT INTO game_save (id, player_hp, player_en, enemy_hp, enemy_en, battle_log, is_player_turn, updated_at)
            VALUES (1, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (id) DO UPDATE SET
                player_hp = EXCLUDED.player_hp,
                player_en = EXCLUDED.player_en,
                enemy_hp = EXCLUDED.enemy_hp,
                enemy_en = EXCLUDED.enemy_en,
                battle_log = EXCLUDED.battle_log,
                is_player_turn = EXCLUDED.is_player_turn,
                updated_at = CURRENT_TIMESTAMP;
            """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, player.getNyawa());
            pstmt.setInt(2, player.getEnergi());
            pstmt.setInt(3, enemy.getNyawa());
            pstmt.setInt(4, enemy.getEnergi());
            pstmt.setString(5, battleLog);
            pstmt.setBoolean(6, isPlayerTurn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB Autosave warning: " + e.getMessage());
        }
    }

    @Override
    public String loadGame(Combatant player, Combatant enemy) {
        String sql = "SELECT player_hp, player_en, enemy_hp, enemy_en, battle_log FROM game_save WHERE id = 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                if (player instanceof Char p) {
                    p.setNyawa(rs.getInt("player_hp"));
                    p.setEnergi(rs.getInt("player_en"));
                }
                if (enemy instanceof Char e) {
                    e.setNyawa(rs.getInt("enemy_hp"));
                    e.setEnergi(rs.getInt("enemy_en"));
                }
                return rs.getString("battle_log");
            }
        } catch (SQLException e) {
            System.err.println("DB Load warning: " + e.getMessage());
        }
        return null;
    }
}