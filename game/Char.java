package game;

import java.util.Random;

public abstract class Char implements Combatant {
    private String nama;
    private int nyawa;
    private int maxNyawa;
    private int energi;
    private int maxEnergi;
    private Skills[] daftarSkill; // Relasi Agregasi/Asosiasi: Char memiliki Skills
    private static final Random random = new Random();

    public Char(String nama, int nyawa, int energi, Skills[] daftarSkill) {
        this.nama = nama;
        this.nyawa = nyawa;
        this.maxNyawa = nyawa;
        this.energi = energi;
        this.maxEnergi = energi;
        this.daftarSkill = daftarSkill;
    }

    @Override public String getNama() { return nama; }
    @Override public int getNyawa() { return nyawa; }
    @Override public int getMaxNyawa() { return maxNyawa; }
    @Override public int getEnergi() { return energi; }
    @Override public int getMaxEnergi() { return maxEnergi; }
    @Override public boolean masihHidup() { return nyawa > 0; }
    @Override public Skills[] getDaftarSkill() { return daftarSkill; }

    public void setNyawa(int nyawa) {
        this.nyawa = Math.max(0, Math.min(this.maxNyawa, nyawa));
    }

    public void setEnergi(int energi) {
        this.energi = Math.max(0, Math.min(this.maxEnergi, energi));
    }

    @Override
    public boolean gunakanSkill(int indexSkill, Combatant lawan) {
        if (daftarSkill == null || indexSkill < 0 || indexSkill >= daftarSkill.length) return false;
        Skills skill = daftarSkill[indexSkill];
        if (this.energi < skill.getBiayaEnergi()) return false;

        this.energi -= skill.getBiayaEnergi();
        lawan.terimaSerangan(skill.getDayaSerang());
        System.out.println(nama + " memakai [" + skill.getNamaSkill() + "] -> " 
            + lawan.getNama() + " -" + skill.getDayaSerang() + " HP (Sisa EN: " + this.energi + ")");
        return true;
    }

    @Override
    public Skills serangRandom(Combatant lawan) {
        // Cari skill yang energinya cukup; jika tidak ada yang cukup, otomatis istirahat
        Skills terpilih = daftarSkill[random.nextInt(daftarSkill.length)];
        if (this.energi < terpilih.getBiayaEnergi()) {
            for (Skills s : daftarSkill) {
                if (this.energi >= s.getBiayaEnergi()) {
                    terpilih = s;
                    break;
                }
            }
        }

        if (this.energi >= terpilih.getBiayaEnergi()) {
            this.energi -= terpilih.getBiayaEnergi();
            lawan.terimaSerangan(terpilih.getDayaSerang());
            System.out.println("[RANDOM] " + nama + " memakai " + terpilih.getNamaSkill() 
                + " -> " + lawan.getNama() + " -" + terpilih.getDayaSerang() + " HP");
            return terpilih;
        } else {
            pulihkanEnergi(4);
            System.out.println("[RANDOM] " + nama + " kehabisan energi dan beristirahat (+4 EN)");
            return null;
        }
    }

    @Override
    public void terimaSerangan(int jumlahDamage) {
        this.nyawa = Math.max(0, this.nyawa - jumlahDamage);
    }

    @Override
    public void pulihkanEnergi(int jumlah) {
        this.energi = Math.min(this.maxEnergi, this.energi + jumlah);
    }

    @Override
    public void resetStatus() {
        this.nyawa = this.maxNyawa;
        this.energi = this.maxEnergi;
    }

    @Override
    public abstract String rayakanKemenangan();
}