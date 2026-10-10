package com.sdem.integration;

import com.sdem.citizen.service.SimulationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests: the citizen simulation, the three providers, the smart
 * city service and the web front end running together and talking over HTTP.
 * Tests run in order because each step builds on the data of the previous one.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemIntegrationTest {

    private static TestSystem system;

    @BeforeAll
    static void startSystem() {
        system = new TestSystem().start();
    }

    @AfterAll
    static void stopSystem() {
        if (system != null) {
            system.close();
        }
    }

    // ---- Microservices working together -------------------------------------

    @Test
    @Order(1)
    void citizenSimulationDeliversReadingsToEveryProvider() {
        system.bean(system.citizen, SimulationService.class).tick();

        for (String code : TestSystem.PROVIDERS) {
            HttpResponse<String> summary = api(system.providerPorts.get(code)).get("/api/v1/summary");
            assertThat(summary.statusCode()).isEqualTo(200);
            // 3 citizens per provider, each sending opening + 3 months + month to date = 15 readings at least
            assertThat(number(summary.body(), "readingCount")).isGreaterThanOrEqualTo(15);
            assertThat(number(summary.body(), "citizenCount")).isEqualTo(3);
        }
        assertThat(system.bean(system.citizen, SimulationService.class).status().pendingBackfill()).isZero();
    }

    @Test
    @Order(2)
    void manualEntryIsForwardedToTheProviderInRealTime() {
        HttpSession citizenApi = api(system.citizenPort);
        String citizen = citizenApi.get("/api/v1/citizens/NEWCASTLE-B-M001").body();
        double next = decimal(citizen, "lastReadingKwh") + 12.5;
        String value = String.format(java.util.Locale.UK, "%.3f", next);

        HttpResponse<String> submit = citizenApi.postJson("/api/v1/citizens/NEWCASTLE-B-M001/readings",
                "{\"readingKwh\":" + value + "}");
        assertThat(submit.statusCode()).isEqualTo(201);

        String latest = api(system.providerPorts.get("b"))
                .get("/api/v1/readings?citizenId=NEWCASTLE-B-M001&limit=1").body();
        assertThat(decimal(latest, "readingKwh")).isEqualTo(Double.parseDouble(value), org.assertj.core.data.Offset.offset(0.0005));
        assertThat(latest).contains("\"meterType\":\"MANUAL\"");
    }

    @Test
    @Order(3)
    void lowerReadingsAreRejectedByCitizenAndProvider() {
        assertThat(api(system.citizenPort).postJson("/api/v1/citizens/NEWCASTLE-B-M001/readings",
                "{\"readingKwh\":1}").statusCode()).isEqualTo(422);
        assertThat(api(system.providerPorts.get("b")).postJson("/api/v1/readings",
                "{\"citizenId\":\"NEWCASTLE-B-M001\",\"meterType\":\"MANUAL\",\"readingKwh\":1}").statusCode())
                .isEqualTo(422);
    }

    @Test
    @Order(4)
    void smartCityCollectsAndStoresAggregatesFromAllProviders() {
        HttpSession cityApi = api(system.smartCityPort);
        HttpResponse<String> collect = cityApi.postJson("/api/v1/aggregates/collect", "");
        assertThat(collect.statusCode()).isEqualTo(200);
        assertThat(collect.body()).doesNotContain("\"reachable\":false");

        String aggregates = cityApi.get("/api/v1/aggregates?months=3").body();
        assertThat(aggregates).contains("\"provider\":\"A\"", "\"provider\":\"B\"", "\"provider\":\"C\"");
        assertThat(aggregates).contains("\"city\":\"Newcastle\"");

        String summary = cityApi.get("/api/v1/summary?months=3").body();
        assertThat(decimal(summary, "totalKwh")).isGreaterThan(0);
    }

    // ---- Front end working with the microservices ----------------------------

    @Test
    @Order(5)
    void pagesRequireSignIn() {
        HttpResponse<String> response = web().get("/");
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("Location").orElse("")).endsWith("/login");
    }

    @Test
    @Order(6)
    void wrongPasswordIsRefused() {
        HttpResponse<String> response = web().login("admin", "not-the-password");
        assertThat(response.headers().firstValue("Location").orElse("")).contains("/login?error");
    }

    @Test
    @Order(7)
    void cityPageShowsTheDataTableAndChartFromTheSmartCityService() {
        HttpSession browser = signedIn("viewer", "viewer123");

        HttpResponse<String> page = browser.get("/cities/newcastle?months=3");

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains("Newcastle by provider", "Provider A (kWh)", "Provider C (kWh)");
        assertThat(page.body()).contains("Monthly average per citizen in Newcastle");
        assertThat(page.body()).doesNotContain("No data has been collected");
    }

    @Test
    @Order(8)
    void dashboardAndProviderPagesHandleCitiesThatAreDown() {
        HttpSession browser = signedIn("viewer", "viewer123");

        String dashboard = browser.get("/").body();
        assertThat(dashboard).contains("Monthly average per citizen, all cities", "All cities");
        assertThat(dashboard).contains("1 / 5");
        assertThat(dashboard).contains("Unavailable");

        String provider = browser.get("/providers/a").body();
        assertThat(provider).contains("Provider A by city", "Newcastle (kWh)", "Durham (kWh)");
        assertThat(provider).contains("Durham smart city service is unavailable");
    }

    @Test
    @Order(9)
    void unknownCityGives404() {
        assertThat(signedIn("viewer", "viewer123").get("/cities/york").statusCode()).isEqualTo(404);
    }

    @Test
    @Order(10)
    void onlyAdministratorsCanManageUsers() {
        assertThat(signedIn("viewer", "viewer123").get("/admin/users").statusCode()).isEqualTo(403);
        assertThat(signedIn("admin", "admin123").get("/admin/users").statusCode()).isEqualTo(200);
    }

    @Test
    @Order(11)
    void administratorCreatesAUserWhoChangesTheirPasswordAndIsThenDeleted() {
        HttpSession admin = signedIn("admin", "admin123");
        HttpResponse<String> created = admin.submitForm("/admin/users", "/admin/users",
                Map.of("username", "analyst", "password", "first-pass-1", "role", "USER"));
        assertThat(created.statusCode()).isEqualTo(302);
        assertThat(admin.get("/admin/users").body()).contains("analyst");

        HttpSession analyst = signedIn("analyst", "first-pass-1");
        HttpResponse<String> changed = analyst.submitForm("/account/password", "/account/password",
                Map.of("currentPassword", "first-pass-1", "newPassword", "second-pass-2",
                        "confirmPassword", "second-pass-2"));
        assertThat(changed.statusCode()).isEqualTo(302);

        assertThat(web().login("analyst", "first-pass-1").headers().firstValue("Location").orElse(""))
                .contains("error");
        signedIn("analyst", "second-pass-2");

        admin.submitForm("/admin/users", "/admin/users/analyst/delete", Map.of());
        assertThat(admin.get("/admin/users").body()).doesNotContain(">analyst<");
        assertThat(web().login("analyst", "second-pass-2").headers().firstValue("Location").orElse(""))
                .contains("error");
    }

    @Test
    @Order(12)
    void theLastAdministratorCannotDeleteThemselves() {
        HttpSession admin = signedIn("admin", "admin123");
        admin.submitForm("/admin/users", "/admin/users/admin/delete", Map.of());
        assertThat(admin.get("/admin/users").body()).contains(">admin<");
    }

    // ---- helpers -------------------------------------------------------------

    private static HttpSession api(int port) {
        return new HttpSession(TestSystem.url(port));
    }

    private static HttpSession web() {
        return new HttpSession(TestSystem.url(system.frontEndPort));
    }

    private static HttpSession signedIn(String user, String password) {
        HttpSession session = web();
        HttpResponse<String> login = session.login(user, password);
        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(login.headers().firstValue("Location").orElse("")).doesNotContain("error");
        return session;
    }

    private static long number(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\":(\\d+)").matcher(json);
        assertThat(m.find()).as("field %s in %s", field, json).isTrue();
        return Long.parseLong(m.group(1));
    }

    private static double decimal(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\":([0-9.]+)").matcher(json);
        assertThat(m.find()).as("field %s in %s", field, json).isTrue();
        return Double.parseDouble(m.group(1));
    }
}
