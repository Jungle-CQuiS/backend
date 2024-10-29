package meowKai.CQuiS_backend.domain;

/**
 * 게임의 상태를 나타냄,
 * ALL_READY 상태로 변경 후 5초간 유저에 의한 레디 취소가 발생하지 않으면
 * GAME_START 상태로 전환됨
 */
public enum GameEvent {
    WAITING, ALL_READY, STOP_READY, GAME_START, GAME_END
}
