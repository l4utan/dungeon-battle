package game;

public class Player extends Char {
    public Player(String nama, int nyawa, int energi, Skills[] daftarSkill) {
        super(nama, nyawa, energi, daftarSkill);
    }

    @Override
    public String rayakanKemenangan() {
        String msg = getNama() + " MENANG! Ksatria berhasil menaklukkan dungeon!";
        System.out.println(msg);
        return msg;
    }
}