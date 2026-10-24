import java.io.Serializable;

class Location implements Serializable {
    private static final long serialVersionUID = 1L;
    private final District district;
    private final String place;

    Location(District district, String place) {
        this.district = district;
        this.place = place;
    }

    public District getDistrict() { return district; }
    public String getPlace() { return place; }

    @Override public String toString() { return district + " - " + place; }
}
