package meowKai.CQuiS_backend.dto;

import meowKai.CQuiS_backend.domain.RoomUserTeam;
import meowKai.CQuiS_backend.dto.response.ResponseSelectQuizDto;

/**
 * Service -> Controller로
 * responseDto와 defenseTeamColor를 한꺼번에 전달하기 위한 record
 */
public record SelectQuizResult<T>(
        T responseDto,
        RoomUserTeam defenseTeamColor
) {}
