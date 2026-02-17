package com.operator.service.version;

import com.operator.common.dto.version.*;
import com.operator.common.enums.VersionStatus;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.core.version.domain.Version;
import com.operator.core.version.repository.VersionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Version Service Implementation (Stub)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;

    @Override
    @Transactional
    public VersionResponse createVersion(VersionRequest request, String username) {
        log.info("Creating version {} by user: {}", request.getVersionNumber(), username);

        Version version = Version.builder()
                .versionNumber(request.getVersionNumber())
                .description(request.getDescription())
                .status(VersionStatus.DRAFT)
                .isReleased(false)
                .build();

        version.setCreatedBy(username);

        version = versionRepository.save(version);

        return mapToResponse(version);
    }

    @Override
    public VersionResponse getVersionById(Long id) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Version", id));
        return mapToResponse(version);
    }

    @Override
    @Transactional
    public VersionResponse updateVersionStatus(Long id, String status, String username) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Version", id));

        version.setStatus(VersionStatus.valueOf(status));
        version.setUpdatedBy(username);
        version = versionRepository.save(version);

        return mapToResponse(version);
    }

    @Override
    @Transactional
    public VersionResponse releaseVersion(Long id, String username) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Version", id));

        version.setStatus(VersionStatus.PUBLISHED);
        version.setIsReleased(true);
        version.setReleaseDate(LocalDateTime.now());
        version.setUpdatedBy(username);
        version = versionRepository.save(version);

        return mapToResponse(version);
    }

    @Override
    public VersionComparison compareVersions(Long versionId1, Long versionId2) {
        Version v1 = versionRepository.findById(versionId1)
                .orElseThrow(() -> new ResourceNotFoundException("Version", versionId1));
        Version v2 = versionRepository.findById(versionId2)
                .orElseThrow(() -> new ResourceNotFoundException("Version", versionId2));

        List<String> differences = new ArrayList<>();
        if (!v1.getVersionNumber().equals(v2.getVersionNumber())) {
            differences.add("Version number: " + v1.getVersionNumber() + " vs " + v2.getVersionNumber());
        }
        if (!v1.getStatus().equals(v2.getStatus())) {
            differences.add("Status: " + v1.getStatus() + " vs " + v2.getStatus());
        }

        return new VersionComparison(
                mapToResponse(v1),
                mapToResponse(v2),
                differences
        );
    }

    @Override
    @Transactional
    public void deleteVersion(Long id, String username) {
        Version version = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Version", id));

        versionRepository.delete(version);
    }

    private VersionResponse mapToResponse(Version version) {
        return VersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .description(version.getDescription())
                .status(version.getStatus())
                .codeFilePath(version.getCodeFilePath())
                .fileName(version.getFileName())
                .fileSize(version.getFileSize())
                .gitCommitHash(version.getGitCommitHash())
                .gitTag(version.getGitTag())
                .isReleased(version.getIsReleased())
                .releaseDate(version.getReleaseDate())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
    }
}
