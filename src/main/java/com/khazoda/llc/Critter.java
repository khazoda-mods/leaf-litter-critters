package com.khazoda.llc;

import java.util.Locale;

enum Critter {
  WOODLOUSE(false),
  LADYBIRD(false),
  WORM(false),
  SHIELDBUG(true),
  FROGHOPPER(true),
  MILLIPEDE(false),
  YELLOW_FLAT_FOOTED_FLY(true);

  final String texture = name().toLowerCase(Locale.ROOT);
  final boolean airborne;

  Critter(boolean airborne) {
    this.airborne = airborne;
  }
}
