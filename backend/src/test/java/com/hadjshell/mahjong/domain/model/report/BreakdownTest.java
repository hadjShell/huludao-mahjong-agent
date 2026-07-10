package com.hadjshell.mahjong.domain.model.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;
import com.hadjshell.mahjong.domain.model.kong.ConcealedKong;
import com.hadjshell.mahjong.domain.model.kong.KongTileType;

class BreakdownTest {

  @Test
  void rejectsNegativeLostFan() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new LostBreakdown(
        Seat.SOUTH,
        -1,
        List.of(SettlementReason.DEALER_DOUBLE)));
  }

  @Test
  void copiesLostReasonsDefensively() {
    List<SettlementReason> reasons = new java.util.ArrayList<>();
    reasons.add(SettlementReason.DEALER_DOUBLE);

    LostBreakdown breakdown = new LostBreakdown(Seat.SOUTH, 2, reasons);
    reasons.add(SettlementReason.CLOSED_HAND_DOUBLE);

    assertEquals(List.of(SettlementReason.DEALER_DOUBLE), breakdown.lostReasons());
  }

  @Test
  void rejectsKongDeltaMissingSeat() {
    Map<Seat, Integer> delta = new EnumMap<>(Seat.class);
    delta.put(Seat.EAST, 0);

    assertThrows(
      IllegalArgumentException.class,
      () -> new KongBreakdown(
        new ConcealedKong(Seat.SOUTH, KongTileType.NON_DRAGON),
        delta,
        List.of(SettlementReason.CONCEALED_KONG)));
  }

  @Test
  void copiesKongDeltaAndReasonsDefensively() {
    Map<Seat, Integer> delta = zeroDelta();
    List<SettlementReason> reasons = new java.util.ArrayList<>();
    reasons.add(SettlementReason.CONCEALED_KONG);

    KongBreakdown breakdown = new KongBreakdown(
      new ConcealedKong(Seat.SOUTH, KongTileType.NON_DRAGON),
      delta,
      reasons);
    delta.put(Seat.EAST, 99);
    reasons.add(SettlementReason.DRAGON_KONG);

    assertEquals(0, breakdown.kongDelta().get(Seat.EAST));
    assertEquals(List.of(SettlementReason.CONCEALED_KONG), breakdown.reasons());
  }

  private static Map<Seat, Integer> zeroDelta() {
    Map<Seat, Integer> result = new EnumMap<>(Seat.class);
    for (Seat seat : Seat.values())
      result.put(seat, 0);

    return result;
  }
}
