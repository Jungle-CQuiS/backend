package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class RoomUser {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // gameRoom과 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    // user와 매핑
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 게임에 참가한 유저의 역할(방장, 일반 유저)
    @Column
    private RoomUserRole role;

    // 유저의 리더 여부
    @Column
    private Boolean isLeader;

    // 게임에 참가한 유저가 속한 팀(레드팀, 블루팀)
    @Column
    private RoomUserTeam team;

    // 현재 유저의 레디 여부
    @Column
    private Boolean isReady;

    /**
     * 엔티티 비즈니스 로직
     */

    // 방 참가하기
    public static RoomUser createRoomUser(GameRoom gameRoom, User user, RoomUserRole userRole, RoomUserTeam userTeam) {
        return RoomUser.builder()
                .gameRoom(gameRoom)
                .user(user)
                .role(userRole)
                .team(userTeam)
                .isLeader(false)
                .isReady(false)
                .build();
    }

    // team 바꾸기
    public void changeTeam() {
        if (isLeader) {
            throw new IllegalStateException("팀을 바꾸기 위해서는 리더 권한을 타인에게 위임해야 합니다.");
        }
        this.team = (this.team == RoomUserTeam.RED) ? RoomUserTeam.BLUE : RoomUserTeam.RED;
    }

    // role 바꾸기
    public void changeRole() {
        this.role = (this.role == RoomUserRole.HOST) ? RoomUserRole.GUEST : RoomUserRole.HOST;
    }

    // 리더 여부 바꾸기
    public void changeLeader() {
        this.isLeader = !this.isLeader;
    }

    // 레디하기/레디 취소하기
    public void changeReady() {
        this.isReady = !this.isReady;
    }
}