class MatchResult {
    private final Item item;
    private final int score;

    MatchResult(Item item, int score) {
        this.item = item;
        this.score = score;
    }

    public Item getItem() { return item; }
    public int getScore() { return score; }
}
