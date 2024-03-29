package peoplehere.peoplehere.controller.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import peoplehere.peoplehere.controller.dto.jwt.JwtTokenResponse;

@Getter
@AllArgsConstructor
public class PostLoginResponse {
    private long userId;
    private JwtTokenResponse jwtToken;
}
