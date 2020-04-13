package activity

object Time {

    private def getTime(seconds: Double): (Int, Int, Int) = {
        var s = seconds.toInt
        var m = 0
        var h = 0

        while (s >= 60) {
            s -= 60
            m += 1
        }
        while (m >= 60) {
            m -= 60
            h += 1
        }

        (h, m, s)
    }

    def time(seconds: Double): String = {
        val (h, m, s) = getTime(seconds)

        var str = ""

        if (h > 0) {
            str = s"$h hour${if (h != 1) "s"}"
            if (m > 0) str += s" $m minute${if (m != 1) "s"}"
        }
        else if (m > 0) {
            str = s"$m minute${if (m != 1) "s"}"
            if (s > 0) str += s" $s second${if (s != 1) "s"}"
        }
        else str = s"$s second${if (s != 1) "s"}"

        str
    }

    def time(seconds: Double, everything: Boolean): String = {
        val (h, m, s) = getTime(seconds)

        if (everything) {
            var str = ""
            if (h > 0) str = s"$h hour${if (h != 1)"s"} "
            if (m > 0) str += s"$m minute${if (m != 1)"s"} "
            if (s > 0) str += s"$s second${if (s != 1)"s"} "
            str
        }
        else time(seconds)
    }
}
