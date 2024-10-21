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

    // 게임에 참가한 유저가 속한 팀(레드팀, 블루팀)
    @Column
    private RoomUserTeam team;

    // TODO: 리더 필드도 추가해야함.

    /**
     * 엔티티 비즈니스 로직
     */

    // 방 생성하기(방을 생성한 사람이 방장이 됨)
    public static RoomUser createRoomUser(GameRoom gameRoom, User user) {
        return RoomUser.builder()
                .gameRoom(gameRoom)
                .user(user)
                .role(RoomUserRole.HOST)
                .team(RoomUserTeam.RED)
                .build();
    }

    // team 바꾸기
    public void changeTeam() {
        this.team = (this.team == RoomUserTeam.RED) ? RoomUserTeam.BLUE : RoomUserTeam.RED;
    }

    // role 바꾸기
    public void changeRole() {
        this.role = (this.role == RoomUserRole.HOST) ? RoomUserRole.GUEST : RoomUserRole.HOST;
    }
}