package lbtbirchi;

public class SplitAndCF {

    private boolean split;
    private CF cf;

    public SplitAndCF(boolean split, CF cf) {
        this.split = split;
        this.cf = cf;
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public CF getCf() {
        return cf;
    }

    public void setCf(CF cf) {
        this.cf = cf;
    }
}
