package per.op;

/**
 * Created by AlphaGo on 2017/12/29.
 */

public class Program {
    private String NAME,PROCESS_NAME;

    public Program(String NAME, String PROCESS_NAME) {
        this.NAME = NAME;
        this.PROCESS_NAME = PROCESS_NAME;
    }

    public Program() {
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public void setPROCESS_NAME(String PROCESS_NAME) {
        this.PROCESS_NAME = PROCESS_NAME;
    }

    public String getNAME() {
        return NAME;
    }

    public String getPROCESS_NAME() {
        return PROCESS_NAME;
    }
}
