package capstat.model.user;

import java.time.Year;

/**
 * Immutable value object.
 *
 * @author Christian Persson
 */
public class Admittance implements Comparable<Admittance> {

    private final Year year;
    private final Admittance.Period readingPeriod;

    /**
     * Creates a new Admittance instance.
     * @param year the admittance year
     * @param readingPeriod a number from 1 to 4, denoting which reading period the person was admitted to/started studying at Chalmers
     */
    public Admittance(Year year, Admittance.Period readingPeriod) {
        this.year = year;
        this.readingPeriod = readingPeriod;
    }

    /**
     * Creates a new Admittance instance, as a copy of the given Admittance instance.
     * @param admittance the Admittance instance to create a copy of
     */
    public Admittance(Admittance admittance) {
        this.year = admittance.getYear();
        this.readingPeriod = admittance.getReadingPeriod();
    }

    /**
     * Returns the admittance year.
     * @return the admittance year
     */
    public Year getYear() {
        return this.year;
    }

    /**
     *
     * @return the year of this admittance represented by an integer.
     */
    public int getYearValue() {
        return this.year.getValue();
    }

    /**
     * Returns the admittance reading period.
     * @return the admittance reading period
     */
    public Admittance.Period getReadingPeriod() {
        return this.readingPeriod;
    }

    public int getReadingPeridoValue() {
        //The reading periods go from 1 to 4, but are zero-indexed.
        // Therefore, we add 1.
        return this.readingPeriod.ordinal() + 1;
    }

    @Override
    public int compareTo(Admittance other) {
        Year otherYear = other.getYear();
        if (!this.year.equals(otherYear)) return this.year.compareTo
                (otherYear);

        Admittance.Period otherReadingPeriod = other.getReadingPeriod();
        return this.readingPeriod.compareTo(otherReadingPeriod);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Admittance bd = (Admittance) o;

        if (!(this.year.equals(bd.getYear()))) return false;
        if (!(this.readingPeriod == bd.getReadingPeriod())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year.hashCode();
        result = 31 * result + readingPeriod.hashCode();
        return result;
    }

    public enum Period {
        ONE, TWO, THREE, FOUR
    }
}
