package com.hadjshell.mahjong.domain.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hadjshell.mahjong.domain.model.Seat;

class GameConfigTest {

  @Test
  void createsDefaultConfig() {
    GameConfig config = GameConfig.defaultConfig();

    assertEquals(BigDecimal.ONE, config.base());
    assertEquals(100, config.initialPoints());
    assertEquals(10, config.roundCount());
    assertEquals(Seat.defaultOrder(), config.seatOrder());
  }

  @Test
  void rejectsNonPositiveBase() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new GameConfig(BigDecimal.ZERO, 100, 10, Seat.defaultOrder()));
  }

  @Test
  void rejectsSeatOrderWithoutExactlyFourSeats() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new GameConfig(
        BigDecimal.ONE,
        100,
        10,
        List.of(Seat.EAST, Seat.EAST, Seat.WEST, Seat.SOUTH)));
  }
}
