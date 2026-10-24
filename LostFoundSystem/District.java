enum District {
    DOWNTOWN("Downtown"), NORTH("North District"), SOUTH("South District"),
    EAST("East District"), WEST("West District"), HARBOR("Harbor & Port"),
    AIRPORT("Airport"), UNIVERSITY("University Area"), MARKET("Public Market Area"),
    TRANSIT("Transit Hub / Terminals"), PARKS("Parks & Recreation"), MALLS("Shopping Malls");

    private final String label;
    District(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
