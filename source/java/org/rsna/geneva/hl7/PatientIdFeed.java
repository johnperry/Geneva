package org.rsna.geneva.hl7;

public interface PatientIdFeed extends IHETransaction {
    public abstract void setAssigningAuthority( String s);
    public abstract void setPatientId( String s);
}
