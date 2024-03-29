package peoplehere.peoplehere.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import peoplehere.peoplehere.domain.util.BaseTimeEntity;

@Entity
@Getter
public class UserLanguage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_language_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @ColumnDefault("'일반'")
    private String status = "일반";

    //==연관관계 편의 메서드==//
    public void setUser(User user) {
        this.user = user;
        user.getLanguages().add(this);
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
