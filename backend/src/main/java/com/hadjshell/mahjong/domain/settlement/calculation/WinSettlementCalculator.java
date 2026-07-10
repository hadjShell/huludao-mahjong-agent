package com.hadjshell.mahjong.domain.settlement.calculation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.hadjshell.mahjong.domain.exception.InvalidSettlementInputException;
import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;
import com.hadjshell.mahjong.domain.model.report.LostBreakdown;
import com.hadjshell.mahjong.domain.model.win.DiscardWin;
import com.hadjshell.mahjong.domain.model.win.ExhaustiveDraw;
import com.hadjshell.mahjong.domain.model.win.HandVisibility;
import com.hadjshell.mahjong.domain.model.win.SelfDrawWin;
import com.hadjshell.mahjong.domain.model.win.WinOutcome;
import com.hadjshell.mahjong.domain.model.win.WinningCategory;
import com.hadjshell.mahjong.domain.settlement.SettlementCalculator;

public class WinSettlementCalculator {

  /**
   * Builds the per-loser win fan explanation for the provided win outcome.
   */
  public List<LostBreakdown> buildLostBreakdowns(
      Seat                    dealer,
      WinOutcome              outcome
  ) {
    if (outcome instanceof DiscardWin win) 
      return buildDiscardLostBreakdowns(dealer, win);
    if (outcome instanceof SelfDrawWin win)
      return buildSelfDrawLostBreakdowns(dealer, win);
    if (outcome instanceof ExhaustiveDraw)
      return buildExhaustiveLostBreakdowns(dealer);
    throw new InvalidSettlementInputException("Unsupported win outcome");
  }

  /**
   * Converts lost-fan breakdowns into the win delta for each seat.
   */
  public Map<Seat, Integer> calculateWinDelta(
      Seat                    dealer,
      WinOutcome              outcome,
      List<LostBreakdown>     lostBreakdowns
  ) {
    Map<Seat, Integer> winDelta = zeroDelta();

    if (outcome instanceof DiscardWin win) {
      int total = lostBreakdowns.stream().mapToInt(LostBreakdown::lostFan).sum();
      for (Seat s : Seat.values()) {
        if (s.equals(win.discarder()))    winDelta.put(s, -total);
        else if (s.equals(win.winner()))  winDelta.put(s, total);
        else                              winDelta.put(s, 0);
      }
    }
    else if (outcome instanceof SelfDrawWin win) {
      int total = lostBreakdowns.stream().mapToInt(LostBreakdown::lostFan).sum();
      winDelta.put(win.winner(), total);
      for (LostBreakdown lb : lostBreakdowns) 
        winDelta.put(lb.loser(), lb.lostFan() * -1);
    }
    else if (outcome instanceof ExhaustiveDraw) {
      for (Seat seat : Seat.values()) 
        winDelta.put(seat, seat == dealer ? 3 : -1);
    }
    else
      throw new InvalidSettlementInputException("Unsupported win outcome");

    return winDelta;
  }

  /**
   * Builds lost-fan breakdowns for a discard win.
   */
  private List<LostBreakdown> buildDiscardLostBreakdowns(
      Seat                    dealer,
      DiscardWin              win
  ) {
    List<LostBreakdown> lostBreakdowns = new ArrayList<>();

    Seat winner = win.winner();
    Seat discarder = win.discarder();
    WinningCategory category = win.category();
    Map<Seat, HandVisibility> visibility = win.loserVisibility();
    List<Seat> losers = losersOf(winner);

    for (Seat loser : losers) {
      LostBreakdown lostBreakdown;

      if (isBasicOrSingle(category)) {
        lostBreakdown = calculateBasicOrSingleLostFan(
          dealer, winner, loser, category, visibility,
          false, loser.equals(discarder), false
          );
      }
      else 
        lostBreakdown = calculateFixedDiscardLostFan(loser, discarder, category);

      lostBreakdowns.add(lostBreakdown);
    }

    return lostBreakdowns;
  }

  /**
   * Builds lost-fan breakdowns for a self-draw win.
   */
  private List<LostBreakdown> buildSelfDrawLostBreakdowns(
      Seat                    dealer,
      SelfDrawWin             win
  ) {
    List<LostBreakdown> lostBreakdowns = new ArrayList<>();

    Seat winner = win.winner();
    List<Seat> losers = losersOf(winner);
    WinningCategory category = win.category();
    Map<Seat, HandVisibility> visibility = win.loserVisibility();
    boolean isKongReplacementWin = win.isKongReplacementWin();

    for (Seat loser : losers) {
      LostBreakdown lostBreakdown;

      if (isBasicOrSingle(category)) {
        lostBreakdown = calculateBasicOrSingleLostFan(
          dealer, winner, loser, category, visibility, 
          true, false, isKongReplacementWin);
      }
      else 
        lostBreakdown = calculateFixedSelfDrawLostFan(loser, category);

      lostBreakdowns.add(lostBreakdown);
    }

    return lostBreakdowns;
  }

  /**
   * Builds lost-fan breakdowns for an exhaustive draw.
   */
  private List<LostBreakdown> buildExhaustiveLostBreakdowns(
      Seat dealer
  ) {
    List<LostBreakdown> lostBreakdowns = new ArrayList<>();

    for (Seat loser : Seat.values()) {
      if (!loser.equals(dealer)) 
        lostBreakdowns.add(new LostBreakdown(
          loser, 
          SettlementCalculator.EXHAUSTIVE_FAN, 
          List.of(SettlementReason.EXHAUSTIVE_DRAW)));
    }

    return lostBreakdowns;
  }

  /**
   * Calculates the Basic or Single lost fan for one loser.
   */
  private LostBreakdown calculateBasicOrSingleLostFan(
      Seat dealer,
      Seat winner,
      Seat loser,
      WinningCategory category,
      Map<Seat, HandVisibility> visibility,
      boolean selfDraw,
      boolean loserIsDiscarder,
      boolean isKongReplacementWin
  ) {
    int fan = category == WinningCategory.BASIC
      ? SettlementCalculator.BASIC_FAN
      : SettlementCalculator.SINGLE_FAN;
    List<SettlementReason> reasons = new ArrayList<>();

    if (selfDraw) {
      fan *= 2;
      reasons.add(SettlementReason.SELF_DRAW_DOUBLE);
    }
    if (loserIsDiscarder) {
      fan *= 2;
      reasons.add(SettlementReason.DISCARDER_DOUBLE);
    }
    if (winner == dealer || loser == dealer) {
      fan *= 2;
      reasons.add(SettlementReason.DEALER_DOUBLE);
    }
    if (visibility.get(loser) == HandVisibility.CLOSED) {
      fan *= 2;
      reasons.add(SettlementReason.CLOSED_HAND_DOUBLE);
    }
    if (allLosersClosed(visibility)) {
      fan *= 2;
      reasons.add(SettlementReason.THREE_CLOSED_LOSERS_DOUBLE);
    }
    if (isKongReplacementWin) {
      fan *= 2;
      reasons.add(SettlementReason.KONG_REPLACEMENT_WIN_DOUBLE);
    }

    return new LostBreakdown(loser, fan, reasons);
  }

  /**
   * Calculates fixed-category lost fan for one loser in a discard win.
   */
  private LostBreakdown calculateFixedDiscardLostFan(
      Seat loser,
      Seat discarder,
      WinningCategory category
  ) {
    if (category == WinningCategory.SEVEN_PAIRS) {
      int fan = loser.equals(discarder) 
        ? SettlementCalculator.SEVEN_PAIRS_DISCARD_FAN
        : SettlementCalculator.SEVEN_PAIRS_FIX_FAN;
      return new LostBreakdown(loser, fan, List.of(SettlementReason.SEVEN_PAIRS_FIXED));
    }
    if (category == WinningCategory.ALL_TRIPLETS) {
      int fan = loser.equals(discarder)
        ? SettlementCalculator.ALL_TRIPLETS_DISCARD_FAN
        : SettlementCalculator.ALL_TRIPLETS_FIX_FAN;
      return new LostBreakdown(loser, fan, List.of(SettlementReason.ALL_TRIPLETS_FIXED));
    }

    throw new InvalidSettlementInputException("Invalid winning category");
  }

  /**
   * Calculates fixed-category lost fan for one loser in a self-draw win.
   */
  private LostBreakdown calculateFixedSelfDrawLostFan(
      Seat loser,
      WinningCategory category
  ) {
    if (category == WinningCategory.SEVEN_PAIRS) {
      return new LostBreakdown(
        loser,
        SettlementCalculator.SEVEN_PAIRS_DISCARD_FAN,
        List.of(SettlementReason.SEVEN_PAIRS_FIXED));
    }
    if (category == WinningCategory.ALL_TRIPLETS) {
      return new LostBreakdown(
        loser,
        SettlementCalculator.ALL_TRIPLETS_DISCARD_FAN,
        List.of(SettlementReason.ALL_TRIPLETS_FIXED));
    }

    throw new InvalidSettlementInputException("Invalid winning category");
  }

  /**
   * Returns all non-winning seats.
   */
  private List<Seat> losersOf(Seat winner) {
    List<Seat> losers = new ArrayList<>();
    for (Seat seat : Seat.values())
      if (!seat.equals(winner)) losers.add(seat);

    return losers;
  }

  /**
   * Returns whether the winning category uses Basic/Single multiplier rules.
   */
  private boolean isBasicOrSingle(WinningCategory category) {
    return category == WinningCategory.BASIC || category == WinningCategory.SINGLE;
  }

  /**
   * Returns whether all losers hands are closed.
   */
  private boolean allLosersClosed(Map<Seat, HandVisibility> visibility) {
    return visibility.values().stream()
      .allMatch(v -> v == HandVisibility.CLOSED);
  }

  /**
   * Creates a delta map initialized to zero for every seat.
   */
  private Map<Seat, Integer> zeroDelta() {
    Map<Seat, Integer> result = new EnumMap<>(Seat.class);
    for (Seat seat : Seat.values())
      result.put(seat, 0);

    return result;
  }
}
