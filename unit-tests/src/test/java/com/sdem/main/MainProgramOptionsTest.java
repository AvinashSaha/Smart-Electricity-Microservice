package com.sdem.main;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MainProgramOptionsTest {

    @Test
    void runsAllFiveCitiesByDefault() {
        LaunchOptions options = LaunchOptions.parse();
        assertThat(options.cities()).extracting(CityPlan::id)
                .containsExactly("newcastle", "sunderland", "durham", "middlesbrough", "darlington");
    }

    @Test
    void parsesSelectedCitiesAndPassesOtherArgumentsThrough() {
        LaunchOptions options = LaunchOptions.parse("--cities=Durham,newcastle", "--data-dir=/tmp/sdem",
                "--sdem.simulation.tick-interval=PT5S");
        assertThat(options.cities()).extracting(CityPlan::id).containsExactly("durham", "newcastle");
        assertThat(options.dataDir()).isEqualTo("/tmp/sdem");
        assertThat(options.passThrough()).containsExactly("--sdem.simulation.tick-interval=PT5S");
    }

    @Test
    void rejectsUnknownCities() {
        assertThatThrownBy(() -> LaunchOptions.parse("--cities=york")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eachCityHasItsOwnPortBlock() {
        CityPlan sunderland = CityPlan.byId("sunderland");
        assertThat(sunderland.smartCityPort()).isEqualTo(8120);
        assertThat(sunderland.providerPort("a")).isEqualTo(8121);
        assertThat(sunderland.providerPort("C")).isEqualTo(8123);
        assertThat(sunderland.citizenPort()).isEqualTo(8124);
    }
}
