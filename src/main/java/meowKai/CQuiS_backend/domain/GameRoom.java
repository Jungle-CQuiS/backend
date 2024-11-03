package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import java.util.List;
import java.util.Random;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;
import static meowKai.CQuiS_backend.domain.TeamStatus.DEFENSE;
import static meowKai.CQuiS_backend.domain.TeamStatus.OFFENSE;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class GameRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 방에 참가한 유저 목록
    @OneToMany(mappedBy = "gameRoom")
    private List<RoomUser> roomUsers;

    // 방의 팀 목록
    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL)
    private List<Team> teams;

    // 방 제목
    @Column
    private String name;

    // 현재 방 인원 수
    @Column
    private Integer currentUsers;

    // 방 최대 인원 수
    @Column
    private Integer maxUsers;

    // 방 비밀번호
    @Column
    private Integer password;

    @Enumerated(value = STRING)
    private GameStatus gameStatus;

    // 현재 선택된 퀴즈의 id
    @Column
    private Long currentQuizId;

    /**
     * 엔티티 비즈니스 로직
     */

    // 방 생성
    public static GameRoom createGameRoom(String name, Integer maxUsers, Integer password) {
        return GameRoom.builder()
                .name(name)
                .currentUsers(0)
                .maxUsers(maxUsers)
                .password(password)
                .gameStatus(GameStatus.WAITING)
                .build();
    }

    // 방 인원 추가
    public void addUser() {
        this.currentUsers++;
    }

    // 방 인원 감소
    public void removeUser() {
        this.currentUsers--;
    }

    // 게임 상태 전환
    public void changeGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    // 랜덤으로 공격, 수비 팀을 정하고 공격팀을 반환
    public Team assignRandomTeamStatus() {
        Random random = new Random();
        int firstOffense = random.nextInt(2);
        teams.get(firstOffense).changeTeamStatus(OFFENSE);
        teams.get(firstOffense ^ 1).changeTeamStatus(DEFENSE);

        return teams.get(firstOffense);
    }

    // 현재 선택된 퀴즈를 gameRoom에 저장해 둠
    public void saveCurrentQuizId(Long quizId) {
        this.currentQuizId = quizId;
    }
}