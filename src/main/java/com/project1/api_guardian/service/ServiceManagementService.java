package com.project1.api_guardian.service;

import com.project1.api_guardian.dto.CreateServiceRequest;
import com.project1.api_guardian.dto.ServiceResponse;
import com.project1.api_guardian.dto.UpdateServiceRequest;
import com.project1.api_guardian.exception.ConflictException;
import com.project1.api_guardian.entity.Project;
import com.project1.api_guardian.entity.Service;
import com.project1.api_guardian.exception.ResourceNotFoundException;
import com.project1.api_guardian.repository.ApiSpecificationRepository;
import com.project1.api_guardian.repository.ProjectRepository;
import com.project1.api_guardian.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final ApiSpecificationRepository specificationRepository;

    public ServiceResponse create(Long projectId, CreateServiceRequest request) {
        Project project = findProject(projectId);
        Service service = Service.create(request.getServiceName(), project);
        return toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listByProject(Long projectId) {
        findProject(projectId);
        return serviceRepository.findAllByProject_Id(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse get(Long id) {
        return toResponse(findService(id));
    }

    public ServiceResponse update(Long id, UpdateServiceRequest request) {
        Service service = findService(id);
        service.setServiceName(request.getServiceName());
        return toResponse(serviceRepository.save(service));
    }

    public void delete(Long id) {
        Service service = findService(id);
        if (specificationRepository.existsByService_Id(id)) {
            throw new ConflictException("Remove the service's API specifications before deleting the service");
        }
        serviceRepository.delete(service);
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private Service findService(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }

    private ServiceResponse toResponse(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getServiceName(),
                service.getProject().getId()
        );
    }
}
