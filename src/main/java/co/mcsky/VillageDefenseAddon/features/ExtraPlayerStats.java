package co.mcsky.VillageDefenseAddon.features;

public class ExtraPlayerStats {

    private double totalDamageDone;
    private int kills;

    public ExtraPlayerStats addKills() {
        kills++;
        return this;
    }

    public ExtraPlayerStats addDamage(double damage) {
        totalDamageDone += damage;
        return this;
    }

    public int getKills() {
        return kills;
    }

    public double getTotalDamageDone() {
        return totalDamageDone;
    }

}
