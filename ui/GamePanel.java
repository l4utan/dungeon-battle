package ui;

import game.Combatant;
import game.DialogManager;
import game.GameRepository;
import game.Skills;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements ActionListener {
    private final Combatant player;
    private final Combatant enemy;
    private final GameRepository repository;
    
    private String playerState = "idle";
    private String enemyState = "idle";
    
    private boolean isPlayerTurn = true;
    private boolean animating = false;
    private boolean autoBattleMode = false;
    private Timer timer;
    private String battleLog = "Giliranmu! Pilih skill atau klik AUTO.";
    
    private long lastSaveTime = 0;

    // Dialog Tipis-Tipis
    private String playerDialog = "";
    private String enemyDialog = "";
    private long playerDialogExpire = 0;
    private long enemyDialogExpire = 0;

    // Cache Background
    private BufferedImage cachedBackground;
    private int cachedW = -1;
    private int cachedH = -1;

    // Animasi 24 FPS
    private float playerAnimX = 0f;
    private float targetPlayerX = 0f;
    private float enemyAnimX = 0f;
    private float targetEnemyX = 0f;
    private int frameTick = 0;

    // Efek damage melayang
    private int floatingDamage = 0;
    private int damageX = 0;
    private int damageY = 0;
    private boolean showDamage = false;

    // Interaksi Mouse & Tombol (0-2: Skill, 3: Rest, 4: Auto, 5: Restart)
    private int hoveredButton = -1;
    private Rectangle[] skillBounds = new Rectangle[3];
    private Rectangle restButtonBounds;
    private Rectangle autoButtonBounds;
    private Rectangle restartButtonBounds;

    public GamePanel(Combatant player, Combatant enemy, GameRepository repository) {
        this.player = player;
        this.enemy = enemy;
        this.repository = repository;
        setBackground(new Color(24, 24, 32)); 
        setDoubleBuffered(true);

        String loadedLog = repository.loadGame(player, enemy);
        if (loadedLog != null) {
            this.battleLog = loadedLog;
            if (!player.masihHidup()) this.playerState = "dead";
            if (!enemy.masihHidup()) this.enemyState = "dead";
            this.isPlayerTurn = true;
            if (player.masihHidup() && enemy.masihHidup()) {
                showPlayerDialog("Kembali bertarung...", 3000);
            }
        } else {
            showPlayerDialog(DialogManager.getRandomDialog("PLAYER_START"), 3000);
            showEnemyDialog(DialogManager.getRandomDialog("ENEMY_START"), 3500);
        }
        
        initMouseListeners();
        timer = new Timer(42, this); // 24 FPS
        timer.start();
    }

    public void triggerAutoSave() {
        final String logSnapshot = battleLog;
        final boolean turnSnapshot = isPlayerTurn;
        lastSaveTime = System.currentTimeMillis();

        new Thread(() -> repository.saveGame(player, enemy, logSnapshot, turnSnapshot)).start();
    }

    private void showPlayerDialog(String text, int durationMs) {
        this.playerDialog = text;
        this.playerDialogExpire = System.currentTimeMillis() + durationMs;
    }

    private void showEnemyDialog(String text, int durationMs) {
        this.enemyDialog = text;
        this.enemyDialogExpire = System.currentTimeMillis() + durationMs;
    }

    private void initMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int prevHover = hoveredButton;
                hoveredButton = -1;

                if (!player.masihHidup() || !enemy.masihHidup()) {
                    if (restartButtonBounds != null && restartButtonBounds.contains(p)) hoveredButton = 5;
                } else {
                    for (int i = 0; i < skillBounds.length; i++) {
                        if (skillBounds[i] != null && skillBounds[i].contains(p)) hoveredButton = i;
                    }
                    if (restButtonBounds != null && restButtonBounds.contains(p)) hoveredButton = 3;
                    if (autoButtonBounds != null && autoButtonBounds.contains(p)) hoveredButton = 4;
                }

                setCursor(hoveredButton != -1 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                if (prevHover != hoveredButton) repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();

                if (!player.masihHidup() || !enemy.masihHidup()) {
                    if (restartButtonBounds != null && restartButtonBounds.contains(p)) restartGame();
                    return;
                }

                if (autoButtonBounds != null && autoButtonBounds.contains(p)) {
                    toggleAutoBattle();
                    return;
                }

                if (autoBattleMode) return;

                for (int i = 0; i < skillBounds.length; i++) {
                    if (skillBounds[i] != null && skillBounds[i].contains(p)) {
                        executePlayerTurn(i);
                        return;
                    }
                }

                if (restButtonBounds != null && restButtonBounds.contains(p)) {
                    executePlayerRest();
                }
            }
        });
    }

    private void toggleAutoBattle() {
        autoBattleMode = !autoBattleMode;
        if (autoBattleMode && isPlayerTurn && !animating && player.masihHidup() && enemy.masihHidup()) {
            executeAutoPlayerTurn();
        }
        repaint();
    }

    private void restartGame() {
        player.resetStatus();
        enemy.resetStatus();
        playerState = "idle";
        enemyState = "idle";
        playerAnimX = 0f;
        targetPlayerX = 0f;
        enemyAnimX = 0f;
        targetEnemyX = 0f;
        isPlayerTurn = true;
        animating = false;
        autoBattleMode = false;
        showDamage = false;
        hoveredButton = -1;
        battleLog = "Pertarungan baru dimulai! HP & EN 10/10.";
        
        showPlayerDialog(DialogManager.getRandomDialog("PLAYER_START"), 2800);
        showEnemyDialog(DialogManager.getRandomDialog("ENEMY_START"), 3200);
        triggerAutoSave();
        repaint();
    }

    private void executeAutoPlayerTurn() {
        if (!isPlayerTurn || animating || !player.masihHidup() || !enemy.masihHidup()) return;

        isPlayerTurn = false;
        animating = true;

        Skills used = player.serangRandom(enemy);
        if (used == null) {
            battleLog = "[AUTO] " + player.getNama() + " kehabisan energi -> Istirahat (+4 EN)";
            showPlayerDialog(DialogManager.getRandomDialog("PLAYER_REST"), 1500);
            triggerAutoSave();
            Timer delay = new Timer(550, e -> enemyTurn());
            delay.setRepeats(false);
            delay.start();
            return;
        }

        playerState = "attack1";
        targetPlayerX = 95f;
        battleLog = "[AUTO] " + player.getNama() + " memakai " + used.getNamaSkill() + " (-" + used.getDayaSerang() + " HP)";
        showPlayerDialog(DialogManager.getRandomDialog("PLAYER_ATTACK"), 1500);

        Timer attackTimer = new Timer(280, e -> {
            enemyState = "hit";
            targetEnemyX = 22f;
            floatingDamage = used.getDayaSerang();
            damageX = (getWidth() / 2) + 125;
            damageY = 260;
            showDamage = true;

            if (!enemy.masihHidup()) {
                enemyState = "dead";
                autoBattleMode = false;
                battleLog = player.rayakanKemenangan();
                showEnemyDialog("Kriek... aku kalah...", 2500);
            } else {
                showEnemyDialog(DialogManager.getRandomDialog("ENEMY_HIT"), 1400);
            }

            triggerAutoSave();

            Timer resetTimer = new Timer(450, e2 -> {
                playerState = "idle";
                targetPlayerX = 0f;
                targetEnemyX = 0f;
                showDamage = false;
                if (enemy.masihHidup()) {
                    enemyState = "idle";
                    enemyTurn();
                } else {
                    animating = false;
                }
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        });
        attackTimer.setRepeats(false);
        attackTimer.start();
    }

    public void executePlayerRest() {
        if (!isPlayerTurn || animating || !player.masihHidup() || !enemy.masihHidup()) return;
        
        isPlayerTurn = false;
        animating = true;
        player.pulihkanEnergi(4);
        battleLog = player.getNama() + " beristirahat (+4 Energi)!";
        showPlayerDialog(DialogManager.getRandomDialog("PLAYER_REST"), 2000);
        triggerAutoSave();
        repaint();

        Timer delayTimer = new Timer(650, e -> enemyTurn());
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    public void executePlayerTurn(int skillIndex) {
        if (!isPlayerTurn || animating || !player.masihHidup() || !enemy.masihHidup()) return;
        
        Skills[] skills = player.getDaftarSkill();
        if (skills == null || skillIndex >= skills.length) return;
        Skills selectedSkill = skills[skillIndex];
        
        if (!player.gunakanSkill(skillIndex, enemy)) {
            battleLog = "Energi tidak cukup untuk " + selectedSkill.getNamaSkill() + "! Klik Rest.";
            showPlayerDialog("Energiku habis...", 1500);
            repaint();
            return;
        }

        isPlayerTurn = false;
        animating = true;
        playerState = "attack" + (skillIndex + 1);
        targetPlayerX = 95f;
        
        int damage = selectedSkill.getDayaSerang();
        battleLog = player.getNama() + " memakai " + selectedSkill.getNamaSkill() + " (-" + damage + " HP)!";

        String dialogKey = selectedSkill.getNamaSkill().equals("Tampar") ? "PLAYER_ULTIMATE" : "PLAYER_ATTACK";
        showPlayerDialog(DialogManager.getRandomDialog(dialogKey), 1800);

        Timer attackTimer = new Timer(300, e -> {
            enemyState = "hit";
            targetEnemyX = 22f;
            floatingDamage = damage;
            damageX = (getWidth() / 2) + 125;
            damageY = 260;
            showDamage = true;
            
            if (!enemy.masihHidup()) {
                enemyState = "dead";
                battleLog = player.rayakanKemenangan();
                showEnemyDialog("Kriek... aku kalah...", 2500);
            } else {
                showEnemyDialog(DialogManager.getRandomDialog("ENEMY_HIT"), 1800);
            }

            triggerAutoSave();
            
            Timer resetTimer = new Timer(500, e2 -> {
                playerState = "idle";
                targetPlayerX = 0f;
                targetEnemyX = 0f;
                showDamage = false;
                
                if (enemy.masihHidup()) {
                    enemyState = "idle";
                    enemyTurn();
                } else {
                    animating = false;
                }
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        });
        attackTimer.setRepeats(false);
        attackTimer.start();
    }

    private void enemyTurn() {
        enemyState = "attack";
        targetEnemyX = -95f;

        Skills chosen = enemy.serangRandom(player);
        if (chosen == null) {
            enemyState = "idle";
            targetEnemyX = 0f;
            battleLog = enemy.getNama() + " kehabisan energi & istirahat (+4 EN)!";
            showEnemyDialog("Tunggu sebentar, ngos-ngosan...", 1800);
            isPlayerTurn = true;
            animating = false;
            triggerAutoSave();
            if (autoBattleMode) {
                Timer nextAuto = new Timer(500, e -> executeAutoPlayerTurn());
                nextAuto.setRepeats(false);
                nextAuto.start();
            }
            return;
        }

        battleLog = enemy.getNama() + " memakai " + chosen.getNamaSkill() + " (-" + chosen.getDayaSerang() + " HP)!";
        showEnemyDialog(DialogManager.getRandomDialog("ENEMY_ATTACK"), 1600);
        
        Timer enemyAttackTimer = new Timer(350, e -> {
            int damage = chosen.getDayaSerang();
            playerState = "hit";
            targetPlayerX = -22f;

            floatingDamage = damage;
            damageX = (getWidth() / 2) - 155;
            damageY = 260;
            showDamage = true;

            if (!player.masihHidup()) {
                playerState = "dead";
                autoBattleMode = false;
                battleLog = enemy.rayakanKemenangan();
                showPlayerDialog("Maaf... aku tumbang...", 2500);
            } else {
                showPlayerDialog(DialogManager.getRandomDialog("PLAYER_HIT"), 1600);
            }
            
            Timer resetTimer = new Timer(500, e2 -> {
                enemyState = "idle";
                targetEnemyX = 0f;
                targetPlayerX = 0f;
                showDamage = false;
                playerState = player.masihHidup() ? "idle" : "dead";
                if (player.masihHidup()) {
                    player.pulihkanEnergi(1);
                }
                isPlayerTurn = true; 
                animating = false;
                triggerAutoSave();

                if (autoBattleMode && player.masihHidup() && enemy.masihHidup()) {
                    Timer nextAuto = new Timer(350, e3 -> executeAutoPlayerTurn());
                    nextAuto.setRepeats(false);
                    nextAuto.start();
                }
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        });
        enemyAttackTimer.setRepeats(false);
        enemyAttackTimer.start();
    }

    private void buildBackgroundCache(int w, int h) {
        if (w <= 0 || h <= 0) return;
        cachedW = w;
        cachedH = h;
        int centerX = w / 2;
        int horizonY = 390;

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        cachedBackground = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
        Graphics2D g2 = cachedBackground.createGraphics();

        g2.setColor(new Color(32, 32, 42));
        g2.fillRect(0, 0, w, horizonY);

        g2.setColor(new Color(42, 42, 54));
        for (int j = 0; j < horizonY; j += 32) {
            int offset = ((j / 32) % 2 == 0) ? 0 : 32;
            for (int i = -32; i < w; i += 64) {
                g2.fillRect(i + offset + 2, j + 2, 60, 28);
            }
        }

        GradientPaint topShadow = new GradientPaint(0, 0, new Color(10, 10, 15, 220), 0, 130, new Color(10, 10, 15, 0));
        g2.setPaint(topShadow);
        g2.fillRect(0, 0, w, 130);

        g2.setColor(new Color(48, 38, 38));
        g2.fillRect(0, horizonY, w, h - horizonY);
        g2.setColor(new Color(28, 22, 22));
        g2.fillRect(0, horizonY, w, 6);

        drawCachedCandle(g2, centerX - 155, 350);
        drawCachedCandle(g2, centerX + 145, 350);

        g2.dispose();
    }

    private void drawCachedCandle(Graphics2D g2, int x, int y) {
        RadialGradientPaint glow = new RadialGradientPaint(
            new Point(x + 6, y), 110f,
            new float[]{0.0f, 1.0f},
            new Color[]{new Color(255, 190, 60, 65), new Color(255, 190, 60, 0)}
        );
        g2.setPaint(glow);
        g2.fillOval(x - 104, y - 110, 220, 220);

        g2.setColor(new Color(230, 220, 190));
        g2.fillRect(x, y, 12, 35);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        int centerX = w / 2;

        if (cachedBackground == null || cachedW != w || cachedH != h) {
            buildBackgroundCache(w, h);
        }
        if (cachedBackground != null) {
            g2.drawImage(cachedBackground, 0, 0, null);
        }

        int flicker = (frameTick % 4 < 2) ? 0 : 1;
        drawCandleFlame(g2, centerX - 155, 350, flicker);
        drawCandleFlame(g2, centerX + 145, 350, 1 - flicker);

        int floorY = 285;
        int playerBaseX = centerX - 190;
        int enemyBaseX = centerX + 110;
        int breathOffset = (int) Math.round(Math.sin(frameTick * 0.35) * 2);

        SpriteRenderer.drawKnight(g2, playerBaseX, floorY, playerState, playerAnimX, playerState.equals("hit"), breathOffset);
        SpriteRenderer.drawSkeleton(g2, enemyBaseX, floorY, enemyState, enemyAnimX, enemyState.equals("hit"), -breathOffset);
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long now = System.currentTimeMillis();
        if (now < playerDialogExpire) {
            drawSpeechBubble(g2, playerDialog, Math.round(playerBaseX + playerAnimX + 40), floorY - 5, new Color(80, 160, 255));
        }
        if (now < enemyDialogExpire) {
            drawSpeechBubble(g2, enemyDialog, Math.round(enemyBaseX + enemyAnimX + 40), floorY - 5, new Color(240, 95, 95));
        }

        if (showDamage) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 28));
            g2.setColor(Color.BLACK);
            g2.drawString("-" + floatingDamage, damageX + 2, damageY + 2);
            g2.setColor(new Color(255, 70, 70));
            g2.drawString("-" + floatingDamage, damageX, damageY);
        }

        drawTopHUD(g2);
        drawBottomControlPanel(g2);

        if (!player.masihHidup() || !enemy.masihHidup()) {
            drawEndGameOverlay(g2);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawSpeechBubble(Graphics2D g2, String text, int anchorX, int anchorY, Color borderColor) {
        if (text == null || text.isEmpty()) return;

        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        FontMetrics fm = g2.getFontMetrics();
        int padX = 12;
        int padY = 8;
        int boxW = fm.stringWidth(text) + (padX * 2);
        int boxH = fm.getHeight() + (padY * 2);

        int boxX = Math.max(15, Math.min(getWidth() - boxW - 15, anchorX - (boxW / 2)));
        int boxY = anchorY - boxH - 12;

        g2.setColor(new Color(20, 22, 30, 230));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g2.setColor(borderColor);
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        int[] triX = {anchorX - 5, anchorX + 5, anchorX};
        int[] triY = {boxY + boxH, boxY + boxH, boxY + boxH + 7};
        g2.setColor(new Color(20, 22, 30, 230));
        g2.fillPolygon(triX, triY, 3);
        g2.setColor(borderColor);
        g2.drawLine(anchorX - 5, boxY + boxH, anchorX, boxY + boxH + 7);
        g2.drawLine(anchorX + 5, boxY + boxH, anchorX, boxY + boxH + 7);

        g2.setColor(new Color(240, 240, 245));
        g2.drawString(text, boxX + padX, boxY + padY + fm.getAscent());
    }

    private void drawCandleFlame(Graphics2D g2, int x, int y, int flicker) {
        g2.setColor(new Color(255, 140, 0));
        g2.fillOval(x + 2, y - 12 - flicker, 8, 12 + flicker);
        g2.setColor(Color.YELLOW);
        g2.fillOval(x + 4, y - 9, 4, 7);
    }

    private void drawTopHUD(Graphics2D g2) {
        int w = getWidth();

        drawStatCard(g2, 25, 18, 310, 82);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.drawString(player.getNama().toUpperCase(), 40, 40);

        drawProgressBar(g2, 40, 48, 280, 16, player.getNyawa(), player.getMaxNyawa(), new Color(65, 195, 100), "HP");
        drawProgressBar(g2, 40, 72, 280, 14, player.getEnergi(), player.getMaxEnergi(), new Color(55, 155, 245), "EN");

        String turnText = autoBattleMode ? "AUTO BATTLE (RANDOM)" : (isPlayerTurn ? "GILIRAN KAMU" : "GILIRAN MUSUH");
        Color turnColor = autoBattleMode ? new Color(255, 195, 65) : (isPlayerTurn ? new Color(80, 210, 120) : new Color(240, 90, 90));
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int badgeW = fm.stringWidth(turnText) + 24;
        int badgeX = (w - badgeW) / 2;

        g2.setColor(new Color(20, 20, 28, 220));
        g2.fillRoundRect(badgeX, 22, badgeW, 28, 14, 14);
        g2.setColor(turnColor);
        g2.drawRoundRect(badgeX, 22, badgeW, 28, 14, 14);
        g2.drawString(turnText, badgeX + 12, 41);

        if (System.currentTimeMillis() - lastSaveTime < 2000) {
            g2.setColor(new Color(140, 220, 160, 200));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            String saveMsg = "● SAVED TO POSTGRESQL";
            int sw = g2.getFontMetrics().stringWidth(saveMsg);
            g2.drawString(saveMsg, (w - sw) / 2, 66);
        }

        drawStatCard(g2, w - 335, 18, 310, 82);
        g2.setColor(new Color(255, 120, 120));
        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2.drawString(enemy.getNama().toUpperCase(), w - 320, 40);

        drawProgressBar(g2, w - 320, 48, 280, 16, enemy.getNyawa(), enemy.getMaxNyawa(), new Color(225, 65, 65), "HP");
        drawProgressBar(g2, w - 320, 72, 280, 14, enemy.getEnergi(), enemy.getMaxEnergi(), new Color(180, 110, 240), "EN");
    }

    private void drawStatCard(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(18, 18, 26, 210));
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(70, 70, 90));
        g2.drawRoundRect(x, y, w, h, 12, 12);
    }

    private void drawProgressBar(Graphics2D g2, int x, int y, int w, int h, int val, int max, Color barColor, String label) {
        g2.setColor(new Color(35, 35, 45));
        g2.fillRoundRect(x, y, w, h, 6, 6);

        double ratio = max > 0 ? (double) val / max : 0;
        int fillW = (int) (w * ratio);
        if (fillW > 0) {
            g2.setColor(barColor);
            g2.fillRoundRect(x, y, fillW, h, 6, 6);
        }

        g2.setColor(new Color(20, 20, 25));
        g2.drawRoundRect(x, y, w, h, 6, 6);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        String text = label + " : " + val + "/" + max;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + h - 3);
    }

    private void drawBottomControlPanel(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        int panelH = 125;
        int panelY = h - panelH;

        g2.setColor(new Color(18, 18, 26, 240));
        g2.fillRect(0, panelY, w, panelH);
        g2.setColor(new Color(65, 65, 85));
        g2.drawLine(0, panelY, w, panelY);

        int logX = 25;
        int logY = panelY + 18;
        int logW = 295;
        int logH = 88;
        g2.setColor(new Color(28, 28, 38));
        g2.fillRoundRect(logX, logY, logW, logH, 10, 10);
        g2.setColor(new Color(60, 60, 80));
        g2.drawRoundRect(logX, logY, logW, logH, 10, 10);

        g2.setColor(new Color(140, 140, 165));
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("BATTLE LOG", logX + 12, logY + 20);

        g2.setColor(new Color(255, 220, 110));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawWrappedText(g2, battleLog, logX + 12, logY + 42, logW - 24);

        // 5 Tombol identik di kanan: Pukul, Cubit, Tampar, Rest, Auto
        Skills[] skills = player.getDaftarSkill();
        int btnW = 102;
        int btnH = 88;
        int gap = 10;
        int totalButtons = 5;
        int totalGroupWidth = (totalButtons * btnW) + ((totalButtons - 1) * gap);
        int startX = w - 25 - totalGroupWidth;

        for (int i = 0; i < 3; i++) {
            int bx = startX + i * (btnW + gap);
            int by = panelY + 18;
            skillBounds[i] = new Rectangle(bx, by, btnW, btnH);

            if (skills != null && i < skills.length) {
                Skills sk = skills[i];
                boolean enoughEnergy = player.getEnergi() >= sk.getBiayaEnergi();
                boolean canClick = !autoBattleMode && isPlayerTurn && !animating && enoughEnergy && player.masihHidup() && enemy.masihHidup();
                boolean isHovered = (hoveredButton == i) && canClick;

                g2.setColor(!canClick ? new Color(32, 32, 40) : (isHovered ? new Color(55, 75, 115) : new Color(40, 45, 65)));
                g2.fillRoundRect(bx, by, btnW, btnH, 10, 10);
                g2.setColor(isHovered ? new Color(110, 170, 255) : new Color(75, 85, 115));
                g2.drawRoundRect(bx, by, btnW, btnH, 10, 10);

                g2.setColor(canClick ? Color.WHITE : Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString(sk.getNamaSkill(), bx + 10, by + 24);

                g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                g2.setColor(canClick ? new Color(255, 110, 110) : Color.DARK_GRAY);
                g2.drawString("DMG : " + sk.getDayaSerang(), bx + 10, by + 58);

                g2.setColor(enoughEnergy ? new Color(90, 185, 255) : new Color(220, 70, 70));
                g2.drawString("COST: " + sk.getBiayaEnergi() + " EN", bx + 10, by + 76);
            }
        }

        // Tombol 4: Rest
        int restX = startX + 3 * (btnW + gap);
        int restY = panelY + 18;
        restButtonBounds = new Rectangle(restX, restY, btnW, btnH);

        boolean canRest = !autoBattleMode && isPlayerTurn && !animating && player.masihHidup() && enemy.masihHidup();
        boolean restHovered = (hoveredButton == 3) && canRest;

        g2.setColor(restHovered ? new Color(45, 95, 75) : (canRest ? new Color(35, 65, 55) : new Color(32, 32, 40)));
        g2.fillRoundRect(restX, restY, btnW, btnH, 10, 10);
        g2.setColor(restHovered ? new Color(90, 220, 150) : new Color(60, 110, 90));
        g2.drawRoundRect(restX, restY, btnW, btnH, 10, 10);

        g2.setColor(canRest ? Color.WHITE : Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Rest", restX + 10, restY + 24);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(canRest ? new Color(110, 240, 165) : Color.DARK_GRAY);
        g2.drawString("GAIN: +4 EN", restX + 10, restY + 76);

        // Tombol 5: Auto Battle (Random Skill sampai HP 0)
        int autoX = startX + 4 * (btnW + gap);
        int autoY = panelY + 18;
        autoButtonBounds = new Rectangle(autoX, autoY, btnW, btnH);

        boolean canAuto = player.masihHidup() && enemy.masihHidup();
        boolean autoHovered = (hoveredButton == 4) && canAuto;

        g2.setColor(autoBattleMode ? new Color(145, 100, 25) : (autoHovered ? new Color(95, 75, 35) : new Color(65, 52, 30)));
        g2.fillRoundRect(autoX, autoY, btnW, btnH, 10, 10);
        g2.setColor(autoBattleMode ? new Color(255, 210, 80) : new Color(150, 120, 60));
        g2.drawRoundRect(autoX, autoY, btnW, btnH, 10, 10);

        g2.setColor(canAuto ? Color.WHITE : Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(autoBattleMode ? "Stop" : "Auto", autoX + 10, autoY + 24);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(new Color(255, 205, 90));
        g2.drawString("RANDOM", autoX + 10, autoY + 42);
        g2.drawString("BATTLE", autoX + 10, autoY + 56);
    }

    private void drawEndGameOverlay(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(10, 10, 16, 180));
        g2.fillRect(0, 0, w, h);

        int boxW = 420;
        int boxH = 195;
        int boxX = (w - boxW) / 2;
        int boxY = (h - boxH) / 2 - 20;

        g2.setColor(new Color(25, 25, 36));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        
        boolean playerWon = player.masihHidup();
        Color themeColor = playerWon ? new Color(80, 210, 120) : new Color(235, 75, 75);
        g2.setColor(themeColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2.setStroke(new BasicStroke(1));

        String title = playerWon ? "PLAYER MENANG!" : "MUSUH MENANG!";
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, boxX + (boxW - fm.stringWidth(title)) / 2, boxY + 55);

        String sub = playerWon ? player.getNama() + " mengalahkan " + enemy.getNama() + "!" : enemy.getNama() + " mengalahkan " + player.getNama() + "!";
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g2.getFontMetrics();
        g2.drawString(sub, boxX + (boxW - fm.stringWidth(sub)) / 2, boxY + 85);

        int btnW = 160;
        int btnH = 42;
        int btnX = boxX + (boxW - btnW) / 2;
        int btnY = boxY + 120;
        restartButtonBounds = new Rectangle(btnX, btnY, btnW, btnH);

        g2.setColor(hoveredButton == 5 ? themeColor.brighter() : themeColor);
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2.setColor(new Color(15, 15, 20));
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        fm = g2.getFontMetrics();
        String btnText = "MAIN LAGI";
        g2.drawString(btnText, btnX + (btnW - fm.stringWidth(btnText)) / 2, btnY + 26);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;

        for (String word : words) {
            if (fm.stringWidth(line + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g2.drawString(line.toString(), x, curY);
                curY += fm.getHeight() + 2;
                line = new StringBuilder(word).append(" ");
            }
        }
        g2.drawString(line.toString(), x, curY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frameTick++;
        playerAnimX += (targetPlayerX - playerAnimX) * 0.42f;
        enemyAnimX += (targetEnemyX - enemyAnimX) * 0.42f;
        if (showDamage) damageY -= 2;
        repaint();
    }
}