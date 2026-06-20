package pl.coffeechess.game.model.enums;

// poziomy trudności bota mapowane na głębokość i siłę silnika
public enum BotDifficulty {
    EASY(4, 5),
    MEDIUM(8, 12),
    HARD(12, 20);

    private final int depth;
    private final int skill;

    BotDifficulty(int depth, int skill) {
        this.depth = depth;
        this.skill = skill;
    }

    public int getDepth() {
        return depth;
    }

    public int getSkill() {
        return skill;
    }
}
