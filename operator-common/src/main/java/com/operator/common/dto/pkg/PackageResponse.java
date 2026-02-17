package com.operator.common.dto.pkg;

import lombok.Data;
import com.operator.common.dto.pkg.PackageOperatorResponse;
import java.util.List;
@Data
public class PackageResponse {
    private Long id;
    private String name;
    private String description;
    private String businessScenario;
    private String status;
    private String version;
    private List<PackageOperatorResponse> operators;
}
