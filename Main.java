import game.*;
import ui.GameFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.xrender", "true");
        System.setProperty("sun.java2d.opengl", "true");

        // Aturan No. 1 & 4: 3 Skill (Pukul, Cubit, Tampar), damage != energi, selisih maks 2
        Skills[] skillPlayer = {
            new Skills("Pukul", 3, 2),   // selisih 1
            new Skills("Cubit", 2, 1),   // selisih 1
            new Skills("Tampar", 5, 3)   // selisih 2
        };
        Skills[] skillMusuh = {
            new Skills("Pukul", 3, 2),
            new Skills("Cubit", 2, 1),
            new Skills("Tampar", 5, 3)
        };

        // Aturan No. 2: Masing-masing memiliki 10 poin nyawa dan 10 poin energi
        Combatant playerUtama = new Player("Ksatria Tudung", 10, 10, skillPlayer);
        Combatant musuh = new Enemy("Tengkorak Hidup", 10, 10, skillMusuh);

        // Dependency Inversion Principle (DIP): Inject implementasi PostgreSQL
        String dbUrl = System.getenv("DB_URL");
        GameRepository repository = (dbUrl != null)
            ? new PostgresRepository(dbUrl, System.getenv("DB_USER"), System.getenv("DB_PASS"))
            : new InMemoryRepository();

        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame(playerUtama, musuh, repository);
            frame.setVisible(true);
        });
    }
}