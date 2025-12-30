package com.bantunes82.ledger.datatransferobject;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record ErrorDTO(Map<String, String> errors) {
    public ErrorDTO {
        if (Objects.isNull(errors)) {
            errors = Collections.emptyMap();
        } else {
            errors = Map.copyOf(errors);
        }
    }
}
