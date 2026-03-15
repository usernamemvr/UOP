package com.spring.userservice.service;

import com.spring.userservice.dto.UserDto;
import com.spring.userservice.entities.User;
import com.spring.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto createNewUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setRole(userDto.getRole());
        user.setLocation(userDto.getLocation());

        User savedUser = userRepository.save(user);

        return mapToUserDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new RuntimeException("No user found for the given ID: " + id));
        return mapToUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserService::mapToUserDto)
                .toList();
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new RuntimeException("No user found for the given ID: " + id));

        user.setName(userDto.getName());
        user.setRole(userDto.getRole());
        user.setLocation(userDto.getLocation());

        User updatedUser = userRepository.save(user);
        return mapToUserDto(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("No user found for the given ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private static UserDto mapToUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getRole(), user.getLocation());
    }
}
