package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.UserCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.UserUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse createStaff(UserCreateRequest request);

    UserResponse updateStaff(Long id, UserUpdateRequest request);

    void delete(Long id);

    List<UserResponse> findAll();

    PageDTO<UserResponse> getPagination(Map<String, String> params);

    UserResponse uploadImage(Long id, MultipartFile file) throws Exception;


    List<UserResponse> getAll();

    UserResponse changeStatus(Long id);

    UserResponse unlockAccount(Long id);
}