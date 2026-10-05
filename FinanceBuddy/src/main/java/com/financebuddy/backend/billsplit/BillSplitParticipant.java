package com.financebuddy.backend.billsplit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_split_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSplitParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_split_id", nullable = false)
    private BillSplit billSplit;

    @Column(name = "participant_name", nullable = false, length = 150)
    private String participantName;

    @Column(name = "share_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal shareAmount;

    @ManyToMany(mappedBy = "assignedParticipants")
    @Builder.Default
    private List<BillSplitItem> assignedItems = new ArrayList<>();
}
