package pl.coffeechess.game_service.model.domain;

public record Position(int x, int y) {
    public boolean isValid() {
        return (x >= 0 && x <= 7) && (y >= 0 && y <= 7);
    }
}
