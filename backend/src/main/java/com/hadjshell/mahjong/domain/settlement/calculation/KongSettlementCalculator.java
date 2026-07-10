package com.hadjshell.mahjong.domain.settlement.calculation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.hadjshell.mahjong.domain.exception.InvalidSettlementInputException;
import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;
import com.hadjshell.mahjong.domain.model.kong.ConcealedKong;
import com.hadjshell.mahjong.domain.model.kong.DirectExposedKong;
import com.hadjshell.mahjong.domain.model.kong.KongEvent;
import com.hadjshell.mahjong.domain.model.kong.KongTileType;
import com.hadjshell.mahjong.domain.model.kong.UpgradedKong;
import com.hadjshell.mahjong.domain.model.report.KongBreakdown;

public class KongSettlementCalculator {

  /**
   * Builds one explanatory breakdown per kong event.
   */
  public List<KongBreakdown> buildKongBreakdowns(
      List<KongEvent> events
  ) {
    List<KongBreakdown> kongBreakdowns = new ArrayList<>();

    for (KongEvent event : events) {
      if (event instanceof DirectExposedKong e)
        kongBreakdowns.add(buildDirectExposedKongBreakdown(e));
      else if (event instanceof UpgradedKong e)
        kongBreakdowns.add(buildUpgradedKongBreakdown(e));
      else if (event instanceof ConcealedKong e)
        kongBreakdowns.add(buildConcealedKongBreakdown(e));
      else
        throw new InvalidSettlementInputException("Unsupported kong event type");
    }

    return kongBreakdowns;
  }

  /**
   * Calculates the aggregate kong delta across all kong breakdowns.
   */
  public Map<Seat, Integer> calculateKongDelta(
      List<KongBreakdown> kongBreakdowns
  ) {
    Map<Seat, Integer> kongDelta = zeroDelta();

    for (KongBreakdown kongBreakdown : kongBreakdowns) {
      Map<Seat, Integer> delta = kongBreakdown.kongDelta();
      for (Seat seat : Seat.values())
        kongDelta.merge(seat, delta.get(seat), Integer::sum);
    }
    
    return kongDelta;
  }

  /**
   * Builds a direct exposed kong breakdown where the discarder pays all shares.
   */
  private KongBreakdown buildDirectExposedKongBreakdown(
      DirectExposedKong event
  ) {
    Map<Seat, Integer> kongDelta = zeroDelta();
    List<SettlementReason> reasons = kongReasons(
      SettlementReason.DIRECT_EXPOSED_KONG,
      event.tileType());

    kongDelta.put(event.declarer(), 3 * event.fan());
    kongDelta.put(event.discarder(), -3 * event.fan());

    return new KongBreakdown(event, kongDelta, reasons);
  }

  /**
   * Builds an upgraded kong breakdown where all other players pay one share.
   */
  private KongBreakdown buildUpgradedKongBreakdown(
      UpgradedKong event
  ) {
    Map<Seat, Integer> kongDelta = zeroDelta();
    List<SettlementReason> reasons = kongReasons(
      SettlementReason.UPGRADED_KONG,
      event.tileType());

    for (Seat seat : Seat.values()) {
      if (seat.equals(event.declarer()))
        kongDelta.put(seat, 3 * event.fan());
      else  
        kongDelta.put(seat, -1 * event.fan());
    }

    return new KongBreakdown(event, kongDelta, reasons);
  }

  /**
   * Builds a concealed kong breakdown where all other players pay one share.
   */
  private KongBreakdown buildConcealedKongBreakdown(
      ConcealedKong event
  ) {
    Map<Seat, Integer> kongDelta = zeroDelta();
    List<SettlementReason> reasons = kongReasons(
      SettlementReason.CONCEALED_KONG,
      event.tileType());

    for (Seat seat : Seat.values()) {
      if (seat.equals(event.declarer()))
        kongDelta.put(seat, 3 * event.fan());
      else  
        kongDelta.put(seat, -1 * event.fan());
    }

    return new KongBreakdown(event, kongDelta, reasons);
  }

  /**
   * Returns the reasons for a kong event, including dragon status when needed.
   */
  private List<SettlementReason> kongReasons(
      SettlementReason baseReason,
      KongTileType tileType
  ) {
    List<SettlementReason> reasons = new ArrayList<>();
    reasons.add(baseReason);
    if (tileType == KongTileType.DRAGON)
      reasons.add(SettlementReason.DRAGON_KONG);

    return reasons;
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
