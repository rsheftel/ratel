package com.fftw.bloomberg.types;

public enum BBFuturesCategory {
    Unknown("Unknown"), Currency("CURRENCIES"), BondFutures("BOND FUTURES"), InterestRate("INTEREST RATE"), StockIndex(
        "STOCK INDICES"), Energies("OIL/GAS/ELECTRICITY"), Livestock("LIVESTOCK"), Foodstuffs("FOODSTUFFS"), Grains(
        "CROPS/GRAINS"), Metal("METAL"), IndustrialMaterial("INDUSTRIAL MATERIAL"), Fibers("FIBERS");

    private String fileString;

    private BBFuturesCategory(String fileString) {
        this.fileString = fileString;
    }

    public static BBFuturesCategory fromString(String fileString) {

        if (fileString == null || fileString.trim().length() == 0) {
            return Unknown;
        }

        if (Currency.fileString.equalsIgnoreCase(fileString)) {
            return Currency;
        }

        if (BondFutures.fileString.equalsIgnoreCase(fileString)) {
            return BondFutures;
        }

        if (InterestRate.fileString.equalsIgnoreCase(fileString)) {
            return InterestRate;
        }

        if (StockIndex.fileString.equalsIgnoreCase(fileString)) {
            return StockIndex;
        }

        if (Energies.fileString.equalsIgnoreCase(fileString)) {
            return Energies;
        }

        if (Livestock.fileString.equalsIgnoreCase(fileString)) {
            return Livestock;
        }

        if (Foodstuffs.fileString.equalsIgnoreCase(fileString)) {
            return Foodstuffs;
        }

        if (Grains.fileString.equalsIgnoreCase(fileString)) {
            return Grains;
        }

        if (Metal.fileString.equalsIgnoreCase(fileString)) {
            return Metal;
        }

        if (IndustrialMaterial.fileString.equalsIgnoreCase(fileString)) {
            return IndustrialMaterial;
        }

        if (Fibers.fileString.equalsIgnoreCase(fileString)) {
            return Fibers;
        }

        // throw new IllegalArgumentException("No futures category defined for '" + fileString + "'");
        System.err.println(("No futures category defined for '" + fileString + "'"));
        return Unknown;
    }

}
