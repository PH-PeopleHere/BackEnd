package peoplehere.peoplehere.service;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import peoplehere.peoplehere.common.exception.TourException;
import peoplehere.peoplehere.common.exception.UserException;
import peoplehere.peoplehere.controller.dto.tour.GetTourDatesResponse;
import peoplehere.peoplehere.controller.dto.tour.GetTourParticipantsResponse;
import peoplehere.peoplehere.controller.dto.tour.TourDateDtoConverter;
import peoplehere.peoplehere.controller.dto.tour.TourDateInfoDto;
import peoplehere.peoplehere.controller.dto.user.UserInfoDto;
import peoplehere.peoplehere.domain.enums.Status;
import peoplehere.peoplehere.domain.*;
import peoplehere.peoplehere.domain.enums.TourDateStatus;
import peoplehere.peoplehere.domain.enums.TourHistoryStatus;
import peoplehere.peoplehere.repository.*;
import peoplehere.peoplehere.util.security.UserDetailsImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static peoplehere.peoplehere.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TourDateService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourHistoryRepository tourHistoryRepository;
    private final TourDateRepository tourDateRepository;


    /**
     * 특정 투어의 모든 일정 조회
     */
    public List<GetTourDatesResponse> getTourDates(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new TourException(TOUR_NOT_FOUND));

        LocalDate today = LocalDate.now();
        return tour.getTourDates().stream()
            .filter(
                tourDate -> tourDate.getDate().isAfter(today) || tourDate.getDate().isEqual(today))
            .filter(tourDate -> tourDate.getStatus() == TourDateStatus.AVAILABLE)
            .map(tourDate -> {
                GetTourDatesResponse response = TourDateDtoConverter.tourDateToGetTourDatesResponse(
                    tourDate);
                List<UserInfoDto> participants = tourDate.getTourHistories().stream()
                    .filter(th -> th.getStatus() == TourHistoryStatus.CONFIRMED)
                    .map(th -> new UserInfoDto(th.getUser().getId(), th.getUser().getFirstName(),
                        th.getUser().getImageUrl()))
                    .toList();
                response.setParticipants(participants);
                return response;
            })
            .toList();
    }

    /**
     * 특정 투어 일정 조회
     */
    public TourDateInfoDto getTourDateInfo(Long tourDateId) {
        TourDate tourDate = tourDateRepository.findById(tourDateId)
            .orElseThrow(() -> new TourException(TOUR_DATE_NOT_FOUND));
        return TourDateDtoConverter.convertToTourDateInfoDto(tourDate);
    }

    /**
     * 투어 참여
     */
    public void joinTourDate(Authentication authentication, Long tourDateId) {
        TourDate tourDate = tourDateRepository.findById(tourDateId)
            .orElseThrow(() -> new TourException(TOUR_DATE_NOT_FOUND));
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UserException(USER_NOT_LOGGED_IN);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 해당 일정의 투어 상태가 ACTIVE가 아니면 참여 불가
        if (tourDate.getTour().getStatus() != Status.ACTIVE) {
            throw new TourException(TOUR_INACTIVATED);
        }
        // 해당 일정의 상태가 AVAILABLE이 아니면 참여 불가
        if (tourDate.getStatus() != TourDateStatus.AVAILABLE) {
            throw new TourException(TOUR_DATE_NOT_FOUND);
        }
        // 해당 일정의 투어 리더와 참여 유저가 같으면 참여 불가
        if (Objects.equals(tourDate.getTour().getUser().getId(), user.getId())) {
            throw new TourException(SAME_AS_TOUR_LEADER);
        }

        // 이미 참여한 경우 중복 참여 방지
        boolean alreadyJoined = tourDate.getTour().getTourHistories().stream()
            .anyMatch(th -> th.getUser().getId().equals(user.getId()) && th.getTourDate()
                .equals(tourDate));
        if (alreadyJoined) {
            throw new TourException(TOUR_ALREADY_JOINED);
        }

        TourHistory tourHistory = new TourHistory();
        tourHistory.setUser(user);
        tourHistory.setTourDate(tourDate);
        tourHistory.setTour(tourDate.getTour());
        tourHistory.setStatus(TourHistoryStatus.RESERVED);

        tourHistoryRepository.save(tourHistory);
    }

    /**
     * 투어 일정 추가
     */
    @Transactional
    public void addOrUpdateTourDate(Long tourId, LocalDate date, LocalTime time) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new TourException(TOUR_NOT_FOUND));

        validateTourDate(date, time);

        // 해당 날짜에 대한 TourDate 객체 찾기
        Optional<TourDate> existingTourDate = tour.getTourDates().stream()
            .filter(td -> td.getDate().equals(date))
            .findFirst();

        existingTourDate.ifPresentOrElse(
            tourDate -> {
                if (time != null) {
                    tourDate.setTime(time);
                }
                tourDate.makeAvailable();
            },
            () -> {
                TourDate newTourDate = new TourDate();
                newTourDate.setDate(date);
                if (time != null) {
                    newTourDate.setTime(time);
                }
                newTourDate.makeAvailable();
                tour.addTourDate(newTourDate);
                tourDateRepository.save(newTourDate);
            }
        );
    }

    private void validateTourDate(LocalDate date, LocalTime time) {

        LocalDateTime localDateTime;

        if (time != null) {
            localDateTime = LocalDateTime.of(date, time);
        } else {
            localDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        }

        // 날짜가 과거인지 확인
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new TourException(TOUR_DATE_IN_PAST);
        }
    }

    /**
     * 투어 일정 삭제
     */
    public void updateTourDateStatus(Long tourDateId, TourDateStatus status) {
        TourDate tourDate = tourDateRepository.findById(tourDateId)
            .orElseThrow(() -> new TourException(TOUR_DATE_NOT_FOUND));
        tourDate.setStatus(status);
        tourDateRepository.save(tourDate);
    }

    /**
     * 투어 참여 상태 변경
     */
    public void updateReservationStatus(Long tourHistoryId, TourHistoryStatus status) {
        TourHistory tourHistory = tourHistoryRepository.findById(tourHistoryId)
            .orElseThrow(() -> new TourException(TOUR_HISTORY_NOT_FOUND));

        if (!tourHistory.getStatus().equals(TourHistoryStatus.RESERVED)) {
            throw new TourException(INVALID_TOUR_HISTORY_STATUS);
        }

        tourHistory.setStatus(status);
        tourHistoryRepository.save(tourHistory);
    }

    /**
     * TODO: 채팅으로 로직 변경
     * 한 투어에 대한 참여 요청들 조회
     */
    public List<GetTourParticipantsResponse> getParticipantRequests(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new TourException(TOUR_NOT_FOUND));

        List<TourHistory> tourHistories = tourHistoryRepository.findAllByTourAndStatus(tour,
            TourHistoryStatus.RESERVED);

        List<GetTourParticipantsResponse> getTourDatesResponses = new ArrayList<>();

        for (TourHistory tourHistory : tourHistories) {
            User user = userRepository.findById(tourHistory.getUser().getId()).orElseThrow();
            TourDate tourDate = tourHistory.getTourDate();
            UserInfoDto userInfoDto = new UserInfoDto(user.getId(), user.getFirstName(),
                user.getImageUrl());


            getTourDatesResponses.add(new GetTourParticipantsResponse(tourHistory.getId(), tourDate.getId(),
                tourDate.getDate(), tourDate.getTime(), tourDate.getStatus(), userInfoDto));
        }
        return getTourDatesResponses;
    }

}
