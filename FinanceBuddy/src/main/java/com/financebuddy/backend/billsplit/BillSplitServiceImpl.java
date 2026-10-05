package com.financebuddy.backend.billsplit;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillSplitServiceImpl implements BillSplitService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final BillSplitRepository billSplitRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BillSplitResponse createBillSplit(BillSplitRequest request) {
        User user = getCurrentUser();
        BillSplit billSplit = buildBillSplit(request, user);
        return mapToResponse(billSplitRepository.saveAndFlush(billSplit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillSplitResponse> getAllBillSplits() {
        User user = getCurrentUser();
        return billSplitRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BillSplitResponse getBillSplitById(Long id) {
        return mapToResponse(getOwnedBillSplit(id, getCurrentUser()));
    }

    @Override
    @Transactional
    public BillSplitResponse updateBillSplit(Long id, BillSplitRequest request) {
        BillSplit billSplit = getOwnedBillSplitForUpdate(id, getCurrentUser());
        applyRequest(billSplit, request);
        billSplit.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(billSplitRepository.saveAndFlush(billSplit));
    }

    @Override
    @Transactional
    public void deleteBillSplit(Long id) {
        billSplitRepository.delete(getOwnedBillSplit(id, getCurrentUser()));
    }

    private BillSplit buildBillSplit(BillSplitRequest request, User user) {
        BillSplit billSplit = BillSplit.builder()
                .user(user)
                .build();
        applyRequest(billSplit, request);
        return billSplit;
    }

    private void applyRequest(BillSplit billSplit, BillSplitRequest request) {
        List<String> participantNames = normalizedParticipantNames(request);
        List<ItemRequest> itemRequests = request.getItems() == null ? List.of() : request.getItems();
        validateRequest(request, itemRequests);

        BigDecimal subtotal = itemRequests.isEmpty()
                ? money(request.getSubtotal())
                : calculateItemSubtotal(itemRequests);
        BigDecimal taxAmount = optionalMoney(request.getTaxAmount());
        BigDecimal tipAmount = optionalMoney(request.getTipAmount());
        BigDecimal totalAmount = subtotal.add(taxAmount).add(tipAmount).setScale(2, RoundingMode.HALF_UP);

        billSplit.setTitle(request.getTitle().trim());
        billSplit.setDescription(blankToNull(request.getDescription()));
        billSplit.setBillDate(request.getBillDate());
        billSplit.setSubtotal(subtotal);
        billSplit.setTaxAmount(taxAmount);
        billSplit.setTipAmount(tipAmount);
        billSplit.setTotalAmount(totalAmount);
        billSplit.setSplitType(request.getSplitType());

        List<BillSplitParticipant> participants = new ArrayList<>();
        billSplit.getParticipants().clear();
        for (String participantName : participantNames) {
            BillSplitParticipant participant = BillSplitParticipant.builder()
                    .billSplit(billSplit)
                    .participantName(participantName)
                    .shareAmount(ZERO)
                    .build();
            participants.add(participant);
            billSplit.getParticipants().add(participant);
        }

        billSplit.getItems().clear();
        for (ItemRequest itemRequest : itemRequests) {
            List<BillSplitParticipant> assigned = assignedParticipants(
                    itemRequest,
                    participants,
                    request.getSplitType()
            );
            BigDecimal quantity = itemRequest.getQuantity();
            BigDecimal unitPrice = money(itemRequest.getUnitPrice());
            BigDecimal amount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            BillSplitItem item = BillSplitItem.builder()
                    .billSplit(billSplit)
                    .itemName(itemRequest.getItemName().trim())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .amount(amount)
                    .assignedParticipants(assigned)
                    .build();
            billSplit.getItems().add(item);
        }

        if (request.getSplitType() == SplitType.CUSTOM) {
            calculateCustomShares(billSplit, participants);
        } else {
            applyEqualShares(totalAmount, participants);
        }

        validateCalculatedTotals(billSplit);
    }

    private void validateRequest(BillSplitRequest request, List<ItemRequest> itemRequests) {
        if (request.getSplitType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Split type is required.");
        }

        if (request.getSplitType() == SplitType.CUSTOM && itemRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom splits require at least one item.");
        }

        BigDecimal subtotal = money(request.getSubtotal());
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtotal must be greater than zero.");
        }

        if (request.getTaxAmount() != null && request.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax amount must not be negative.");
        }
        if (request.getTipAmount() != null && request.getTipAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tip amount must not be negative.");
        }

        if (!itemRequests.isEmpty()) {
            for (ItemRequest itemRequest : itemRequests) {
                validateItem(itemRequest, request.getSplitType());
            }

            BigDecimal itemTotal = calculateItemSubtotal(itemRequests);
            if (itemTotal.compareTo(subtotal) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtotal must equal the sum of item totals.");
            }
        }
    }

    private void validateItem(ItemRequest itemRequest, SplitType splitType) {
        if (itemRequest.getQuantity() == null || itemRequest.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item quantity must be greater than zero.");
        }
        if (itemRequest.getUnitPrice() == null || itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item unit price must not be negative.");
        }
        if (itemRequest.getItemName() == null || itemRequest.getItemName().trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name is required.");
        }
        if (splitType == SplitType.CUSTOM
                && (itemRequest.getParticipantNames() == null || itemRequest.getParticipantNames().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Every item must have at least one participant.");
        }
    }

    private List<BillSplitParticipant> assignedParticipants(
            ItemRequest request,
            List<BillSplitParticipant> participants,
            SplitType splitType
    ) {
        if (splitType != SplitType.CUSTOM) {
            return new ArrayList<>();
        }

        Map<String, BillSplitParticipant> byName = participants.stream()
                .collect(Collectors.toMap(
                        participant -> participant.getParticipantName().toLowerCase(Locale.ROOT),
                        participant -> participant,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        Set<BillSplitParticipant> selected = new LinkedHashSet<>();
        for (String name : request.getParticipantNames()) {
            String normalizedName = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
            BillSplitParticipant participant = byName.get(normalizedName);
            if (participant == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Item assignment must use an existing participant."
                );
            }
            selected.add(participant);
        }

        if (selected.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Every item must have at least one participant.");
        }

        return new ArrayList<>(selected);
    }

    private void calculateCustomShares(BillSplit billSplit, List<BillSplitParticipant> participants) {
        for (BillSplitParticipant participant : participants) {
            participant.setShareAmount(ZERO);
        }

        for (BillSplitItem item : billSplit.getItems()) {
            List<BillSplitParticipant> assigned = item.getAssignedParticipants();
            List<BigDecimal> shares = calculateEqualShares(item.getAmount(), assigned);
            for (int index = 0; index < assigned.size(); index++) {
                BillSplitParticipant participant = assigned.get(index);
                participant.setShareAmount(participant.getShareAmount().add(shares.get(index)).setScale(2, RoundingMode.UNNECESSARY));
            }
        }

        BigDecimal surcharge = billSplit.getTaxAmount().add(billSplit.getTipAmount());
        List<BigDecimal> surchargeShares = calculateEqualShares(surcharge, participants);
        for (int index = 0; index < participants.size(); index++) {
            participants.get(index).setShareAmount(
                    participants.get(index).getShareAmount().add(surchargeShares.get(index)).setScale(2, RoundingMode.UNNECESSARY)
            );
        }
    }

    private void applyEqualShares(BigDecimal totalAmount, List<BillSplitParticipant> participants) {
        List<BigDecimal> shares = calculateEqualShares(totalAmount, participants);
        for (int index = 0; index < participants.size(); index++) {
            participants.get(index).setShareAmount(shares.get(index));
        }
    }

    private List<BigDecimal> calculateEqualShares(BigDecimal totalAmount, List<BillSplitParticipant> participants) {
        return distributeEqually(totalAmount, participants.size());
    }

    private List<BigDecimal> distributeEqually(BigDecimal totalAmount, int participantCount) {
        if (participantCount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one participant is required.");
        }

        BigInteger cents = totalAmount.movePointRight(2).toBigIntegerExact();
        BigInteger[] quotientAndRemainder = cents.divideAndRemainder(BigInteger.valueOf(participantCount));
        BigInteger baseCents = quotientAndRemainder[0];
        int remainderCents = quotientAndRemainder[1].intValueExact();

        return java.util.stream.IntStream.range(0, participantCount)
                .mapToObj(index -> new BigDecimal(
                        baseCents.add(index < remainderCents ? BigInteger.ONE : BigInteger.ZERO)
                ).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY))
                .toList();
    }

    private BigDecimal calculateItemSubtotal(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(item -> item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP))
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateCalculatedTotals(BillSplit billSplit) {
        BigDecimal itemTotal = billSplit.getItems().stream()
                .map(BillSplitItem::getAmount)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (!billSplit.getItems().isEmpty() && itemTotal.compareTo(billSplit.getSubtotal()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtotal must equal the sum of item totals.");
        }

        BigDecimal participantTotal = billSplit.getParticipants().stream()
                .map(BillSplitParticipant::getShareAmount)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (participantTotal.compareTo(billSplit.getTotalAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant shares must equal the bill total.");
        }
    }

    private List<String> normalizedParticipantNames(BillSplitRequest request) {
        if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one participant is required.");
        }

        Set<String> uniqueNames = new HashSet<>();
        List<String> names = request.getParticipants().stream()
                .map(ParticipantRequest::getParticipantName)
                .map(name -> name == null ? "" : name.trim())
                .toList();

        for (String name : names) {
            if (name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant name is required.");
            }
            if (!uniqueNames.add(name.toLowerCase(Locale.ROOT))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant names must be unique.");
            }
        }
        return names;
    }

    private BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtotal is required.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal optionalMoney(BigDecimal amount) {
        return amount == null ? ZERO : amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BillSplit getOwnedBillSplit(Long id, User user) {
        return billSplitRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill split not found."));
    }

    private BillSplit getOwnedBillSplitForUpdate(Long id, User user) {
        return billSplitRepository.findForUpdateByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill split not found."));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private BillSplitResponse mapToResponse(BillSplit billSplit) {
        return BillSplitResponse.builder()
                .id(billSplit.getId())
                .title(billSplit.getTitle())
                .description(billSplit.getDescription())
                .billDate(billSplit.getBillDate())
                .subtotal(billSplit.getSubtotal())
                .taxAmount(billSplit.getTaxAmount())
                .tipAmount(billSplit.getTipAmount())
                .totalAmount(billSplit.getTotalAmount())
                .splitType(billSplit.getSplitType())
                .participants(billSplit.getParticipants().stream()
                        .map(participant -> ParticipantResponse.builder()
                                .id(participant.getId())
                                .participantName(participant.getParticipantName())
                                .shareAmount(participant.getShareAmount())
                                .build())
                        .toList())
                .items(billSplit.getItems().stream()
                        .map(item -> ItemResponse.builder()
                                .id(item.getId())
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .amount(item.getAmount())
                                .participantNames(item.getAssignedParticipants().stream()
                                        .map(BillSplitParticipant::getParticipantName)
                                        .toList())
                                .build())
                        .toList())
                .createdAt(billSplit.getCreatedAt())
                .updatedAt(billSplit.getUpdatedAt())
                .build();
    }
}
