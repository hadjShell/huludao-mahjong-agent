package com.hadjshell.mahjong.domain.model.kong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.hadjshell.mahjong.domain.model.Seat;

class KongEventTest {

  @Test
  void calculatesKongFanByEventTypeAndTileType() {
    assertEquals(2, new DirectExposedKong(
      Seat.SOUTH, Seat.WEST, KongTileType.NON_DRAGON).fan());
    assertEquals(4, new DirectExposedKong(
      Seat.SOUTH, Seat.WEST, KongTileType.DRAGON).fan());
    assertEquals(2, new UpgradedKong(Seat.SOUTH, KongTileType.NON_DRAGON).fan());
    assertEquals(4, new UpgradedKong(Seat.SOUTH, KongTileType.DRAGON).fan());
    assertEquals(4, new ConcealedKong(Seat.SOUTH, KongTileType.NON_DRAGON).fan());
    assertEquals(8, new ConcealedKong(Seat.SOUTH, KongTileType.DRAGON).fan());
  }

  @Test
  void rejectsDirectExposedKongWithSameDeclarerAndDiscarder() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new DirectExposedKong(Seat.SOUTH, Seat.SOUTH, KongTileType.NON_DRAGON));
  }

  @Test
  void rejectsNullRequiredFields() {
    assertThrows(
      NullPointerException.class,
      () -> new ConcealedKong(null, KongTileType.NON_DRAGON));
    assertThrows(
      NullPointerException.class,
      () -> new UpgradedKong(Seat.SOUTH, null));
    assertThrows(
      NullPointerException.class,
      () -> new DirectExposedKong(Seat.SOUTH, null, KongTileType.NON_DRAGON));
  }
}
