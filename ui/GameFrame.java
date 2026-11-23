package ui;

import game.Combatant;
import game.GameRepository;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameFrame extends JFrame {
    private GamePanel gamePanel;
    private Point initialClick;

    public GameFrame(Combatant player, Combatant enemy, GameRepository repository) {
        setTitle("Dungeon Battle");
        setUndecorated(true); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 75), 2));
        
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(18, 18, 24));
        titleBar.setPreferredSize(new Dimension(920, 32));
        titleBar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));

        JLabel titleLabel = new JLabel("DUNGEON BATTLE // OOP & S.O.L.I.D.");
        titleLabel.setForeground(new Color(180, 180, 200));
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        windowControls.setOpaque(false);
        
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        titleBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - initialClick.x, getLocation().y + e.getY() - initialClick.y);
            }
        });

        CustomButton btnMin = new CustomButton(new Color(245, 190, 65), "Minimize");
        CustomButton btnMax = new CustomButton(new Color(80, 200, 120), "Fullscreen");
        CustomButton btnClose = new CustomButton(new Color(235, 85, 85), "Close");

        btnMin.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMax.addActionListener(e -> setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH));
        btnClose.addActionListener(e -> {
            gamePanel.triggerAutoSave();
            System.exit(0);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.triggerAutoSave();
            }
        });

        windowControls.add(btnMin);
        windowControls.add(btnMax);
        windowControls.add(btnClose);
        titleBar.add(windowControls, BorderLayout.EAST);

        gamePanel = new GamePanel(player, enemy, repository);

        mainContainer.add(titleBar, BorderLayout.NORTH);
        mainContainer.add(gamePanel, BorderLayout.CENTER);
        
        setContentPane(mainContainer);
    }
}