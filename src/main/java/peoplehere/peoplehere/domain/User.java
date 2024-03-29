package peoplehere.peoplehere.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import peoplehere.peoplehere.domain.enums.Gender;
import peoplehere.peoplehere.domain.enums.LoginType;
import peoplehere.peoplehere.domain.enums.Status;
import peoplehere.peoplehere.domain.util.BaseTimeEntity;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 50, nullable = false)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 30, nullable = false)
    private String firstName;

    @Column(length = 30, nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @Column(nullable = false)
    private boolean hasCreatedTour = false;

    @Column(nullable = false)
    private boolean marketingConsent;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserQuestion> userQuestions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private LocalDate birth;

    private String job;

    private String almaMater;

    private String hobby;

    private String pet;

    private String favourite;

    private String imageUrl;

    @Column(length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    // TODO: 문답에 대한 field 추가 예정
    // FIX: @Builder.Default 어노테이션 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLanguage> languages = new ArrayList<>();

    //투어 리더 입장에서 만든 투어 조회하기 위한 양방향 매핑. 여행객이 이용한 투어를 조회하려면 TourHistory테이블 조인해야한다.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Tour> tours = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Chat> chats = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TourHistory> tourHistories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserReview> userReviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wishlist> wishlists = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SearchHistory> searchHistories = new ArrayList<>();


    //TODO: 유저 권한 설정
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public void addLanguage(UserLanguage userLanguage) {
    }
}
