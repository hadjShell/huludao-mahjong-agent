package com.hadjshell.mahjong.domain.settlement.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hadjshell.mahjong.domain.config.GameConfig;
import com.hadjshell.mahjong.domain.exception.InvalidSettlementInputException;
import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;
import com.hadjshell.mahjong.domain.model.kong.ConcealedKong;
import com.hadjshell.mahjong.domain.model.kong.DirectExposedKong;
import com.hadjshell.mahjong.domain.model.kong.KongTileType;
import com.hadjshell.mahjong.domain.model.kong.UpgradedKong;
import com.hadjshell.mahjong.domain.model.win.DiscardWin;
import com.hadjshell.mahjong.domain.model.win.ExhaustiveDraw;
import com.hadjshell.mahjong.domain.model.win.HandVisibility;
import com.hadjshell.mahjong.domain.model.win.SelfDrawWin;
import com.hadjshell.mahjong.domain.model.win.WinningCategory;
import com.hadjshell.mahjong.domain.settlement.SettlementCalculator;
import com.hadjshell.mahjong.domain.settlement.SettlementInput;
import com.hadjshell.mahjong.domain.settlement.SettlementResult;

class SettlementCalculatorTest {

  @Test
  void calculatesBasicWinOnDiscard() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.NORTH,
      new DiscardWin(
        Seat.SOUTH,
        Seat.WEST,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN))),
      List.of());

    SettlementResult result = calculateAndPrint("basic discard win", input);

    assertSeatDeltas(result.totalDelta(), 0, 0, -5, 5);
    assertSeatDeltas(result.winDelta(), 0, 0, -5, 5);
    assertSeatDeltas(result.kongDelta(), 0, 0, 0, 0);
  }

  @Test
  void calculatesSingleWinByDealerSelfDraw() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new SelfDrawWin(
        Seat.EAST,
        WinningCategory.SINGLE,
        loserVisibility(Map.of(
          Seat.SOUTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN)),
        false),
      List.of());

    SettlementResult result = calculateAndPrint("dealer single self-draw", input);

    assertSeatDeltas(result.totalDelta(), 24, -8, -8, -8);
    assertSeatDeltas(result.winDelta(), 24, -8, -8, -8);
    assertSeatDeltas(result.kongDelta(), 0, 0, 0, 0);
  }

  @Test
  void calculatesBasicSelfDrawWithAllLosersClosed() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.SOUTH,
      new SelfDrawWin(
        Seat.WEST,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.CLOSED,
          Seat.SOUTH, HandVisibility.CLOSED,
          Seat.NORTH, HandVisibility.CLOSED)),
        false),
      List.of());

    SettlementResult result = calculateAndPrint("basic self-draw all losers closed", input);

    assertSeatDeltas(result.totalDelta(), -8, -8, 32, -16);
    assertSeatDeltas(result.winDelta(), -8, -8, 32, -16);
    assertSeatDeltas(result.kongDelta(), 0, 0, 0, 0);
  }

  @Test
  void calculatesSevenPairsOnDiscard() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new DiscardWin(
        Seat.SOUTH,
        Seat.WEST,
        WinningCategory.SEVEN_PAIRS,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN))),
      List.of());

    SettlementResult result = calculateAndPrint("seven pairs discard win", input);

    assertSeatDeltas(result.totalDelta(), 0, 0, -100, 100);
    assertSeatDeltas(result.winDelta(), 0, 0, -100, 100);
    assertSeatDeltas(result.kongDelta(), 0, 0, 0, 0);
  }

  @Test
  void calculatesAllTripletsBySelfDraw() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.NORTH,
      new SelfDrawWin(
        Seat.EAST,
        WinningCategory.ALL_TRIPLETS,
        loserVisibility(Map.of(
          Seat.SOUTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN)),
        false),
      List.of());

    SettlementResult result = calculateAndPrint("all triplets self-draw", input);

    assertSeatDeltas(result.totalDelta(), 90, -30, -30, -30);
    assertSeatDeltas(result.winDelta(), 90, -30, -30, -30);
    assertSeatDeltas(result.kongDelta(), 0, 0, 0, 0);
  }

  @Test
  void calculatesDirectExposedKongAsDiscarderPaysAllShares() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new ExhaustiveDraw(),
      List.of(
        new DirectExposedKong(Seat.SOUTH, Seat.WEST, KongTileType.NON_DRAGON),
        new ConcealedKong(Seat.NORTH, KongTileType.DRAGON)));

    SettlementResult result = calculateAndPrint("kong rewards only", input);

    assertSeatDeltas(result.kongDelta(), -8, 24, -14, -2);
    assertSeatDeltas(result.totalDelta(), -5, 23, -15, -3);
  }

  @Test
  void calculatesBasicWinPlusUpgradedKongReward() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new DiscardWin(
        Seat.SOUTH,
        Seat.WEST,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN))),
      List.of(new UpgradedKong(Seat.WEST, KongTileType.NON_DRAGON)));

    SettlementResult result = calculateAndPrint("basic discard win plus upgraded kong", input);

    assertSeatDeltas(result.winDelta(), 0, 0, -5, 5);
    assertSeatDeltas(result.kongDelta(), -2, -2, 6, -2);
    assertSeatDeltas(result.totalDelta(), -2, -2, 1, 3);
  }

  @Test
  void calculatesSingleKongReplacementSelfDrawWithMixedVisibilityAndMultipleKongs() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.WEST,
      new SelfDrawWin(
        Seat.NORTH,
        WinningCategory.SINGLE,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.CLOSED,
          Seat.WEST, HandVisibility.OPEN,
          Seat.SOUTH, HandVisibility.OPEN)),
        true),
      List.of(
        new DirectExposedKong(Seat.SOUTH, Seat.EAST, KongTileType.DRAGON),
        new UpgradedKong(Seat.NORTH, KongTileType.DRAGON),
        new ConcealedKong(Seat.WEST, KongTileType.NON_DRAGON)));

    SettlementResult result = calculateAndPrint(
      "single kong replacement self-draw with mixed visibility and multiple kongs",
      input);

    assertSeatDeltas(result.winDelta(), -16, 40, -16, -8);
    assertSeatDeltas(result.kongDelta(), -20, 8, 8, 4);
    assertSeatDeltas(result.totalDelta(), -36, 48, -8, -4);
  }

  @Test
  void calculatesAllTripletsDiscardWinWithDealerDiscarderAndMultipleKongs() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.SOUTH,
      new DiscardWin(
        Seat.EAST,
        Seat.SOUTH,
        WinningCategory.ALL_TRIPLETS,
        loserVisibility(Map.of(
          Seat.NORTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN,
          Seat.SOUTH, HandVisibility.OPEN))),
      List.of(
        new ConcealedKong(Seat.EAST, KongTileType.DRAGON),
        new DirectExposedKong(Seat.WEST, Seat.NORTH, KongTileType.NON_DRAGON),
        new UpgradedKong(Seat.SOUTH, KongTileType.NON_DRAGON)));

    SettlementResult result = calculateAndPrint(
      "all triplets discard win with dealer discarder and multiple kongs",
      input);

    assertSeatDeltas(result.winDelta(), 70, 0, 0, -70);
    assertSeatDeltas(result.kongDelta(), 22, -16, -4, -2);
    assertSeatDeltas(result.totalDelta(), 92, -16, -4, -72);
  }

  @Test
  void convertsComplexSettlementToMoneyUsingConfiguredBase() {
    SettlementInput input = new SettlementInput(
      new GameConfig(
        new BigDecimal("2.50"),
        GameConfig.DEFAULT_INITIAL_POINTS,
        GameConfig.DEFAULT_ROUND_COUNT,
        Seat.defaultOrder()),
      Seat.NORTH,
      new SelfDrawWin(
        Seat.SOUTH,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.CLOSED,
          Seat.WEST, HandVisibility.OPEN)),
        false),
      List.of(
        new DirectExposedKong(Seat.EAST, Seat.WEST, KongTileType.NON_DRAGON),
        new ConcealedKong(Seat.SOUTH, KongTileType.DRAGON)));

    SettlementResult result = calculateAndPrint(
      "complex self-draw with configured money base",
      input);

    assertSeatDeltas(result.winDelta(), -2, -8, -2, 12);
    assertSeatDeltas(result.kongDelta(), -2, -8, -14, 24);
    assertSeatDeltas(result.totalDelta(), -4, -16, -16, 36);
    assertEquals(new BigDecimal("-10.00"), result.moneyDelta().get(Seat.EAST));
    assertEquals(new BigDecimal("-40.00"), result.moneyDelta().get(Seat.NORTH));
    assertEquals(new BigDecimal("-40.00"), result.moneyDelta().get(Seat.WEST));
    assertEquals(new BigDecimal("90.00"), result.moneyDelta().get(Seat.SOUTH));
  }

  @Test
  void includesDragonReasonForDragonKongs() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new ExhaustiveDraw(),
      List.of(new ConcealedKong(Seat.NORTH, KongTileType.DRAGON)));

    SettlementResult result = calculateAndPrint("dragon concealed kong reason", input);

    assertEquals(
      List.of(SettlementReason.CONCEALED_KONG, SettlementReason.DRAGON_KONG),
      result.kongBreakdowns().get(0).reasons());
  }

  @Test
  void rejectsNullInput() {
    assertThrows(
      NullPointerException.class,
      () -> new SettlementCalculator().calculate(null));
  }

  @Test
  void rejectsSettlementResultWithMissingSeatDelta() {
    Map<Seat, Integer> partialWinDelta = new EnumMap<>(Seat.class);
    partialWinDelta.put(Seat.EAST, 0);

    assertThrows(
      IllegalArgumentException.class,
      () -> new SettlementResult(
        partialWinDelta,
        integerSeatMap(0),
        integerSeatMap(0),
        moneySeatMap(BigDecimal.ZERO),
        List.of(),
        List.of()));
  }

  @Test
  void rejectsNullLoserVisibilityValue() {
    Map<Seat, HandVisibility> loserVisibility = new EnumMap<>(Seat.class);
    loserVisibility.put(Seat.EAST, HandVisibility.OPEN);
    loserVisibility.put(Seat.NORTH, null);
    loserVisibility.put(Seat.WEST, HandVisibility.CLOSED);

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> new SelfDrawWin(
        Seat.SOUTH,
        com.hadjshell.mahjong.domain.model.win.WinningCategory.BASIC,
        loserVisibility,
        false));

    assertTrue(exception.getMessage().contains("loserVisibility values"));
  }

  @Test
  void rejectsDirectExposedKongDeclarerMarkedClosed() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new SelfDrawWin(
        Seat.SOUTH,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.CLOSED,
          Seat.WEST, HandVisibility.OPEN)),
        false),
      List.of(new DirectExposedKong(Seat.NORTH, Seat.WEST, KongTileType.NON_DRAGON)));

    InvalidSettlementInputException exception = assertThrows(
      InvalidSettlementInputException.class,
      () -> new SettlementCalculator().calculate(input));

    assertTrue(exception.getMessage().contains("declarers must be open"));
  }

  @Test
  void rejectsUpgradedKongDeclarerMarkedClosed() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new DiscardWin(
        Seat.SOUTH,
        Seat.WEST,
        WinningCategory.BASIC,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.CLOSED,
          Seat.NORTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN))),
      List.of(new UpgradedKong(Seat.EAST, KongTileType.NON_DRAGON)));

    InvalidSettlementInputException exception = assertThrows(
      InvalidSettlementInputException.class,
      () -> new SettlementCalculator().calculate(input));

    assertTrue(exception.getMessage().contains("declarers must be open"));
  }

  @Test
  void rejectsSevenPairsWinnerWithUpgradedKong() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new SelfDrawWin(
        Seat.SOUTH,
        WinningCategory.SEVEN_PAIRS,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN)),
        false),
      List.of(new UpgradedKong(Seat.SOUTH, KongTileType.NON_DRAGON)));

    InvalidSettlementInputException exception = assertThrows(
      InvalidSettlementInputException.class,
      () -> new SettlementCalculator().calculate(input));

    assertTrue(exception.getMessage().contains("declarers must be open"));
  }

  @Test
  void rejectsKongReplacementWinWithoutWinnerUpgradedKong() {
    SettlementInput input = new SettlementInput(
      GameConfig.defaultConfig(),
      Seat.EAST,
      new SelfDrawWin(
        Seat.SOUTH,
        WinningCategory.SINGLE,
        loserVisibility(Map.of(
          Seat.EAST, HandVisibility.OPEN,
          Seat.NORTH, HandVisibility.OPEN,
          Seat.WEST, HandVisibility.OPEN)),
        true),
      List.of(new ConcealedKong(Seat.SOUTH, KongTileType.NON_DRAGON)));

    InvalidSettlementInputException exception = assertThrows(
      InvalidSettlementInputException.class,
      () -> new SettlementCalculator().calculate(input));

    assertTrue(exception.getMessage().contains("winner must have an upgraded kong"));
  }

  private static Map<Seat, Integer> integerSeatMap(int value) {
    Map<Seat, Integer> result = new EnumMap<>(Seat.class);
    for (Seat seat : Seat.values())
      result.put(seat, value);

    return result;
  }

  private static Map<Seat, BigDecimal> moneySeatMap(BigDecimal value) {
    Map<Seat, BigDecimal> result = new EnumMap<>(Seat.class);
    for (Seat seat : Seat.values())
      result.put(seat, value);

    return result;
  }

  private static Map<Seat, HandVisibility> loserVisibility(
      Map<Seat, HandVisibility> visibility
  ) {
    return new EnumMap<>(visibility);
  }

  private static SettlementResult calculateAndPrint(
      String label,
      SettlementInput input
  ) {
    printInput(label, input);
    SettlementResult result = new SettlementCalculator().calculate(input);
    printResult(label, result);

    return result;
  }

  private static void assertSeatDeltas(
      Map<Seat, Integer> deltas,
      int east,
      int north,
      int west,
      int south
  ) {
    assertEquals(east, deltas.get(Seat.EAST));
    assertEquals(north, deltas.get(Seat.NORTH));
    assertEquals(west, deltas.get(Seat.WEST));
    assertEquals(south, deltas.get(Seat.SOUTH));
  }

  private static void printResult(String label, SettlementResult result) {
    System.out.println();
    System.out.println("Settlement result: " + label);
    System.out.println("Seat   Win   Kong   Total   Money");
    for (Seat seat : Seat.values()) {
      System.out.printf(
        "%-5s %5d %6d %7d %7s%n",
        seat.name(),
        result.winDelta().get(seat),
        result.kongDelta().get(seat),
        result.totalDelta().get(seat),
        result.moneyDelta().get(seat).toPlainString());
    }
  }

  private static void printInput(String label, SettlementInput input) {
    System.out.println();
    System.out.println("Settlement input: " + label);
    System.out.printf(
      "Config: base=%s, initialPoints=%d, roundCount=%d, seatOrder=%s%n",
      input.config().base().toPlainString(),
      input.config().initialPoints(),
      input.config().roundCount(),
      input.config().seatOrder());
    System.out.println("Dealer: " + input.dealer());
    System.out.println("Win outcome: " + formatWinOutcome(input.winOutcome()));
    System.out.println("Kong events:");
    if (input.kongEvents().isEmpty()) {
      System.out.println("  none");
      return;
    }

    for (int i = 0; i < input.kongEvents().size(); i++) {
      System.out.printf("  %d. %s%n", i + 1, formatKongEvent(input.kongEvents().get(i)));
    }
  }

  private static String formatWinOutcome(com.hadjshell.mahjong.domain.model.win.WinOutcome outcome) {
    if (outcome instanceof DiscardWin discardWin) {
      return "discard win, winner=" + discardWin.winner()
        + ", discarder=" + discardWin.discarder()
        + ", category=" + discardWin.category()
        + ", loserVisibility=" + discardWin.loserVisibility();
    }
    if (outcome instanceof SelfDrawWin selfDrawWin) {
      return "self-draw win, winner=" + selfDrawWin.winner()
        + ", category=" + selfDrawWin.category()
        + ", kongReplacement=" + selfDrawWin.isKongReplacementWin()
        + ", loserVisibility=" + selfDrawWin.loserVisibility();
    }
    if (outcome instanceof ExhaustiveDraw) {
      return "exhaustive draw";
    }

    return outcome.toString();
  }

  private static String formatKongEvent(com.hadjshell.mahjong.domain.model.kong.KongEvent event) {
    if (event instanceof DirectExposedKong directExposedKong) {
      return "direct exposed kong, declarer=" + directExposedKong.declarer()
        + ", discarder=" + directExposedKong.discarder()
        + ", tileType=" + directExposedKong.tileType()
        + ", fan=" + directExposedKong.fan();
    }
    if (event instanceof ConcealedKong concealedKong) {
      return "concealed kong, declarer=" + concealedKong.declarer()
        + ", tileType=" + concealedKong.tileType()
        + ", fan=" + concealedKong.fan();
    }
    if (event instanceof UpgradedKong upgradedKong) {
      return "upgraded kong, declarer=" + upgradedKong.declarer()
        + ", tileType=" + upgradedKong.tileType()
        + ", fan=" + upgradedKong.fan();
    }

    return event.toString();
  }
}
