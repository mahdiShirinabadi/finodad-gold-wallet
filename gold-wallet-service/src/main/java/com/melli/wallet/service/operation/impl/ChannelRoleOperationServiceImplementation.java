package com.melli.wallet.service.operation.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.persistence.ChannelRepository;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.domain.master.persistence.RoleRepository;
import com.melli.wallet.domain.response.panel.ChannelListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.ChannelRoleOperationService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChannelRoleOperationServiceImplementation implements ChannelRoleOperationService {
    
    private final ChannelRepository channelRepository;
    private final RoleRepository roleRepository;
    private final ChannelRoleRepository channelRoleRepository;

    
    @Override
    @Transactional
    @LogExecutionTime("Assign role to channel")
    public void assignRoleToChannel(Long roleId, Long channelId, String createdBy) throws InternalServiceException {
        log.info("Starting assignRoleToChannel - roleId: {}, channelId: {}, createdBy: {}", roleId, channelId, createdBy);
        
        try {
            // Check if role exists
            RoleEntity role = roleRepository.findById(roleId).orElse(null);
            if (role == null) {
                log.error("Role not found - roleId: {}", roleId);
                throw new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND);
            }
            log.info("Role found - roleId: {}, roleName: {}", roleId, role.getName());
            
            // Check if channel exists
            ChannelEntity channel = channelRepository.findById(channelId).orElse(null);
            if (channel == null) {
                log.error("Channel not found - channelId: {}", channelId);
                throw new InternalServiceException(StatusRepositoryService.CHANNEL_NOT_FOUND);
            }
            log.info("Channel found - channelId: {}, channelName: {}", channelId, channel.getFirstName() + " " + channel.getLastName());
            
            // First, delete all existing role assignments for this channel
            List<ChannelRoleEntity> existingAssignments = channelRoleRepository.findByChannelId(channelId);
            log.info("Existing role assignments for channelId: {} - count: {}", channelId, existingAssignments.size());
            
            if (!existingAssignments.isEmpty()) {
                log.info("Deleting existing role assignments for channelId: {}", channelId);
                channelRoleRepository.deleteAll(existingAssignments);
            }
            
            // Then, create new channel-role assignment
            log.info("Creating new role assignment - roleId: {}, channelId: {}", roleId, channelId);
            ChannelRoleEntity channelRole = new ChannelRoleEntity();
            channelRole.setRoleEntity(role);
            channelRole.setChannelEntity(channel);
            channelRole.setCreatedBy(createdBy);
            channelRole.setCreatedAt(new java.util.Date());
            
            channelRoleRepository.save(channelRole);
            
            log.info("Successfully completed assignRoleToChannel - roleId: {}, channelId: {}", roleId, channelId);
            
        } catch (InternalServiceException e) {
            log.error("InternalServiceException in assignRoleToChannel - roleId: {}, channelId: {}, error: {}", roleId, channelId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in assignRoleToChannel - roleId: {}, channelId: {}, error: {}", roleId, channelId, e.getMessage(), e);
            throw new InternalServiceException(StatusRepositoryService.GENERAL_ERROR);
        }
    }
    

    @Override
    @LogExecutionTime("Get channel list")
    public List<ChannelListResponse> getChannelList() throws InternalServiceException {
        log.info("Starting getChannelList");
        
        try {
            // Get all channels with their assigned roles
            List<ChannelRoleEntity> channelRoles = channelRoleRepository.findAllWithChannelAndRole();
            log.info("Found {} channel-role assignments", channelRoles.size());
            
            // Group by channel and create response
            Map<ChannelEntity, List<RoleEntity>> channelRoleMap = channelRoles.stream()
                    .collect(Collectors.groupingBy(
                            ChannelRoleEntity::getChannelEntity,
                            Collectors.mapping(
                                    ChannelRoleEntity::getRoleEntity,
                                    Collectors.toList()
                            )
                    ));
            
            log.info("Grouped into {} unique channels", channelRoleMap.size());
            
            // Convert to ChannelListResponse
            List<ChannelListResponse> result = channelRoleMap.entrySet().stream()
                    .map(entry -> {
                        ChannelEntity channel = entry.getKey();
                        List<RoleEntity> roles = entry.getValue();
                        
                        List<ChannelListResponse.RoleSummaryResponse> roleSummaries = roles.stream()
                                .map(role -> ChannelListResponse.RoleSummaryResponse.builder()
                                        .id(role.getId())
                                        .name(role.getName())
                                        .persianDescription(role.getPersianDescription())
                                        .build())
                                .collect(Collectors.toList());
                        
                        return ChannelListResponse.builder()
                                .id(channel.getId())
                                .name(channel.getFirstName() + " " + channel.getLastName())
                                .username(channel.getUsername())
                                .description(channel.getStatus())
                                .isActive("ACTIVE".equals(channel.getStatus()))
                                .assignedRoles(roleSummaries)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("Successfully completed getChannelList - returned {} channels", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("Unexpected error in getChannelList - error: {}", e.getMessage(), e);
            throw new InternalServiceException(StatusRepositoryService.GENERAL_ERROR);
        }
    }
}
