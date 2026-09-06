package supersymmetry.api.rocketry.rockets;

public enum ComponentValidationResult {

    SUCCESS("success"),
    INVALID_CARD("invalid_card"),
    VALIDATION_FAILURE("validation_failure"),
    INVALID_AMOUNT(
            "invalid_amount"),
    INCOMPATIBLE_CARD("incompatible_card"),
    FAIRING_TOO_SMALL("fairing_too_small"),
    UNKNOWN("unknown");

    private String name;

    ComponentValidationResult(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getTranslationKey() {
        return "susy.rocketry.components.validation_codes." + this.name;
    }
}
