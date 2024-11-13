package meowKai.CQuiS_backend.domain;

public enum ResponseStatus {
    EMOJI_SELECT, // 이모티콘 전달
    QUIZ_SELECT, // 공격팀 리더의 문제 중간 선택
    FINAL_SELECT, // 공격팀 리더의 문제 최종 선택
    DEF_QUIZ_SELECT, // 수비팀 리더의 답안 중간 선택
    ANSWER_SELECT, // 수비팀 리더의 답안 최종 선택
    ALL_SUBMIT, // 수비팀 팀원이 모두 답안을 제출
    ROUND_END, // 한 라운드가 종료
    GAME_END // 게임이 종료
}
