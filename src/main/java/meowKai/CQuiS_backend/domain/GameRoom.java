package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import java.util.List;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

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

    /**
     * 엔티티 비즈니스 로직
     */

    // 방 생성
    public static GameRoom createGameRoom(String name, Integer maxUsers, Integer password) {
        return GameRoom.builder()
                .name(name)
                .currentUsers(1)
                .maxUsers(maxUsers)
                .password(password)
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
}