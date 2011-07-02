package org.rsna.geneva.hl7;

public class IHETransactionResponse {
    private int statusInt;
    private String statusString;

    public IHETransactionResponse( int i, String s) {
        statusInt = i;
        statusString = s;
    }

    public int getStatusInt() {
        return statusInt;
    }

    public String getStatusString() {
        return statusString;
    }
}
