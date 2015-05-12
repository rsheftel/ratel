package com.fftw.bloomberg.types;

public enum BBSecurityType {

    Unknown("Unknown"), CommonStock("Common Stock"), Futures("Future"), Mortgage("TBA"), Note("Note"), Forward(
        "Forward"), MutualFund("Mutual Fund"), WholeLoan("Whole Loan"), Abs("ABS"), Pool("Pool"), Bond("Bond"), Option(
        "Option"), SingleNameCDS("SINGLE NAME CDS"), DepositoryReceipt("Depository Receipt"), PartnershipShares(
        "Partnership Shares"), Warrant("Warrant"), Reit("REIT"), Unit("Unit"), Cmo("CMO"), NA("N.A."), Bill(
        "Bill");

    private String fileString;

    private BBSecurityType(String fileString) {
        this.fileString = fileString;
    }

    public String getFileString() {
        return fileString;
    }
    
    public static BBSecurityType fromString(String fileString) {

        if (fileString == null || fileString.trim().length() == 0) {
            return Unknown;
        }

        if (CommonStock.fileString.equalsIgnoreCase(fileString)) {
            return CommonStock;
        }

        if (Futures.fileString.equalsIgnoreCase(fileString)) {
            return Futures;
        }

        if (Mortgage.fileString.equalsIgnoreCase(fileString)) {
            return Mortgage;
        }

        if (Note.fileString.equalsIgnoreCase(fileString)) {
            return Note;
        }

        if (Forward.fileString.equalsIgnoreCase(fileString)) {
            return Forward;
        }

        if (MutualFund.fileString.equalsIgnoreCase(fileString)) {
            return MutualFund;
        }

        if (WholeLoan.fileString.equalsIgnoreCase(fileString)) {
            return WholeLoan;
        }

        if (Abs.fileString.equalsIgnoreCase(fileString)) {
            return Abs;
        }

        if (Pool.fileString.equalsIgnoreCase(fileString)) {
            return Pool;
        }

        if (Bond.fileString.equalsIgnoreCase(fileString)) {
            return Bond;
        }

        if (Option.fileString.equalsIgnoreCase(fileString)) {
            return Option;
        }

        if (SingleNameCDS.fileString.equalsIgnoreCase(fileString)) {
            return SingleNameCDS;
        }

        if (DepositoryReceipt.fileString.equalsIgnoreCase(fileString)) {
            return DepositoryReceipt;
        }

        if (PartnershipShares.fileString.equalsIgnoreCase(fileString)) {
            return PartnershipShares;
        }

        if (Warrant.fileString.equalsIgnoreCase(fileString)) {
            return Warrant;
        }

        if (Reit.fileString.equalsIgnoreCase(fileString)) {
            return Reit;
        }

        if (Unit.fileString.equalsIgnoreCase(fileString)) {
            return Unit;
        }

        if (Cmo.fileString.equalsIgnoreCase(fileString)) {
            return Cmo;
        }

        // Should be for equity securities
        if (NA.fileString.equalsIgnoreCase(fileString)) {
            return NA;
        }

        if (Bill.fileString.equalsIgnoreCase(fileString)) {
            return Bill;
        }

        // throw new IllegalArgumentException("No security type defined for '" + fileString + "'");
        System.err.println("No security type defined for '" + fileString + "'");
        return Unknown;
    }
}
