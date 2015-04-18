package capstat.model;

/**
 * @author Christian Persson
 */
public class ChalmersAge implements Comparable<ChalmersAge> {

    private Birthday birthday;
    private Admittance admittance;

    /**
     * Creates a ChalmersAge instance.
     * @param birthday the Birthday instance for this ChalmersAge
     * @param admittance the Admittance instance for this ChalmersAge
     */
    public ChalmersAge(Birthday birthday, Admittance admittance) {
        this.birthday = birthday;
        this.admittance = admittance;
    }

    /**
     * Creates a new ChalmersAge instance, as a copy of the given ChalmersAge instance.
     * @param chalmersAge the ChalmersAge instance to create a copy of
     */
    public ChalmersAge(ChalmersAge chalmersAge) {
        this.birthday = chalmersAge.getBirthday();
        this.admittance = chalmersAge.getAdmittance();
    }

    /**
     * Returns the birthday of the ChalmersAge.
     * @return the birthday of the ChalmersAge
     */
    public Birthday getBirthday() {
        return new Birthday(this.birthday);
    }

    /**
     * Returns the admittance of the ChalmersAge.
     * @return the admittance of the ChalmersAge
     */
    public Admittance getAdmittance() {
        return new Admittance(this.admittance);
    }

    @Override
    public int compareTo(ChalmersAge other) {
        Birthday otherBirthday = other.getBirthday();
        int birthdayCompare = this.birthday.compareTo(otherBirthday);
        if (birthdayCompare != 0) return birthdayCompare;

        Admittance otherAdmittance = other.getAdmittance();
        int admittanceCompare = this.admittance.compareTo(otherAdmittance);
        if (admittanceCompare != 0) return admittanceCompare;

        return 0;
    }
}
