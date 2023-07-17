package cl.tbk.msk.producer;

public enum SuccessResponse {
    OK("ok"),
    NOK("nok");

    private final String value;

    SuccessResponse(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}