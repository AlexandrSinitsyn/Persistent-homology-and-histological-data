package entropy;

public class three {
    int x;
    int y;
    int n;

    public three(int x, int y, int n) {
        this.x = x;
        this.y = y;
        this.n = n;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + n + "}";
    }
}
