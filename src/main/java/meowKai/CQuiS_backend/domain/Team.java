package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 팀이 속해있는 방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    // 공격팀, 수비팀
    @Enumerated(value = STRING)
    private TeamStatus teamStatus;

    // RED, BLUE
    @Enumerated(value = STRING)
    private RoomUserTeam teamColor;

    // 팀 체력
    @Column
    private Integer teamHp;

    // 맞춘 문제 수
    @Column
    private Integer correctCount;

    // 팀 생성
    public static Team createTeam(GameRoom gameRoom, RoomUserTeam teamColor) {
        return Team.builder()
                .gameRoom(gameRoom)
                .teamColor(teamColor)
                .teamHp(3)
                .correctCount(0)
                .build();
    }

    //  수비 팀이 방어에 실패하면 체력 감소
    public void decreaseHp() {
        this.teamHp--;
    }

    // teamStatus를 변경
    public void changeTeamStatus(TeamStatus teamStatus) {
        this.teamStatus = teamStatus;
    }

    // 문제를 맞춤
    public void addCorrectCount() {
        correctCount++;
    }
}
