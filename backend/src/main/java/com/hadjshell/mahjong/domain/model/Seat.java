package com.hadjshell.mahjong.domain.model;

import java.util.List;

public enum Seat {
  EAST,
  NORTH,
  WEST,
  SOUTH;

  public static List<Seat> defaultOrder() {
    return List.of(EAST, NORTH, WEST, SOUTH);
  }
}
