package activity;

class Time {

    static String time(double seconds) {
        int s = (int) seconds;
        int m = 0;
        int h = 0;

        while (s >= 60) {
            s -= 60;

            m++;
        }

        while (m >= 60) {
            m -= 60;

            h++;
        }



        var str = "";

        if (h > 0) {
            if (h == 1)
                str = "1 hour";
            else
                str = h + " hours";


            if (m == 1)
                str += " 1 minute";
            else if (m > 1)
                str += " " + m + " minutes";
        } else if (m > 0) {
            if (m == 1)
                str += " 1 minute";
            else
                str += " " + m + " minutes";


            if (s == 1)
                str += " 1 second";
            else if (s > 1)
                str += " " + s + " seconds";
        } else if (s == 1)
            str = "1 second";
        else
            str = s + " seconds";

        return str;
    }

    static String time(double seconds, boolean everything) {
        int s = (int) seconds;
        int m = 0;
        int h = 0;

        while (s >= 60) {
            s -= 60;

            m++;
        }

        while (m >= 60) {
            m -= 60;

            h++;
        }


        if (everything) {
            var str = "";

            if (h > 0) {
                if (h == 1)
                    str = "1 hour ";
                else
                    str = h + " hours ";
            }
            if (m > 0) {
                if (m == 1)
                    str += "1 minute ";
                else
                    str += " " + m + " minutes ";
            }
            if (s > 0) {
                if (s == 1)
                    str += "1 second";
                else
                    str += s + " seconds";
            }

            return str;
        } else
            return time(seconds);
    }
}
