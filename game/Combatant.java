package game;

public interface Combatant {
    String getNama();
    int getNyawa();
    int getMaxNyawa();
    int getEnergi();
    int getMaxEnergi();
    boolean masihHidup();
    Skills[] getDaftarSkill();
    
    // Serangan random sesuai syarat no. 5
    Skills serangRandom(Combatant lawan);
    boolean gunakanSkill(int indexSkill, Combatant lawan);
    void terimaSerangan(int jumlahDamage);
    void pulihkanEnergi(int jumlah);
    void resetStatus();
    String rayakanKemenangan();
}