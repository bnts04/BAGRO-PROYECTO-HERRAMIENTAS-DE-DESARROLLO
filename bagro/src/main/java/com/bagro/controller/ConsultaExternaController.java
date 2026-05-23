package com.bagro.controller;

import com.bagro.dto.external.ReniecResponse;
import com.bagro.dto.external.SunatRucResponse;
import com.bagro.integration.ReniecClient;
import com.bagro.integration.SunatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaExternaController {

    private final ReniecClient reniecClient;
    private final SunatClient sunatClient;

    public ConsultaExternaController(ReniecClient reniecClient,
                                     SunatClient sunatClient) {
        this.reniecClient = reniecClient;
        this.sunatClient = sunatClient;
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ReniecResponse consultarDni(@PathVariable String dni) {
        return reniecClient.consultarPorDni(dni);
    }

    @GetMapping("/ruc/{ruc}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public SunatRucResponse consultarRuc(@PathVariable String ruc) {
        return sunatClient.consultarPorRuc(ruc);
    }
}