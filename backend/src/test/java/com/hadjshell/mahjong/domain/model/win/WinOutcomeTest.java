package com.hadjshell.mahjong.domain.model.win;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hadjshell.mahjong.domain.model.Seat;

class WinOutcomeTest {

  @Test
  void copiesLoserVisibilityDefensively() {
    Map<Seat, HandVisibility> loserVisibility = loserVisibilityFor(Seat.SOUTH);

    SelfDrawWin win = new SelfDrawWin(
      Seat.SOUTH,
      WinningCategory.BASIC,
      loserVisibility,
      false);
    loserVisibility.put(Seat.EAST, HandVisibility.CLOSED);

    assertEquals(HandVisibility.OPEN, win.loserVisibility().get(Seat.EAST));
  }

  @Test
  void rejectsDiscardWinWhenDiscarderIsWinner() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new DiscardWin(
        Seat.SOUTH,
        Seat.SOUTH,
        WinningCategory.BASIC,
        loserVisibilityFor(Seat.SOUTH)));
  }

  @Test
  void rejectsWinnerInsideLoserVisibility() {
    Map<Seat, HandVisibility> loserVisibility = loserVisibilityFor(Seat.SOUTH);
    loserVisibility.put(Seat.SOUTH, HandVisibility.OPEN);

    assertThrows(
      IllegalArgumentException.class,
      () -> new SelfDrawWin(
        Seat.SOUTH,
        WinningCategory.BASIC,
        loserVisibility,
        false));
  }

  @Test
  void rejectsNullLoserVisibilityValue() {
    Map<Seat, HandVisibility> loserVisibility = loserVisibilityFor(Seat.SOUTH);
    loserVisibility.put(Seat.EAST, null);

    assertThrows(
      NullPointerException.class,
      () -> new DiscardWin(
        Seat.SOUTH,
        Seat.WEST,
        WinningCategory.BASIC,
        loserVisibility));
  }

  private static Map<Seat, HandVisibility> loserVisibilityFor(Seat winner) {
    Map<Seat, HandVisibility> result = new EnumMap<>(Seat.class);
    for (Seat seat : Seat.values()) {
      if (!seat.equals(winner))
        result.put(seat, HandVisibility.OPEN);
    }

    return result;
  }
}
