package game;

public interface GameRepository {
    void saveGame(Combatant player, Combatant enemy, String battleLog, boolean isPlayerTurn);
    String loadGame(Combatant player, Combatant enemy);
}