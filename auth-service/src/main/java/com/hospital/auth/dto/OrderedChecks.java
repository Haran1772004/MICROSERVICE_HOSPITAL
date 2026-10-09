package com.hospital.auth.dto;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * Validation order: first the "required" rules (default group), then {@link FormatChecks}.
 * Use it as {@code @Validated(OrderedChecks.class)} on a request body.
 */
@GroupSequence({Default.class, FormatChecks.class})
public interface OrderedChecks {
}
