package com.financebuddy.backend.billsplit;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillSplitServiceImplTest {

    @Mock
    private BillSplitRepository billSplitRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BillSplitServiceImpl billSplitService;

    private User user;

    @BeforeEach
    void setUpAuthentication() {
        user = User.builder().id(17L).email("splitter@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        lenient().when(billSplitRepository.saveAndFlush(any(BillSplit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void equalSplitWithTwoParticipantsIsCalculatedExactly() {
        BillSplitResponse response = billSplitService.createBillSplit(equalRequest(
                "Dinner", "100.00", "Alice", "Bob"
        ));

        assertEquals(new BigDecimal("100.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("50.00"), response.getParticipants().get(0).getShareAmount());
        assertEquals(new BigDecimal("50.00"), response.getParticipants().get(1).getShareAmount());
        assertEquals(response.getTotalAmount(), sumShares(response));
    }

    @Test
    void equalSplitWithThreeParticipantsAllocatesRemainderToFirstParticipants() {
        BillSplitResponse response = billSplitService.createBillSplit(equalRequest(
                "Lunch", "100.00", "Alice", "Bob", "Cara"
        ));

        assertEquals(new BigDecimal("33.34"), response.getParticipants().get(0).getShareAmount());
        assertEquals(new BigDecimal("33.33"), response.getParticipants().get(1).getShareAmount());
        assertEquals(new BigDecimal("33.33"), response.getParticipants().get(2).getShareAmount());
        assertEquals(response.getTotalAmount(), sumShares(response));
    }

    @Test
    void singleParticipantPaysTheEntireBill() {
        BillSplitResponse response = billSplitService.createBillSplit(equalRequest(
                "Solo", "500.00", "Alice"
        ));

        assertEquals(new BigDecimal("500.00"), response.getParticipants().get(0).getShareAmount());
        assertEquals(response.getTotalAmount(), sumShares(response));
    }

    @Test
    void taxAndTipAreIncludedInTheServerCalculatedTotal() {
        BillSplitResponse response = billSplitService.createBillSplit(BillSplitRequest.builder()
                .title("Dinner")
                .subtotal(new BigDecimal("80.00"))
                .taxAmount(new BigDecimal("8.00"))
                .tipAmount(new BigDecimal("12.00"))
                .splitType(SplitType.EQUAL)
                .participants(List.of(
                        ParticipantRequest.builder().participantName("Alice").build(),
                        ParticipantRequest.builder().participantName("Bob").build()
                ))
                .build());

        assertEquals(new BigDecimal("100.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("50.00"), response.getParticipants().get(0).getShareAmount());
        assertEquals(new BigDecimal("50.00"), response.getParticipants().get(1).getShareAmount());
    }

    @Test
    void customSplitUsesAssignedItemsAndBalancesToBillTotal() {
        BillSplitResponse response = billSplitService.createBillSplit(BillSplitRequest.builder()
                .title("Dinner")
                .subtotal(new BigDecimal("1200.00"))
                .splitType(SplitType.CUSTOM)
                .participants(List.of(
                        ParticipantRequest.builder().participantName("Alice").build(),
                        ParticipantRequest.builder().participantName("Bob").build(),
                        ParticipantRequest.builder().participantName("Cara").build()
                ))
                .items(List.of(
                        item("Pizza", "2", "300.00", "Alice", "Bob"),
                        item("Drinks", "3", "100.00", "Alice", "Bob", "Cara"),
                        item("Dessert", "1", "300.00", "Cara")
                ))
                .build());

        assertEquals(new BigDecimal("1200.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("400.00"), response.getParticipants().get(0).getShareAmount());
        assertEquals(new BigDecimal("400.00"), response.getParticipants().get(1).getShareAmount());
        assertEquals(new BigDecimal("400.00"), response.getParticipants().get(2).getShareAmount());
        assertEquals(response.getTotalAmount(), sumShares(response));
    }

    @Test
    void customSplitDistributesRemainderDeterministicallyAcrossAssignedParticipants() {
        BillSplitResponse response = billSplitService.createBillSplit(BillSplitRequest.builder()
                .title("Dessert")
                .subtotal(new BigDecimal("100.00"))
                .splitType(SplitType.CUSTOM)
                .participants(List.of(
                        ParticipantRequest.builder().participantName("Alice").build(),
                        ParticipantRequest.builder().participantName("Bob").build(),
                        ParticipantRequest.builder().participantName("Cara").build()
                ))
                .items(List.of(item("Cake", "1", "100.00", "Alice", "Bob", "Cara")))
                .build());

        assertEquals(new BigDecimal("33.34"), response.getParticipants().get(0).getShareAmount());
        assertEquals(new BigDecimal("33.33"), response.getParticipants().get(1).getShareAmount());
        assertEquals(new BigDecimal("33.33"), response.getParticipants().get(2).getShareAmount());
        assertEquals(response.getTotalAmount(), sumShares(response));
    }

    @Test
    void customItemWithoutParticipantsIsRejected() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> billSplitService.createBillSplit(BillSplitRequest.builder()
                        .title("Invalid")
                        .subtotal(new BigDecimal("10.00"))
                        .splitType(SplitType.CUSTOM)
                        .participants(List.of(
                                ParticipantRequest.builder().participantName("Alice").build(),
                                ParticipantRequest.builder().participantName("Bob").build()
                        ))
                        .items(List.of(item("Pizza", "1", "10.00")))
                        .build())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(billSplitRepository, never()).saveAndFlush(any());
    }

    @Test
    void subtotalMismatchIsRejected() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> billSplitService.createBillSplit(BillSplitRequest.builder()
                        .title("Invalid")
                        .subtotal(new BigDecimal("11.00"))
                        .splitType(SplitType.CUSTOM)
                        .participants(List.of(
                                ParticipantRequest.builder().participantName("Alice").build(),
                                ParticipantRequest.builder().participantName("Bob").build()
                        ))
                        .items(List.of(item("Pizza", "1", "10.00", "Alice")))
                        .build())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Subtotal must equal the sum of item totals.", exception.getReason());
    }

    @Test
    void duplicateParticipantNamesAreRejectedCaseInsensitively() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> billSplitService.createBillSplit(BillSplitRequest.builder()
                        .title("Invalid")
                        .subtotal(new BigDecimal("10.00"))
                        .splitType(SplitType.EQUAL)
                        .participants(List.of(
                                ParticipantRequest.builder().participantName("Alice").build(),
                                ParticipantRequest.builder().participantName(" alice ").build()
                        ))
                        .build())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void retrievalUsesTheAuthenticatedUserAsOwnershipScope() {
        BillSplit billSplit = BillSplit.builder()
                .id(9L)
                .user(user)
                .title("Owned split")
                .subtotal(new BigDecimal("20.00"))
                .taxAmount(new BigDecimal("0.00"))
                .tipAmount(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("20.00"))
                .splitType(SplitType.EQUAL)
                .build();
        when(billSplitRepository.findByIdAndUser(9L, user)).thenReturn(Optional.of(billSplit));

        assertEquals("Owned split", billSplitService.getBillSplitById(9L).getTitle());
        verify(billSplitRepository).findByIdAndUser(9L, user);
    }

    @Test
    void missingOwnedBillIsNotFoundAndDeleteUsesOwnershipScope() {
        when(billSplitRepository.findByIdAndUser(404L, user)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> billSplitService.getBillSplitById(404L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        assertThrows(ResponseStatusException.class, () -> billSplitService.deleteBillSplit(404L));
        verify(billSplitRepository, never()).delete(any());
    }

    private BillSplitRequest equalRequest(String title, String subtotal, String... names) {
        return BillSplitRequest.builder()
                .title(title)
                .subtotal(new BigDecimal(subtotal))
                .splitType(SplitType.EQUAL)
                .participants(List.of(names).stream()
                        .map(name -> ParticipantRequest.builder().participantName(name).build())
                        .toList())
                .build();
    }

    private ItemRequest item(String itemName, String quantity, String unitPrice, String... participantNames) {
        return ItemRequest.builder()
                .itemName(itemName)
                .quantity(new BigDecimal(quantity))
                .unitPrice(new BigDecimal(unitPrice))
                .participantNames(participantNames.length == 0 ? List.of() : List.of(participantNames))
                .build();
    }

    private BigDecimal sumShares(BillSplitResponse response) {
        return response.getParticipants().stream()
                .map(ParticipantResponse::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
