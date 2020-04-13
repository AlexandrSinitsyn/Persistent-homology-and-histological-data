package entropy;

public class pair<I, J> {
    I i;
    J j;

    pair(I i, J j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public String toString() {
        return "{" + i.toString() + ", " + j.toString() + "}";
    }
}
