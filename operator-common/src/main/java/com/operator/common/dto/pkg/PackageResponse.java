package com.operator.common.dto.pkg;

import com.operator.common.dto.library.LibraryPathConfigResponse;
import lombok.Data;
import java.util.List;

@Data
public class PackageResponse {
    private Long id;
    private String name;
    private String description;
    private String businessScenario;
    private String status;
    private String version;
    private String packageTemplate;
    private List<PackageOperatorResponse> operators;
    private List<LibraryPathConfigResponse> commonLibraries;
}
