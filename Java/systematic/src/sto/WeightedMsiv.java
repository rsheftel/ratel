package sto;

import static systemdb.metadata.MsivTable.*;

import java.io.*;

import systemdb.metadata.*;
import db.*;
import db.columns.*;

public class WeightedMsiv implements Serializable {
    private static final long serialVersionUID = 1L;
    private String msiv;
	private double weight;

	public WeightedMsiv(String line) {
		String[] parts = line.split(",");
		msiv = parts[0].replaceAll("\"", "");
		weight = Double.parseDouble(line.split(",")[1]);
	}
	
	public WeightedMsiv(String msiv, double weight) {
        this.msiv = msiv;
        this.weight = weight;
	}

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((msiv == null) ? 0 : msiv.hashCode());
        long temp;
        temp = Double.doubleToLongBits(weight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final WeightedMsiv other = (WeightedMsiv) obj;
        if (msiv == null) {
            if (other.msiv != null) return false;
        } else if (!msiv.equals(other.msiv)) return false;
        if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight)) return false;
        return true;
    }

    public String msiv() {
		return msiv;
	}

	public double weight() {
		return weight;
	}

	public String sivString() {
		return msiv.replaceAll("_[^_]*$", "");
	}

    public Cell<?> cell(NvarcharColumn nameCol) {
        return nameCol.with(msiv);
    }

    public Cell<?> cell(FloatColumn weightCol) {
        return weightCol.with(weight);
    }

    public String name() {
        return msiv;
    }

    public Siv siv() {
        return MSIVS.forName(msiv).siv();
    }
}