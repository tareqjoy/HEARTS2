package com.example.hearts;

public class GameInstanceVariable {
    private GameState gameState;
    private PlayerState playerState;
    private Card card;
    private PlayerPosition playerPosition;

    public GameInstanceVariable(GameState gameState, PlayerState playerState, Card card, PlayerPosition playerPosition) {
        this.gameState = gameState;
        this.playerState = playerState;
        this.card = card;
        this.playerPosition = playerPosition;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public PlayerPosition getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(PlayerPosition playerPosition) {
        this.playerPosition = playerPosition;
    }
}
