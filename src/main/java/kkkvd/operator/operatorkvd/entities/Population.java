package kkkvd.operator.operatorkvd.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "populations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"state_id", "year"})
})
@Getter
@Setter
@NoArgsConstructor
public class Population {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "count_all", nullable = false)
    private Integer countAll;
}
