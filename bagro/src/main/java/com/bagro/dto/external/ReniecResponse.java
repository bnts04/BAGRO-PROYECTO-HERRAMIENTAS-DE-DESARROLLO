package com.bagro.dto.external;

public record ReniecResponse(
        String document_number,
        String first_name,
        String first_last_name,
        String second_last_name
) {
}