package com.operator.service.library;

import com.operator.common.dto.library.*;
import com.operator.common.enums.LibraryType;
import com.operator.common.exception.BadRequestException;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.library.domain.CommonLibrary;
import com.operator.core.library.domain.CommonLibraryFile;
import com.operator.core.library.domain.OperatorCommonLibrary;
import com.operator.core.library.repository.CommonLibraryFileRepository;
import com.operator.core.library.repository.CommonLibraryRepository;
import com.operator.core.library.repository.OperatorCommonLibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共库 Service 实现
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonLibraryServiceImpl implements CommonLibraryService {

    private final CommonLibraryRepository libraryRepository;
    private final CommonLibraryFileRepository libraryFileRepository;
    private final OperatorCommonLibraryRepository operatorCommonLibraryRepository;

    @Override
    @Transactional
    public LibraryResponse createLibrary(LibraryRequest request, String username) {
        log.info("创建公共库：name={}, version={}", request.getName(), request.getVersion());

        // 检查名称+版本是否已存在
        if (libraryRepository.existsByNameAndVersion(request.getName(), request.getVersion())) {
            throw new BadRequestException("该名称和版本的公共库已存在");
        }

        // 创建公共库
        CommonLibrary library = CommonLibrary.builder()
                .name(request.getName())
                .description(request.getDescription())
                .version(request.getVersion())
                .category(request.getCategory())
                .libraryType(request.getLibraryType())
                .build();

        // 保存公共库
        library = libraryRepository.save(library);

        // 保存文件
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (int i = 0; i < request.getFiles().size(); i++) {
                LibraryFileRequest fileRequest = request.getFiles().get(i);
                CommonLibraryFile file = CommonLibraryFile.builder()
                        .library(library)
                        .fileName(fileRequest.getFileName())
                        .filePath(fileRequest.getFilePath())
                        .code(fileRequest.getCode())
                        .orderIndex(fileRequest.getOrderIndex() != null ? fileRequest.getOrderIndex() : i)
                        .build();
                libraryFileRepository.save(file);
            }
        }

        return convertToResponse(library);
    }

    @Override
    @Transactional
    public LibraryResponse updateLibrary(Long id, LibraryRequest request, String username) {
        log.info("更新公共库：id={}", id);

        CommonLibrary library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公共库不存在"));

        // 检查名称+版本是否与其他库冲突
        if (!library.getName().equals(request.getName()) ||
            !library.getVersion().equals(request.getVersion())) {
            if (libraryRepository.existsByNameAndVersion(request.getName(), request.getVersion())) {
                throw new BadRequestException("该名称和版本的公共库已存在");
            }
        }

        // 更新基本信息
        library.setName(request.getName());
        library.setDescription(request.getDescription());
        library.setVersion(request.getVersion());
        library.setCategory(request.getCategory());
        library.setLibraryType(request.getLibraryType());
        library.setUpdatedBy(username);

        // 删除所有旧文件
        libraryFileRepository.deleteByLibraryId(id);

        // 保存新文件
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (int i = 0; i < request.getFiles().size(); i++) {
                LibraryFileRequest fileRequest = request.getFiles().get(i);
                CommonLibraryFile file = CommonLibraryFile.builder()
                        .library(library)
                        .fileName(fileRequest.getFileName())
                        .filePath(fileRequest.getFilePath())
                        .code(fileRequest.getCode())
                        .orderIndex(fileRequest.getOrderIndex() != null ? fileRequest.getOrderIndex() : i)
                        .build();
                libraryFileRepository.save(file);
            }
        }

        library = libraryRepository.save(library);
        return convertToResponse(library);
    }

    @Override
    @Transactional
    public void deleteLibrary(Long id, String username) {
        log.info("删除公共库：id={}", id);

        CommonLibrary library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公共库不存在"));

        // 检查是否有算子正在使用该库
        List<OperatorCommonLibrary> dependencies = operatorCommonLibraryRepository.findByLibraryId(id);
        if (!dependencies.isEmpty()) {
            throw new BadRequestException("该公共库正在被算子使用，无法删除");
        }

        libraryRepository.delete(library);
    }

    @Override
    @Transactional(readOnly = true)
    public LibraryResponse getLibraryById(Long id) {
        CommonLibrary library = libraryRepository.findByIdWithFiles(id)
                .orElseThrow(() -> new ResourceNotFoundException("公共库不存在"));

        LibraryResponse response = convertToResponse(library);

        // 计算使用次数
        long usageCount = operatorCommonLibraryRepository.findByLibraryId(id).size();
        response.setUsageCount(usageCount);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LibraryResponse> searchLibraries(String keyword, String libraryType,
                                                     Integer page, Integer size) {
        log.debug("搜索公共库：keyword={}, libraryType={}, page={}, size={}", keyword, libraryType, page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CommonLibrary> libraryPage;

        // 判断是否有 libraryType 参数
        if (StringUtils.hasText(libraryType)) {
            LibraryType type = LibraryType.valueOf(libraryType);
            log.debug("按类型搜索：libraryType={}", type);
            if (StringUtils.hasText(keyword)) {
                // 同时按类型和关键词搜索
                log.debug("同时按类型和关键词搜索");
                libraryPage = libraryRepository.searchLibrariesByType(type, keyword, pageable);
            } else {
                // 仅按类型搜索
                log.debug("仅按类型搜索");
                libraryPage = libraryRepository.findByLibraryType(type, pageable);
            }
        } else {
            // 未指定类型，搜索所有
            log.debug("搜索所有库");
            if (StringUtils.hasText(keyword)) {
                log.debug("按关键词搜索：keyword={}", keyword);
                libraryPage = libraryRepository.searchLibraries(keyword, pageable);
            } else {
                log.debug("查询所有库（无过滤条件）");
                libraryPage = libraryRepository.findAll(pageable);
            }
        }

        log.debug("查询结果：totalElements={}, contentSize={}", libraryPage.getTotalElements(), libraryPage.getContent().size());

        List<LibraryResponse> responses = libraryPage.getContent().stream()
                .map(lib -> {
                    LibraryResponse response = convertToResponse(lib);
                    long usageCount = operatorCommonLibraryRepository.findByLibraryId(lib.getId()).size();
                    response.setUsageCount(usageCount);
                    return response;
                })
                .collect(Collectors.toList());

        return PageResponse.<LibraryResponse>builder()
                .totalElements(libraryPage.getTotalElements())
                .currentPage(libraryPage.getNumber())
                .pageSize(libraryPage.getSize())
                .content(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LibraryResponse> getLibrariesByType(String libraryType, Pageable pageable) {
        LibraryType type = LibraryType.valueOf(libraryType);
        List<CommonLibrary> libraries = libraryRepository.findByLibraryType(type);

        List<LibraryResponse> responses = libraries.stream()
                .map(lib -> {
                    LibraryResponse response = convertToResponse(lib);
                    long usageCount = operatorCommonLibraryRepository.findByLibraryId(lib.getId()).size();
                    response.setUsageCount(usageCount);
                    return response;
                })
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, libraries.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LibraryResponse> getLibrariesByCategory(String category, Pageable pageable) {
        List<CommonLibrary> libraries = libraryRepository.findByCategory(category);

        List<LibraryResponse> responses = libraries.stream()
                .map(lib -> {
                    LibraryResponse response = convertToResponse(lib);
                    long usageCount = operatorCommonLibraryRepository.findByLibraryId(lib.getId()).size();
                    response.setUsageCount(usageCount);
                    return response;
                })
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(responses, pageable, libraries.size());
    }

    /**
     * 转换为响应 DTO
     */
    private LibraryResponse convertToResponse(CommonLibrary library) {
        List<LibraryFileResponse> fileResponses = library.getFiles().stream()
                .map(this::convertFileToResponse)
                .collect(Collectors.toList());

        return LibraryResponse.builder()
                .id(library.getId())
                .name(library.getName())
                .description(library.getDescription())
                .version(library.getVersion())
                .category(library.getCategory())
                .libraryType(library.getLibraryType())
                .createdBy(library.getCreatedBy() != null ? Long.parseLong(library.getCreatedBy()) : null)
                .createdAt(library.getCreatedAt())
                .updatedAt(library.getUpdatedAt())
                .files(fileResponses)
                .build();
    }

    /**
     * 转换文件为响应 DTO
     */
    private LibraryFileResponse convertFileToResponse(CommonLibraryFile file) {
        return LibraryFileResponse.builder()
                .id(file.getId())
                .libraryId(file.getLibrary() != null ? file.getLibrary().getId() : null)
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .code(file.getCode())
                .orderIndex(file.getOrderIndex())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public LibraryFileResponse createLibraryFile(Long libraryId, LibraryFileCreateRequest request, String username) {
        log.info("创建库文件：libraryId={}, fileName={}", libraryId, request.getFileName());

        CommonLibrary library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("公共库不存在"));

        CommonLibraryFile file = CommonLibraryFile.builder()
                .library(library)
                .fileName(request.getFileName())
                .code("")  // 创建空文件，不包含代码
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();

        file = libraryFileRepository.save(file);

        log.info("库文件创建成功：fileId={}", file.getId());

        return convertFileToResponse(file);
    }

    @Override
    @Transactional
    public void updateLibraryFileName(Long libraryId, Long fileId, LibraryFileRenameRequest request, String username) {
        log.info("更新库文件名：libraryId={}, fileId={}, newFileName={}", libraryId, fileId, request.getFileName());

        CommonLibraryFile file = libraryFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));

        // 验证文件属于该库
        if (file.getLibrary().getId().equals(libraryId)) {
            file.setFileName(request.getFileName());
            file.setUpdatedBy(username);
            libraryFileRepository.save(file);
            log.info("库文件名更新成功：fileId={}", fileId);
        } else {
            throw new BadRequestException("文件不属于该公共库");
        }
    }

    @Override
    @Transactional
    public void updateLibraryFileContent(Long libraryId, Long fileId, LibraryFileContentUpdateRequest request, String username) {
        log.info("更新库文件内容：libraryId={}, fileId={}", libraryId, fileId);

        CommonLibraryFile file = libraryFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));

        // 验证文件属于该库
        if (file.getLibrary().getId().equals(libraryId)) {
            file.setCode(request.getCode());
            file.setUpdatedBy(username);
            libraryFileRepository.save(file);
            log.info("库文件内容更新成功：fileId={}", fileId);
        } else {
            throw new BadRequestException("文件不属于该公共库");
        }
    }

    @Override
    @Transactional
    public void deleteLibraryFile(Long libraryId, Long fileId, String username) {
        log.info("删除库文件：libraryId={}, fileId={}", libraryId, fileId);

        CommonLibraryFile file = libraryFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));

        // 验证文件属于该库
        if (file.getLibrary().getId().equals(libraryId)) {
            libraryFileRepository.delete(file);
            log.info("库文件删除成功：fileId={}", fileId);
        } else {
            throw new BadRequestException("文件不属于该公共库");
        }
    }
}
