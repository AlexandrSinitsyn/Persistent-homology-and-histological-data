package activity;

public class NormalDistribution {

    private double[] data;

    NormalDistribution(double[] data) {
        this.data = data;
    }



    double getMean() {
        double sum = 0;

        for(double a : data)
            sum += a;

        return sum / data.length;
    }

    double getStdDev() {
        double mean = getMean();
        double sum = 0;

        for(double a : data)
            sum += a * a;

        sum /= data.length;

        return Math.sqrt(sum - mean * mean);
    }


    private double normal(double x, double mean, double std) {
        var arg = 1d / (std * Math.sqrt(2 * Math.PI));

        var power = - ((x - mean) * (x - mean)) / (2 * std * std);

        return arg * Math.exp(power);
    }

    double normal(double x) {
        return normal(x, getMean(), getStdDev());
    }
}