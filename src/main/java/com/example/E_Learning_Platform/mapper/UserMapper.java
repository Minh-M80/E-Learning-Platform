package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.request.UserCreationRequest;
import com.example.E_Learning_Platform.dto.request.UserRequest;
import com.example.E_Learning_Platform.dto.request.UserUpdateRequest;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
     User toUser(UserCreationRequest userCreationRequest);
     UserResponse toUserResponse(User user);

     UserUpdateRequest toUserUpdateRequest(UserRequest request);

     @Mapping(target = "roles",ignore = true)
     void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

}
