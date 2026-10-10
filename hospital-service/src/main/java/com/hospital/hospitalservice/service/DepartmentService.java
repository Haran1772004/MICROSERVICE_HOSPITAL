package com.hospital.hospitalservice.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.dto.DepartmentRequest;
import com.hospital.hospitalservice.dto.DepartmentResponse;
import com.hospital.hospitalservice.entity.Department;
import com.hospital.hospitalservice.repository.DepartmentRepository;

/**
 * Business logic for departments. Reads are cached in the cache {@code departments}. Every
 * change clears this cache and the cache {@code doctors}, because a doctor contains its
 * department.
 */
@Service
public class DepartmentService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final DepartmentRepository departmentRepository;

    /**
     * Creates the service.
     *
     * @param departmentRepository database access for departments
     */
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new ACTIVE department.
     *
     * @param request the department data
     * @return the saved department
     * @throws BadRequestException if the name is missing or too long
     * @throws ConflictException   if the name is already used
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "departments", allEntries = true),
            @CacheEvict(value = "doctors", allEntries = true)})
    public DepartmentResponse addDepartment(DepartmentRequest request) {
        String name = Validation.clean(request.name());
        String description = Validation.clean(request.description());
        checkFields(name, description);
        if (departmentRepository.existsByName(name)) {
            throw new ConflictException("Department with name already exists: " + name);
        }
        Department department = new Department(name, description, AccountStatus.ACTIVE);
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    /**
     * Changes the name and description of a department.
     *
     * @param departmentId id of the department
     * @param request      the new data
     * @return the saved department
     * @throws ResourceNotFoundException if the department does not exist
     * @throws BadRequestException       if the name is missing or too long
     * @throws ConflictException         if the name is used by another department
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "departments", allEntries = true),
            @CacheEvict(value = "doctors", allEntries = true)})
    public DepartmentResponse updateDepartment(int departmentId, DepartmentRequest request) {
        Department department = find(departmentId);
        String name = Validation.clean(request.name());
        String description = Validation.clean(request.description());
        checkFields(name, description);
        if (departmentRepository.existsByNameAndDepartmentIdNot(name, departmentId)) {
            throw new ConflictException("Department with name already exists: " + name);
        }
        department.setName(name);
        department.setDescription(description);
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    /**
     * Sets a department to INACTIVE.
     *
     * @param departmentId id of the department
     * @throws ResourceNotFoundException if the department does not exist
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "departments", allEntries = true),
            @CacheEvict(value = "doctors", allEntries = true)})
    public void deactivateDepartment(int departmentId) {
        Department department = find(departmentId);
        department.setStatus(AccountStatus.INACTIVE);
        departmentRepository.save(department);
    }

    /**
     * Sets a department to ACTIVE.
     *
     * @param departmentId id of the department
     * @throws ResourceNotFoundException if the department does not exist
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "departments", allEntries = true),
            @CacheEvict(value = "doctors", allEntries = true)})
    public void activateDepartment(int departmentId) {
        Department department = find(departmentId);
        department.setStatus(AccountStatus.ACTIVE);
        departmentRepository.save(department);
    }

    /**
     * Returns one department. The answer is cached.
     *
     * @param departmentId id of the department
     * @return the department
     * @throws ResourceNotFoundException if the department does not exist
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "#p0")
    public DepartmentResponse getDepartmentById(int departmentId) {
        return DepartmentResponse.from(find(departmentId));
    }

    /**
     * Returns all departments. The answer is cached.
     *
     * @return the departments
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "'all'")
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    private Department find(int departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + departmentId));
    }

    private void checkFields(String name, String description) {
        Validation.required(name, "Department name is required");
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("Department name must be at most 100 characters");
        }
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("Description must be at most 255 characters");
        }
    }
}
