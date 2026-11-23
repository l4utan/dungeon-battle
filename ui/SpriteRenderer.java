package ui;

import java.awt.*;

public class SpriteRenderer {
    
    public static void drawKnight(Graphics2D g2, int x, int y, String state, float animOffsetX, boolean isHit, int breathOffset) {
        int s = 8;
        int drawX = Math.round(x + animOffsetX);
        int drawY = y + (state.equals("idle") ? breathOffset : 0);

        // Bayangan di lantai (tetap menapak di y dasar)
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(drawX - 5, y + (13 * s) + 4, 12 * s, 2 * s);

        if (state.equals("dead")) {
            g2.setColor(new Color(30, 60, 140));
            g2.fillRect(drawX - 10, y + (11 * s), 14 * s, 3 * s);
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(drawX + (10 * s), y + (12 * s), 8 * s, s);
            return;
        }
        
        Color cloakMain = isHit ? new Color(255, 80, 80) : new Color(35, 85, 200);
        Color cloakShadow = isHit ? new Color(200, 40, 40) : new Color(20, 50, 130);
        
        // 1. Tudung & Jubah
        g2.setColor(cloakMain);
        g2.fillRect(drawX + (2*s), drawY, 6*s, 2*s); 
        g2.fillRect(drawX + (1*s), drawY + (2*s), 8*s, 4*s); 
        g2.fillRect(drawX, drawY + (6*s), 10*s, 8*s);

        g2.setColor(cloakShadow);
        g2.fillRect(drawX, drawY + (6*s), 3*s, 8*s);

        // 2. Wajah & Mata
        g2.setColor(new Color(15, 15, 20));
        g2.fillRect(drawX + (3*s), drawY + (3*s), 4*s, 3*s);
        g2.setColor(Color.CYAN);
        g2.fillRect(drawX + (5*s), drawY + (4*s), s, s);

        // 3. Pedang
        if (state.startsWith("attack")) {
            g2.setColor(new Color(150, 220, 255, 140));
            g2.fillArc(drawX + (6*s), drawY - (4*s), 12*s, 14*s, -45, 110);

            g2.setColor(new Color(220, 230, 240));
            g2.fillRect(drawX + (10*s), drawY - (3*s), 2*s, 10*s);
            g2.setColor(new Color(210, 170, 50));
            g2.fillRect(drawX + (8*s), drawY + (6*s), 6*s, 2*s);
        } else {
            g2.setColor(new Color(200, 210, 220));
            g2.fillRect(drawX + (10*s), drawY + (4*s), 2*s, 10*s);
            g2.setColor(new Color(210, 170, 50));
            g2.fillRect(drawX + (8*s), drawY + (6*s), 6*s, 2*s);
        }
    }

    public static void drawSkeleton(Graphics2D g2, int x, int y, String state, float animOffsetX, boolean isHit, int breathOffset) {
        int s = 8;
        int drawX = Math.round(x + animOffsetX);
        int drawY = y + (state.equals("idle") ? breathOffset : 0);

        // Bayangan di lantai
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(drawX - 5, y + (13 * s) + 4, 11 * s, 2 * s);

        if (state.equals("dead")) {
            g2.setColor(new Color(210, 210, 200));
            g2.fillRect(drawX, y + (12 * s), 10 * s, 2 * s);
            g2.fillRect(drawX + (2 * s), y + (10 * s), 5 * s, 3 * s);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(drawX - (3 * s), y + (13 * s), 12 * s, s);
            return;
        }
        
        Color boneColor = isHit ? new Color(255, 80, 80) : new Color(235, 235, 225);
        g2.setColor(boneColor);
        
        // 1. Tengkorak
        g2.fillRect(drawX + (2*s), drawY, 6*s, 6*s);
        g2.fillRect(drawX + (3*s), drawY + (6*s), 4*s, 2*s);
        
        // 2. Mata & Hidung
        g2.setColor(isHit ? Color.WHITE : new Color(255, 50, 50));
        g2.fillRect(drawX + (2*s) + 4, drawY + (2*s), 2*s - 2, 2*s);
        g2.fillRect(drawX + (5*s), drawY + (2*s), 2*s - 2, 2*s);
        g2.setColor(new Color(30, 30, 35));
        g2.fillRect(drawX + (4*s), drawY + (4*s), 2*s, 1*s);

        // 3. Tulang Belakang & Rusuk
        g2.setColor(boneColor);
        g2.fillRect(drawX + (4*s) + 4, drawY + (8*s), s, 6*s);
        for (int i = 0; i < 3; i++) {
            g2.fillRect(drawX + (2*s), drawY + (8*s) + (i*2*s), 6*s, 1*s);
        }

        // 4. Senjata Sabit
        if (state.startsWith("attack")) {
            g2.setColor(new Color(255, 80, 80, 120));
            g2.fillArc(drawX - (8*s), drawY, 10*s, 12*s, 90, 130);

            g2.setColor(new Color(120, 90, 60));
            g2.fillRect(drawX - (2*s), drawY + (2*s), 2*s, 12*s);
            g2.setColor(new Color(180, 190, 200));
            g2.fillRect(drawX - (7*s), drawY + (2*s), 7*s, 2*s);
        } else {
            g2.setColor(new Color(120, 90, 60));
            g2.fillRect(drawX - (2*s), drawY + (6*s), 2*s, 8*s);
            g2.setColor(new Color(180, 190, 200));
            g2.fillRect(drawX - (6*s), drawY + (6*s), 6*s, 2*s);
        }
    }
}