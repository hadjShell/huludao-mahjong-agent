package com.hadjshell.mahjong.domain.settlement;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hadjshell.mahjong.domain.exception.InvalidSettlementInputException;
import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.kong.DirectExposedKong;
import com.hadjshell.mahjong.domain.model.kong.KongEvent;
import com.hadjshell.mahjong.domain.model.kong.UpgradedKong;
import com.hadjshell.mahjong.domain.model.report.KongBreakdown;
import com.hadjshell.mahjong.domain.model.report.LostBreakdown;
import com.hadjshell.mahjong.domain.model.win.DiscardWin;
import com.hadjshell.mahjong.domain.model.win.HandVisibility;
import com.hadjshell.mahjong.domain.model.win.SelfDrawWin;
import com.hadjshell.mahjong.domain.model.win.WinOutcome;
import com.hadjshell.mahjong.domain.model.win.WinningCategory;
import com.hadjshell.mahjong.domain.settlement.calculation.KongSettlementCalculator;
import com.hadjshell.mahjong.domain.settlement.calculation.WinSettlementCalculator;

public class SettlementCalculator {

  public static final int SEVEN_PAIRS_DISCARD_FAN = 40;
  public static final int SEVEN_PAIRS_FIX_FAN = 30;
  public static final int ALL_TRIPLETS_DISCARD_FAN = 30;
  public static final int ALL_TRIPLETS_FIX_FAN = 20;
  public static final int BASIC_FAN = 1;
  public static final int SINGLE_FAN = 2;
  public static final int EXHAUSTIVE_FAN = 1;

  private final WinSettlementCalculator winCalculator = new WinSettlementCalculator();
  private final KongSettlementCalculator kongCalculator = new KongSettlementCalculator();
  
  /**
   * Calculates the complete settlement result for one completed round.
   */
  public SettlementResult calculate(SettlementInput input) {
    validateInput(input);

    List<LostBreakdown>   lostBreakdowns = 
      winCalculator.buildLostBreakdowns(input.dealer(), input.winOutcome());
    Map<Seat, Integer>    winDelta = 
      winCalculator.calculateWinDelta(input.dealer(), input.winOutcome(), lostBreakdowns);
    
    List<KongBreakdown>   kongBreakdowns = 
      kongCalculator.buildKongBreakdowns(input.kongEvents());
    Map<Seat, Integer>    kongDelta = 
      kongCalculator.calculateKongDelta(kongBreakdowns);

    Map<Seat, Integer>    totalDelta = addDelta(winDelta, kongDelta);
    Map<Seat, BigDecimal> moneyDelta = toMoneyDelta(totalDelta, input.config().base());
    
    return new SettlementResult(
      winDelta, kongDelta, totalDelta, moneyDelta, lostBreakdowns, kongBreakdowns);
  }

  /**
   * Validates cross-field rules that require both win outcome and kong events.
   */
  private void validateInput(SettlementInput input) {
    Objects.requireNonNull(input, "input must not be null");
    validateExposedKongDeclarersAreOpen(input.winOutcome(), input.kongEvents());
    validateKongReplacementWinHasUpgradedKong(input.winOutcome(), input.kongEvents());
  }

  /**
   * Ensures any visible loser who declared an exposed kong is marked open.
   */
  private void validateExposedKongDeclarersAreOpen(
      WinOutcome       outcome,
      List<KongEvent>  kongEvents
  ) {
    Map<Seat, HandVisibility> visibility = loserVisibility(outcome);
    for (KongEvent event : kongEvents) {
      Seat declarer = exposedKongDeclarer(event);
      if (declarer != null && isClosedHand(outcome, declarer, visibility)) {
        throw new InvalidSettlementInputException(
          "direct exposed kong and upgraded kong declarers must be open");
      }
    }
  }

  /**
   * Ensures a kong-replacement win is backed by the winner's upgraded kong event.
   */
  private void validateKongReplacementWinHasUpgradedKong(
      WinOutcome       outcome,
      List<KongEvent>  kongEvents
  ) {
    if (!(outcome instanceof SelfDrawWin win) || !win.isKongReplacementWin())
      return;

    boolean winnerHasUpgradedKong = kongEvents.stream()
      .anyMatch(event -> event instanceof UpgradedKong upgradedKong
        && upgradedKong.declarer() == win.winner());

    if (!winnerHasUpgradedKong) {
      throw new InvalidSettlementInputException(
        "kong-replacement winner must have an upgraded kong event");
    }
  }

  /**
   * Returns loser visibility for win outcomes that carry it.
   */
  private Map<Seat, HandVisibility> loserVisibility(WinOutcome outcome) {
    if (outcome instanceof DiscardWin win)
      return win.loserVisibility();
    if (outcome instanceof SelfDrawWin win)
      return win.loserVisibility();

    return Map.of();
  }

  /**
   * Returns the declarer for kong types that require an open hand.
   */
  private Seat exposedKongDeclarer(KongEvent event) {
    if (event instanceof DirectExposedKong directExposedKong)
      return directExposedKong.declarer();
    if (event instanceof UpgradedKong upgradedKong)
      return upgradedKong.declarer();

    return null;
  }

  /**
   * Returns whether the seat is known to have a closed hand.
   */
  private boolean isClosedHand(
      WinOutcome                    outcome,
      Seat                          seat,
      Map<Seat, HandVisibility>     loserVisibility
  ) {
    if (loserVisibility.get(seat) == HandVisibility.CLOSED)
      return true;

    return isSevenPairsWinner(outcome, seat);
  }

  /**
   * Returns whether the seat won with Seven Pairs, which is modeled as closed.
   */
  private boolean isSevenPairsWinner(WinOutcome outcome, Seat seat) {
    if (outcome instanceof DiscardWin win)
      return win.winner() == seat && win.category() == WinningCategory.SEVEN_PAIRS;
    if (outcome instanceof SelfDrawWin win)
      return win.winner() == seat && win.category() == WinningCategory.SEVEN_PAIRS;

    return false;
  }

  /**
   * Adds win and kong deltas into one total delta map.
   */
  private Map<Seat, Integer> addDelta(
      Map<Seat, Integer>  winDelta,
      Map<Seat, Integer>  kongDelta
  ) {
    Map<Seat, Integer> result = zeroDelta();
    winDelta.entrySet().stream()
      .forEach(e -> result.merge(e.getKey(), e.getValue(), Integer::sum));
    kongDelta.entrySet().stream()
      .forEach(e -> result.merge(e.getKey(), e.getValue(), Integer::sum));

    return result;
  }

  /**
   * Converts integer fan deltas into money deltas using the configured base.
   */
  private Map<Seat, BigDecimal> toMoneyDelta(
      Map<Seat, Integer>  totalDelta,
      BigDecimal          base
  ) {
    Map<Seat, BigDecimal> result = new EnumMap<>(Seat.class);
    totalDelta.forEach((seat, delta) ->
        result.put(seat, BigDecimal.valueOf(delta).multiply(base)));

    return result;
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
