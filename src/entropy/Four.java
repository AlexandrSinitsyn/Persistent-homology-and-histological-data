package entropy;

public class Four {
    public String name;
    public double entropy, negative, combined;

    Four(String name, double entropy, double negative, double combined) {
        this.name = name;
        this.entropy = entropy;
        this.negative = negative;
        this.combined = combined;
    }

    @Override
    public String toString() {
        return name + ";" + entropy + ";" + negative + ";" + combined;
    }
}
