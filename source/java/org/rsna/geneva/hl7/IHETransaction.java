package org.rsna.geneva.hl7;

public interface IHETransaction {

    public String getName();
    public IHETransactionResponse send();
}

