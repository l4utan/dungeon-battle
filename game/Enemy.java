package game;

public class Enemy extends Char {
    public Enemy(String nama, int nyawa, int energi, Skills[] daftarSkill) {
        super(nama, nyawa, energi, daftarSkill);
    }

    @Override
    public String rayakanKemenangan() {
        String msg = getNama() + " MENANG! Penjaga dungeon tak terkalahkan!";
        System.out.println(msg);
        return msg;
    }
}