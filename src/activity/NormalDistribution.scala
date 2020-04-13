package activity

class NormalDistribution(data: Array[Double]) {

    def getMean: Double = {
        var sum = 0d

        for (a: Double <- data) sum += a

        sum / data.length
    }

    def getStdDev: Double = {
        val mean = getMean
        var sum = 0d

        for (a <- data) sum += a * a

        sum /= data.length

        Math.sqrt(sum - mean * mean)
    }

    private def normal(x: Double, mean: Double, std: Double): Double = {
        val arg = 1d / (std * Math.sqrt(2 * Math.PI))

        val power = -(x - mean) * (x - mean) / (2 * std * std)

        arg * Math.exp(power)
    }

    private[activity] def normal(x: Double): Double = normal(x, getMean, getStdDev)
}