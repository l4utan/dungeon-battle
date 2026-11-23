package game;

public class InMemoryRepository implements GameRepository {
    private boolean hasSave = false;
    private int playerHp, playerEn, enemyHp, enemyEn;
    private String log;

    @Override
    public void saveGame(Combatant player, Combatant enemy, String battleLog, boolean isPlayerTurn) {
        playerHp = player.getNyawa();
        playerEn = player.getEnergi();
        enemyHp = enemy.getNyawa();
        enemyEn = enemy.getEnergi();
        log = battleLog;
        hasSave = true;
    }

    @Override
    public String loadGame(Combatant player, Combatant enemy) {
        if (!hasSave) return null;
        if (player instanceof Char p) { p.setNyawa(playerHp); p.setEnergi(playerEn); }
        if (enemy instanceof Char e) { e.setNyawa(enemyHp); e.setEnergi(enemyEn); }
        return log;
    }
}