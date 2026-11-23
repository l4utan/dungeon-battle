package game;

public class Skills {
    private final String namaSkill;
    private final int dayaSerang;
    private final int biayaEnergi;

    public Skills(String namaSkill, int dayaSerang, int biayaEnergi) {
        int selisih = Math.abs(dayaSerang - biayaEnergi);
        if (dayaSerang == biayaEnergi || selisih > 2) {
            throw new IllegalArgumentException(
                "Skill " + namaSkill + " melanggar aturan: damage dan energi tidak boleh sama, maksimal selisih 2!"
            );
        }
        this.namaSkill = namaSkill;
        this.dayaSerang = dayaSerang;
        this.biayaEnergi = biayaEnergi;
    }

    public String getNamaSkill() { return namaSkill; }
    public int getDayaSerang() { return dayaSerang; }
    public int getBiayaEnergi() { return biayaEnergi; }
}